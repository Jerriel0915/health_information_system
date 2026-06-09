import os
import logging
from ultralytics import YOLO

logger = logging.getLogger(__name__)

MODEL_PATH = os.path.join(os.path.dirname(__file__), '..', '..', '项目资料', 'yolo26n-cls.pt')
_model = None

def get_model() -> YOLO:
    global _model
    if _model is None:
        path = MODEL_PATH
        if not os.path.exists(path):
            path = 'yolo26n-cls.pt'
        _model = YOLO(path)
        logger.info(f'YOLO26 model loaded: {path}')
    return _model

def classify(image_bytes: bytes, filename: str = 'image.jpg') -> dict:
    import tempfile
    model = get_model()
    suffix = os.path.splitext(filename)[1] or '.jpg'
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
        tmp.write(image_bytes)
        tmp_path = tmp.name
    try:
        results = model(tmp_path, verbose=False)
        if not results or not results[0].probs:
            return {'category': 'unknown', 'confidence': 0, 'description': 'no result'}
        probs = results[0].probs
        top1_idx = int(probs.top1)
        top1_conf = float(probs.top1conf)
        top1_name = results[0].names[top1_idx] if results[0].names else f'class_{top1_idx}'
        return {
            'category': top1_name,
            'confidence': round(top1_conf * 100, 1),
            'description': f'Predicted: {top1_name}, confidence {round(top1_conf * 100, 1)}%',
        }
    except Exception as e:
        logger.error(f'Classify error: {e}')
        return {'category': 'error', 'confidence': 0, 'description': str(e)}
    finally:
        try: os.unlink(tmp_path)
        except: pass
