import logging, os, traceback

# ====== 在一切导入之前清除系统代理和修复 SSL 证书 ======
for _key in ["HTTP_PROXY", "HTTPS_PROXY", "http_proxy", "https_proxy"]:
    os.environ.pop(_key, None)
os.environ["NO_PROXY"] = "*"
os.environ["no_proxy"] = "*"

# 设置 certifi 证书，修复 Windows Schannel 证书损坏问题
import certifi
os.environ["SSL_CERT_FILE"] = certifi.where()
os.environ["REQUESTS_CA_BUNDLE"] = certifi.where()

from fastapi import FastAPI, UploadFile, File, Form, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse, Response
import uvicorn

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger(__name__)

app = FastAPI(title="Health AI Algorithm Service", version="1.0.0")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"])

@app.get("/health")
def health(): return {"status": "ok", "service": "algorithm-service"}

from services.chat import chat as chat_sync, chat_stream as chat_stream_gen

@app.post("/chat")
async def chat_route(question: str = Form(...), session_id: str = Form(default="")):
    if not question.strip(): return {"code": 400, "msg": "question is empty"}
    return chat_sync(question.strip(), session_id or None)

@app.get("/chat/stream")
async def chat_stream_route(question: str = Query(...), session_id: str = Query(default="")):
    if not question.strip():
        async def e(): yield "data: question is empty\n\n"; yield "data: [DONE]\n\n"
        return StreamingResponse(e(), media_type="text/event-stream")
    return StreamingResponse(chat_stream_gen(question.strip(), session_id or None), media_type="text/event-stream")

@app.post("/classify")
async def classify_route(file: UploadFile = File(...)):
    try:
        from services.classifier import classify
        image_bytes = await file.read()
        result = classify(image_bytes, file.filename or "image.jpg")
        return {
            "code": 200,
            "msg": "success",
            "data": result
        }
    except Exception as e:
        logger.error("Classify error: %s\n%s", e, traceback.format_exc())
        return {"code": 500, "msg": f"Classify failed: {e}", "data": None}

from services.detector import run_detection
from bone_classifier import predict as bone_predict

@app.post("/detect")
async def detect_route():
    return run_detection()

@app.post("/predict")
async def predict_route(file: UploadFile = File(...)):
    contents = await file.read()
    return bone_predict(contents)


if __name__ == "__main__":
    port = int(os.getenv("ALGORITHM_PORT", "5001"))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)






