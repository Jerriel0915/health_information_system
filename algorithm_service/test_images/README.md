# 图像分类测试说明

## 文件夹说明
- 	est_images/ — 放你要测试的图片
- 	est_images/results/ — 推理结果输出目录

## 选图要求
图片内容不限，你想测什么就放什么。YOLO26n-cls 是在 ImageNet 上预训练的，支持 1000 个常见类别（动物、物品、场景等），所以可以放各种日常图片看看效果。

### 图片格式要求
- 格式：jpg、jpeg、png、mp
- 分辨率：不限（程序会自动缩放到 224x224）
- 大小：单张不超过 10MB
- 命名：可以是中文，但建议用英文或拼音，避免路径问题
- 数量：建议先放 1-3 张测试

### 命名建议
`
xray_test.jpg
ct_scan.png
hospital_room.jpg
device_ultrasound.jpg
`

## 使用方法
把图片放到 test_images 文件夹后告诉我，我来运行推理。
