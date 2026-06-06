import json
import logging
from typing import Optional
import httpx
from openai import OpenAI
from config import settings

logger = logging.getLogger(__name__)
_client = None


def get_client() -> OpenAI:
    global _client
    if _client is None:
        if not settings.qwen_api_key:
            logger.warning("QWEN_API_KEY not set, current env DASHSCOPE_API_KEY=" + os.getenv("DASHSCOPE_API_KEY", ""))
            return None
        http_client = httpx.Client(
            proxy=None,
            transport=httpx.HTTPTransport(local_address="0.0.0.0"),
        )
        _client = OpenAI(
            api_key=settings.qwen_api_key,
            base_url=settings.qwen_base_url,
            http_client=http_client,
        )
    logger.info(f"Qwen client ready, base_url={settings.qwen_base_url}, key_prefix={settings.qwen_api_key[:8]}..."); return _client


def query_database(sql: str) -> str:
    import pymysql
    if not sql.strip().upper().startswith("SELECT"):
        return "error: SELECT only"
    if "INTO OUTFILE" in sql.upper() or "INTO DUMPFILE" in sql.upper():
        return "error: file ops not allowed"
    try:
        conn = pymysql.connect(
            host=settings.db_host, port=settings.db_port, user=settings.db_user,
            password=settings.db_pass, database=settings.db_name,
            charset="utf8mb4", cursorclass=pymysql.cursors.DictCursor,
        )
        with conn.cursor() as cursor:
            cursor.execute(sql)
            rows = cursor.fetchmany(100)
        conn.close()
        if not rows:
            return "empty result"
        cols = list(rows[0].keys())
        lines = [f"result ({len(rows)} rows):", "\t".join(cols)]
        for row in rows:
            lines.append("\t".join(str(row.get(c, "NULL")) for c in cols))
        return "\n".join(lines)
    except Exception as e:
        return f"SQL error: {e}"


SYSTEM_PROMPT = (
    '你是\u300c健康大数据应用创新研发中心统计分析决策系统\u300d的智能分析助手，叫小康。'
    '你专门帮助研发人员和管理人员分析医疗健康数据，提供统计解读、决策建议和医疗资源配置优化建议。\n'
    '行为准则：\n'
    '1. 语气亲切自然，像同事一样交流。\n'
    '2. 回答结构化，优先用简洁列表或短段落。\n'
    '3. 涉及具体数据时使用 query_database 工具查询。\n'
    '4. 非数据问题直接用自己的知识回答。\n'
    '5. 回答控制在 200 字以内，除非用户要求详细分析。\n'
    '6. 数据库表包括：medical_institution, medical_staff, medical_bed, '
    'medical_service, medical_cost, dim_population, dim_region。'
)

TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "query_database",
            "description": "执行 SQL SELECT 查询获取数据库中的真实数据",
            "parameters": {
                "type": "object",
                "properties": {
                    "sql": {
                        "type": "string",
                        "description": "完整的 SELECT 语句"
                    }
                },
                "required": ["sql"],
            },
        },
    }
]


def chat(question: str, session_id: Optional[str] = None) -> dict:
    c = get_client()
    if not c:
        return {"code": 500, "msg": "QWEN_API_KEY not set"}
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": question},
    ]
    try:
        resp = c.chat.completions.create(
            model=settings.qwen_model,
            messages=messages,
            tools=TOOLS,
            temperature=0.7,
            max_tokens=4096,
        )
        msg = resp.choices[0].message
        if msg.tool_calls:
            messages.append(
                {
                    "role": "assistant",
                    "content": msg.content or "",
                    "tool_calls": [
                        {
                            "id": tc.id,
                            "type": "function",
                            "function": {
                                "name": tc.function.name,
                                "arguments": tc.function.arguments,
                            },
                        }
                        for tc in msg.tool_calls
                    ],
                }
            )
            for tc in msg.tool_calls:
                args = json.loads(tc.function.arguments)
                if tc.function.name == "query_database":
                    result = query_database(args.get("sql", ""))
                else:
                    result = f"unknown tool: {tc.function.name}"
                messages.append(
                    {"role": "tool", "tool_call_id": tc.id, "content": result}
                )
            resp2 = c.chat.completions.create(
                model=settings.qwen_model,
                messages=messages,
                temperature=0.7,
                max_tokens=2048,
            )
            content = resp2.choices[0].message.content or ""
        else:
            content = msg.content or ""
        return {"code": 200, "data": content}
    except Exception as e:
        logger.error(f"API call failed: {e}", exc_info=True)
        return {"code": 500, "msg": str(e)}


async def chat_stream(question: str, session_id: Optional[str] = None):
    c = get_client()
    if not c:
        yield 'data: {"error": "QWEN_API_KEY not set"}\n\n'
        yield "data: [DONE]\n\n"
        return
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": question},
    ]
    try:
        stream = c.chat.completions.create(
            model=settings.qwen_model,
            messages=messages,
            tools=TOOLS,
            temperature=0.7,
            max_tokens=4096,
            stream=True,
        )
        full_content = ""
        fn_buffer = ""
        fn_name = None
        collecting = False
        for chunk in stream:
            delta = chunk.choices[0].delta if chunk.choices else None
            if not delta:
                continue
            if delta.content:
                full_content += delta.content
                yield f"data: {delta.content}\n\n"
            if delta.tool_calls:
                for tc in delta.tool_calls:
                    if tc.function and tc.function.name:
                        fn_name = tc.function.name
                    if tc.function and tc.function.arguments:
                        fn_buffer += tc.function.arguments
                    collecting = True
            fr = chunk.choices[0].finish_reason if chunk.choices else None
            if fr == "tool_calls" and collecting and fn_name:
                args = json.loads(fn_buffer)
                if fn_name == "query_database":
                    result = query_database(args.get("sql", ""))
                else:
                    result = f"unknown tool: {fn_name}"
                messages.append({"role": "assistant", "content": full_content})
                messages.append(
                    {"role": "tool", "tool_call_id": "call_1", "content": result}
                )
                resp2 = c.chat.completions.create(
                    model=settings.qwen_model,
                    messages=messages,
                    temperature=0.7,
                    max_tokens=2048,
                )
                yield f"data: {resp2.choices[0].message.content or ''}\n\n"
                break
        yield "data: [DONE]\n\n"
    except Exception as e:
        logger.error(f"stream error: {e}", exc_info=True)
        yield f"data: {json.dumps({'error': str(e)})}\n\n"
        yield "data: [DONE]\n\n"
