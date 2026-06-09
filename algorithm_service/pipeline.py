import logging, tempfile, os
from fastapi import UploadFile
from fastapi.responses import Response
from dashscope.audio.asr import Recognition
from dashscope.audio.tts import SpeechSynthesizer, SpeechSynthesisResult
from config import settings
from chat import chat
import urllib.parse

logger = logging.getLogger(__name__)

async def pipeline(file: UploadFile) -> Response:
    if not file.filename:
        return Response(content=b"upload audio file", status_code=400)
    if not settings.dashscope_api_key:
        return Response(content=b"key not set", status_code=500)
    suffix = os.path.splitext(file.filename or "audio.wav")[1] or ".wav"
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp.write(await file.read()); tmp_path = tmp.name
    try:
        logger.info("Pipeline step 1: ASR")
        asr_result = Recognition().call(model=settings.asr_model, api_key=settings.dashscope_api_key, file_path=tmp_path)
        text = asr_result.output.get('text', '') if hasattr(asr_result, 'output') and asr_result.output else (asr_result if isinstance(asr_result, str) else "")
        logger.info(f"ASR done: {text}")
    except Exception as e:
        logger.error(f"ASR failed: {e}")
        return Response(content=str(e).encode(), status_code=500)
    finally:
        try: os.unlink(tmp_path)
        except: pass
    if not text.strip(): text = "no speech"
    logger.info("Pipeline step 2: LLM")
    llm_res = chat(f"analyze and answer based on: {text}")
    answer = llm_res.get("data", "") if llm_res.get("code") == 200 else f"error: {llm_res.get('msg','')}"
    logger.info(f"LLM done: {len(answer)} chars")
    logger.info("Pipeline step 3: TTS")
    try:
        tts_result = SpeechSynthesizer().call(model=settings.tts_model, api_key=settings.dashscope_api_key, text=answer.strip(), sample_rate=settings.tts_sample_rate, format="wav")
        audio = tts_result.get_audio_data() if isinstance(tts_result, SpeechSynthesisResult) else (tts_result if isinstance(tts_result, bytes) else bytes(tts_result))
        return Response(content=audio, media_type="audio/wav", headers={"X-Asr-Text": urllib.parse.quote(text), "X-Answer-Text": urllib.parse.quote(answer)})
    except Exception as e:
        logger.error(f"TTS failed: {e}")
        return Response(content=str(e).encode(), status_code=500)
