import json
import logging
import os
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
    logger.info(f"query_database SQL: {sql}")
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
    '你是「健康大数据应用创新研发中心统计分析决策系统」的智能分析助手，叫小康。'
    '你专门帮助研发人员和管理人员分析医疗健康数据，提供统计解读、决策建议和医疗资源配置优化建议。\n'
    '\n'
    '【数据库结构】\n'
    '你有以下数据表可用，必须通过 query_database 工具查询，严禁猜测数据：\n'
    '\n'
    '1. dim_region（区域维度）：id, region_code(区域编码), region_name(区域名称), '
    'region_level(级别:1省/2市/3区), parent_id(上级区域ID)\n'
    '2. dim_population（人口维度）：id, region_id(关联dim_region), total_population(总人口), '
    'male_population(男性人口), female_population(女性人口), age_0_14, age_15_59, age_60_plus, stat_year(统计年份)\n'
    '3. medical_institution（医疗机构）：id, org_code(机构编码), org_name(机构名称), '
    'org_type(机构类型), org_level(机构等级), region_id(关联dim_region), address(地址), '
    'contact_phone(联系电话), is_active(1启用/0停用)\n'
    '4. medical_staff（医务人员）：id, staff_code(人员编号), staff_name(姓名), '
    'gender(性别:1男/2女), birth_date(出生日期), org_id(关联medical_institution), '
    'department(科室), job_title(职称), job_category(职业类别), education(学历), is_active\n'
    '5. medical_bed（医疗床位）：id, org_id(关联medical_institution), bed_count(编制床位数), '
    'actual_bed_count(实际开放床位数), used_bed_count(占用床位数), bed_usage_rate(使用率%), '
    'dept_type(科室类型), stat_year(统计年份), stat_month(统计月份)\n'
    '6. medical_service（医疗服务）：id, service_code(服务编码), org_id(关联medical_institution), '
    'service_category(服务类别,大类:门诊/住院/急诊/体检), patient_gender(患者性别), patient_age(患者年龄), '
    'service_type(服务类型,具体科室:如ICU/心内科/骨科/CCU等), department(科室), diagnosis_code(ICD-10诊断编码), '
    'diagnosis_name(诊断名称), doctor_id(接诊医生ID,关联medical_staff), service_date(服务日期), '
    'discharge_date(出院日期), days_in_hospital(住院天数)\n'
    '7. medical_cost（医疗费用）：id, service_id(关联medical_service,一对一), '
    'total_cost(总费用), drug_cost(药品费), treatment_cost(治疗费), surgery_cost(手术费), '
    'inspection_cost(检查费), laboratory_cost(化验费), bed_cost(床位费), nursing_cost(护理费), '
    'insurance_paid(医保支付), self_paid(自付金额)\n'
    '\n'
    '【表间关系】\n'
    '- dim_region → medical_institution(通过region_id), dim_population(通过region_id)\n'
    '- medical_institution → medical_staff(通过org_id), medical_bed(通过org_id), medical_service(通过org_id)\n'
    '- medical_staff → medical_service(通过doctor_id)\n'
    '- medical_service → medical_cost(通过service_id, 一对一)\n'
    '- 查询患者信息时，患者姓名/性别/年龄在 medical_service 表中，费用明细在 medical_cost 表中\n'
    '\n'
    '【回答规范 — 必须严格遵守】\n'
    '1. 涉及数据的问题，直接调用 query_database 查询，严禁先输出分析、猜测或解释表结构。\n'
    '2. 查询到结果后，用自然流畅的语言总结数据：先给出核心结论，再展开关键细节。\n'
    '3. 严禁输出以下内容：\n'
    '   - 原始SQL语句\n'
    '   - 未经加工的查询结果原文（如制表符分隔的文本行）\n'
    '   - 你的思考过程、推理链、内心独白\n'
    '   - 表结构说明（除非用户明确询问数据库结构）\n'
    '4. 回答控制在 300 字以内，使用简洁的段落或小列表呈现信息，避免大段文字堆积。\n'
    '5. 非数据问题用自己的知识简短回答，控制在 200 字以内。\n'
    '6. 如果查询结果为空，明确告知用户"未查到相关数据"，并建议用户核实查询条件。'
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
            extra_body=STREAM_EXTRA,
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
                extra_body=STREAM_EXTRA,
            )
            content = resp2.choices[0].message.content or ""
        else:
            content = msg.content or ""
        return {"code": 200, "data": content}
    except Exception as e:
        logger.error(f"API call failed: {e}", exc_info=True)
        return {"code": 500, "msg": str(e)}


STREAM_EXTRA = {"enable_thinking": False}

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
            extra_body=STREAM_EXTRA,
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
                try: args = json.loads(fn_buffer)
                except json.JSONDecodeError:
                    try: args = json.loads(fn_buffer[:fn_buffer.rfind("}")+1])
                    except: args = {"sql": "SELECT 1 LIMIT 0"}
                if fn_name == "query_database":
                    result = query_database(args.get("sql", ""))
                else:
                    result = f"unknown tool: {fn_name}"
                messages.append({"role": "assistant", "content": full_content})
                messages.append(
                    {"role": "tool", "tool_call_id": "call_1", "content": result}
                )
                stream2 = c.chat.completions.create(
                    model=settings.qwen_model,
                    messages=messages,
                    temperature=0.7,
                    max_tokens=2048,
                    stream=True,
                    extra_body=STREAM_EXTRA,
                )
                for chunk2 in stream2:
                    delta2 = chunk2.choices[0].delta if chunk2.choices else None
                    if delta2 and delta2.content:
                        yield f"data: {delta2.content}\n\n"
                break
        yield "data: [DONE]\n\n"
    except Exception as e:
        logger.error(f"stream error: {e}", exc_info=True)
        yield f"data: {json.dumps({'error': str(e)})}\n\n"
        yield "data: [DONE]\n\n"
