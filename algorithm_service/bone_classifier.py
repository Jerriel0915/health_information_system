import torch
import torch.nn as nn
import torchvision.models as models
import torchvision.transforms as transforms
from PIL import Image
import io
import os

# 获取当前目录
_MODEL_DIR = os.path.join(os.path.dirname(__file__), "model")
_MODEL_PATH = os.path.join(_MODEL_DIR, "bone_model.pth")

# 设备
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# 模型结构（必须和训练时一致）
class BoneClassifier(nn.Module):
    def __init__(self, num_classes=2):
        super(BoneClassifier, self).__init__()
        self.backbone = models.resnet50(weights=None)
        in_features = self.backbone.fc.in_features
        self.backbone.fc = nn.Sequential(
            nn.Dropout(0.3),
            nn.Linear(in_features, 256),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(256, num_classes)
        )

    def forward(self, x):
        return self.backbone(x)

# 加载模型
_model = None
def get_model():
    global _model
    if _model is None:
        _model = BoneClassifier(num_classes=2)
        state = torch.load(_MODEL_PATH, map_location=device, weights_only=True)
        _model.load_state_dict(state)
        _model.to(device)
        _model.eval()
    return _model

# 图像预处理
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

def predict(image_bytes: bytes) -> dict:
    """骨骼X光图像分类推理"""
    try:
        img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    except Exception:
        return {"code": 400, "msg": "无效的图像文件"}

    img_tensor = transform(img).unsqueeze(0).to(device)

    model = get_model()
    with torch.no_grad():
        outputs = model(img_tensor)
        probabilities = torch.softmax(outputs, dim=1)
        _, predicted = torch.max(outputs, 1)

    confidence = probabilities[0][predicted.item()].item()
    class_id = predicted.item()
    class_name = "正常" if class_id == 0 else "肺炎"

    return {
        "code": 200,
        "data": {
            "class_name": class_name,
            "class_id": class_id,
            "confidence": round(confidence, 4)
        }
    }
