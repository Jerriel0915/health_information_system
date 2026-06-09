import logging, tempfile, os, json
from fastapi import UploadFile
import httpx
from config import settings

logger = logging.getLogger(__name__)

# 把 ASR 请求转发到 Java 后端（8081），Java 的 OkHttp 能正常调 DashScope
JAVA_BACKEND_URL = "http://localhost:8081/algorithm/asr"


async def recognize(file: UploadFile) -> dict:
    if not file.filename:
        return {"code": 400, "msg": "upload audio file"}

    suffix = os.path.splitext(file.filename or "audio.wav")[1] or ".wav"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp.write(await file.read())
        tmp_path = tmp.name

    try:
        with open(tmp_path, "rb") as f:
            files = {"file": (os.path.basename(tmp_path), f, "audio/wav")}

            async with httpx.AsyncClient(timeout=120.0) as client:
                resp = await client.post(JAVA_BACKEND_URL, files=files)

            if resp.status_code == 200:
                result = resp.json()
                logger.info(f"Java backend ASR response: {result}")
                # Java 后端返回 AjaxResult 格式: {code: 200, data: ..., msg: ...}
                if result.get("code") == 200:
                    text = result.get("data", "")
                    return {"code": 200, "data": text or "no speech detected"}
                else:
                    return {"code": 500, "msg": result.get("msg", "unknown error")}
            else:
                body = resp.text[:300]
                logger.error(f"Java backend returned {resp.status_code}: {body}")
                return {"code": 500, "msg": f"backend error: {body}"}

    except Exception as e:
        logger.error(f"ASR forward error: {e}", exc_info=True)
        return {"code": 500, "msg": f"{type(e).__name__}: {e}"}
    finally:
        try:
            os.unlink(tmp_path)
        except:
            pass
