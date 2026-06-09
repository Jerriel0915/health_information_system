import os
import argparse
from ultralytics import YOLO
from ultralytics.data.dataset import ClassificationDataset
from ultralytics.models.yolo.classify import ClassificationTrainer, ClassificationValidator
import torch
import torchvision.transforms as T

class CustomizedDataset(ClassificationDataset):
    def __init__(self, root: str, args, augment: bool = False, prefix: str = ''):
        super().__init__(root, args, augment, prefix)
        train_transforms = T.Compose([
            T.Resize((args.imgsz, args.imgsz)),
            T.RandomHorizontalFlip(p=args.fliplr),
            T.RandomVerticalFlip(p=args.flipud),
            T.RandAugment(interpolation=T.InterpolationMode.BILINEAR),
            T.ColorJitter(brightness=args.hsv_v, contrast=args.hsv_v, saturation=args.hsv_s, hue=args.hsv_h),
            T.ToTensor(),
            T.Normalize(mean=torch.tensor(0), std=torch.tensor(1)),
            T.RandomErasing(p=args.erasing, inplace=True),
        ])
        val_transforms = T.Compose([
            T.Resize((args.imgsz, args.imgsz)),
            T.ToTensor(),
            T.Normalize(mean=torch.tensor(0), std=torch.tensor(1)),
        ])
        self.torch_transforms = train_transforms if augment else val_transforms

class CustomizedTrainer(ClassificationTrainer):
    def build_dataset(self, img_path: str, mode: str = 'train', batch=None):
        return CustomizedDataset(root=img_path, args=self.args, augment=mode == 'train', prefix=mode)

class CustomizedValidator(ClassificationValidator):
    def build_dataset(self, img_path: str):
        return CustomizedDataset(root=img_path, args=self.args, augment=False, prefix=self.args.split)

def train_classifier(data_dir: str, model_path: str = None, epochs: int = 100, imgsz: int = 224, batch: int = 16, lr0: float = 0.01, device: str = '0', project: str = 'runs/classify', name: str = 'exp'):
    if model_path and os.path.exists(model_path):
        model = YOLO(model_path)
        print(f'Loaded: {model_path}')
    else:
        local = os.path.join(os.path.dirname(__file__), '..', '..', '项目资料', 'yolo26n-cls.pt')
        if os.path.exists(local):
            model = YOLO(local)
            print(f'Loaded: {local}')
        else:
            model = YOLO('yolo26n-cls.pt')
            print('Loaded: yolo26n-cls.pt')
    results = model.train(data=data_dir, epochs=epochs, imgsz=imgsz, batch=batch, lr0=lr0, device=device, project=project, name=name, patience=20, save=True, trainer=CustomizedTrainer)
    print(f'Done. Best model: {results.save_dir}/weights/best.pt')
    return results

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Train YOLO26 classifier')
    parser.add_argument('--data', type=str, required=True, help='Dataset dir')
    parser.add_argument('--model', type=str, default=None)
    parser.add_argument('--epochs', type=int, default=100)
    parser.add_argument('--imgsz', type=int, default=224)
    parser.add_argument('--batch', type=int, default=16)
    parser.add_argument('--lr', type=float, default=0.01)
    parser.add_argument('--device', type=str, default='0')
    parser.add_argument('--name', type=str, default='exp')
    args = parser.parse_args()
    train_classifier(data_dir=args.data, model_path=args.model, epochs=args.epochs, imgsz=args.imgsz, batch=args.batch, lr0=args.lr, device=args.device, name=args.name)
