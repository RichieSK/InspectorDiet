import ultralytics
from ultralytics import YOLO
import numpy as np

path = '/content/best.pt'
model = YOLO(path)  # load a pretrained YOLOv8n detection model

#take image and predict the food

names = {0: 'Banana', 1: 'Burger', 2: 'Chapati', 3: 'Chicken Curry', 4: 'Idli', 5: 'Pizza', 6: 'Tomato', 7: 'Vada'} #output classes

image = '/img1.png' #input image
output = model(image) #detect in image
for results in output:
  boxes = results.boxes  # Boxes object for bbox outputs
pred = []
for j in boxes:
  pred.append(round(j[0].cpu().numpy().boxes[0][5])) #get class from tensor

detected = {}
for x in pred:
  detected[x] = 1
detected_classes = []
for x in detected:
  detected_classes.append(names[x])
print(detected_classes)