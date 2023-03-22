import ultralytics
from ultralytics import YOLO
import numpy as np

def predict(model, image):
  names = {0: 'Apple', 1: 'Banana', 2: 'Burger',3: 'Chapati',4: 'Chicken Curry', 
         5: 'Fries', 6: 'Idli', 7: 'Pizza', 8: 'Rice', 9: 'Soda', 10: 'Tomato', 11: 'Vada'} #output classes
  # image = './img1.png' #input image
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
  return detected_classes
