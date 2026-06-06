import logging
from dashscope.audio.tts import SpeechSynthesizer, SpeechSynthesisResult
from config import settings
from fastapi.responses import Response

logger = logging.getLogger(__name__)

async def synthesize(text: str) -> Response:
    if not text or not text.strip():
        return Response(content=b"text is empty", status_code=400)
    if not settings.dashscope_api_key:
        return Response(content=b"key not set", status_code=500)
    try:
        result = SpeechSynthesizer().call(
            model=settings.tts_model,
            api_key=settings.dashscope_api_key,
            text=text.strip(),
            sample_rate=settings.tts_sample_rate,
            format=settings.tts_format,
        )
        audio = result.get_audio_data() if isinstance(result, SpeechSynthesisResult) else (result if isinstance(result, bytes) else bytes(result))
        return Response(content=audio, media_type="audio/wav",
            headers={"Content-Disposition": "inline; filename=\"tts.wav\""})
    except Exception as e:
        logger.error(f"TTS error: {e}")
        return Response(content=str(e).encode(), status_code=500)
