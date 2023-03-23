
# Intel OneAPI Hackathon: Food Detection and Nutrition Details Mobile Application



A mobile application with token authentication that allows the user to take pictures of their meal. This image is then sent to a server which passes the image through a YOLO V8 model that detects the food items present. The nutrition information of detected food is then retrieved from the MongoDB database and sent back to the user. From here, the user can see the nutritional values of their meal. This data is then stored in a MongoDB collection for future reference. On the homepage of the app, the user can view recent meals and take pictures of new meals.


## Authors

- Prashant Srinivasan Sarkar
- Richie Suresh Koshy
- Joel Mathew
- Caleb Stephen
- Gibin K Jayan
- Ruth Moly Benjamin


## Contents

- [Deep Learning Models](#Deep-Learning-Models)
- [Model 1 (YOLOv8)](#Model-1-(YOLOv8))
- [Dataset](#Dataset)
- [Data Preparation](#Data-Preparation)
- [Data Storage and Organization](#Data-Storage-and-Organization)
- [Code for training the model](#Code-for-training-the-model)
- [Model 1 Evaluation](#Model-1-Evaluation)
- [Sample Predictions - Real World Data](#Sample-Predictions---Real-World-Data)
- [Model 2 (YOLOv5)](#Model-2-(YOLOv5))
- [Initial Setup](#Initial-setup)
- [Training the model](#Training-the-model)
- [Making a Prediction using the model](#Making-a-Prediction-using-the-model)
- [Deploying from Roboflow](#Deploying-from-Roboflow)
- [Inspector Diet - The Application](#Inspector-Diet---The-Application)
- [Django Webserver](#Django-Webserver)
- [The Android Application](#The-Android-Application)
- [How to use the server](#How-to-use-the-Server?)
- [How to use the android application](#How-to-use-the-Android-Application?)
- [Demo](#Demo)
- [Acknowledgements](#Acknowledgements)




## Deep Learning Models

To detect the food items from the pictures, a deep learning model needed to be trained. The selected model was the Ultralytics YOLO (You Only Look Once) model.

To develop this application, 2 seperate models were trained on 2 different versions of yolo (YOLOv5 and YOLOv8). These models were then compared to determine which was the most suitable to be deployed on the application.
### Model 1 (YOLOv8)

This model was trained using [YOLOv8](https://github.com/ultralytics/ultralytics). It was trained on the google colab platform and aided by [Intel oneAPI distribution for python](https://www.intel.com/content/www/us/en/developer/tools/oneapi/distribution-for-python.html).

### Dataset

Kaggle: [Food Detection](https://www.kaggle.com/datasets/prashantsarkar/food-detection)

This dataset was prepared by us and uploaded to Kaggle for public use. We prepared a dataset using images of various popular food items in India. The dataset contains 358 images of each food item isolated as well as 33 sample use cases in which the foods are interspersed with each other as well as other foods not in the dataset. The dataset usability rating is 7.50

#### Sample Images
Chapatti

![Sample Image](https://i.imgur.com/QtLEsHw.jpeg)

Pizza

![Sample Image](https://i.imgur.com/vasWL3j.jpeg)

Soda

![Sample Image](https://i.imgur.com/WojINJc.png)

Idli

![Sample Image](https://i.imgur.com/cQ4RRTt.jpeg)

Idli and Vada

![Sample Image](https://i.imgur.com/JbCewEv.jpeg)

Burger, Pizza and Fries

![Sample Image](https://i.imgur.com/ouriA6l.jpeghttps://i.imgur.com/QtLEsHw.jpeg)

## Data Preparation

The Data Was prepared using Roboflow and exported for YOLOv8. You can find the Roboflow link [here](https://universe.roboflow.com/intel-hackathon/food-detection-pgfas).

#### Sample annotation:
![Annotation](https://i.imgur.com/w6zwU5V.jpeg)

![Annotation](https://i.imgur.com/qfcruON.jpeg)

![Annotation](https://i.imgur.com/YNWONvM.jpeg)

![Annotation](https://i.imgur.com/QuZnwRY.jpeg)

#### Preprocessing:
- Auto Orient
- Resize: Stretch to 640x640

#### Augmentation:
- Outputs per training example: 3
- Flip: Horizontal
- Cutout: 3 boxes with 15% each
- Mosaic

#### Final Dataset Size

- Train: 777
- Validation: 74
- Test: 37

#### Augmented data

![Sample](https://i.imgur.com/Soe2ZOA.jpeg)

![Sample](https://i.imgur.com/J2AeLCy.jpeg)

![Sample](https://i.imgur.com/gNVizPg.jpeg)

#### Data Storage and Organization
```
Dataset
|-->Test
|   |-->imagees
|   |-->labels
|
|-->Train
|   |-->images
|   |-->labels
|
|-->Valid
|   |-->images
|   |-->labels
|
|-->data.yaml
|
Sample_Use_Cases
|-->Test
|   |-->imagees
|   |-->labels
|
|-->Train
|   |-->images
|   |-->labels
|
|-->Valid
|   |-->images
|   |-->labels
|
|-->data.yaml
```

The .yaml file contains the configuration of the dataset and holds key information regarding addresses and the labels

#### yaml file
```
train: ../train/images
val: ../valid/images
test: ../test/images

nc: 12
names: ['Apple', 'Banana', 'Burger', 'Chapati', 'Chicken Curry', 'Fries', 'Idli', 'Pizza', 'Rice', 'Soda', 'Tomato', 'Vada']

roboflow:
  workspace: intel-hackathon
  project: food-detection-pgfas
  version: 2
  license: CC BY 4.0
  url: https://universe.roboflow.com/intel-hackathon/food-detection-pgfas/dataset/2
```
### Code for training the model
The link to the following code within the github repository can be found [here](https://github.com/GenitalKenobi/Inspector-Diet/blob/main/YOLOv8.ipynb)

loading dependencies
```
!pip install ultralytics
!pip install scikit-learn-intelex
!pip install intel-numpy
!pip install intel-scipy
!pip install intel-tensorflow
```
import ultralytics and check software and hardware
```
import ultralytics
ultralytics.checks()
```
Import YOLO and patch sklearn with intel optimizations
```
from ultralytics import YOLO
from sklearnex import patch_sklearn
patch_sklearn()
```
Training and validating the model
```
model = YOLO('/content/yolov8n.pt')
results = model.train(data='/content/Dataset/data.yaml', epochs=200)  # train the model
results = model.val()  # evaluate model performance on the validation set
```
load best model
```
model = YOLO('/content/runs/detect/train/weights/best.pt')
```
Function to get class labels from the model
```
names = {0: 'Apple', 1: 'Banana', 2: 'Burger',3: 'Chapati',4: 'Chicken Curry', 
         5: 'Fries', 6: 'Idli', 7: 'Pizza', 8: 'Rice', 9: 'Soda', 10: 'Tomato', 11: 'Vada'}
#this is a dictionary that holds all the labels of classes. It can be found in the .yaml file


def get_class_label(labels):
  objects = [] #list for classes detected
  detected = {} #dictionary for unique classes detected
  for x in labels:
    detected[names[x]] = 1 #get label from names and add to detected
    objects.append(detected)
  return(objects)
```
Function to detect objects using the model from an image address
```
def get_prediction(path): #take image path and predict the class
  outputs = []  #list of predictions for each image
  results = model(path)  # predict on an image
  for result in results:
    boxes = result.boxes  # Boxes object for bbox outputs
  pred = [] #predictions for image
  for j in boxes:
    pred.append(round(j[0].cpu().numpy().boxes[0][5])) #take last value from tensor (class)
    outputs.append(pred)
  class_names = []
  for x in outputs:
    class_names.append(get_class_label(x)) #convert label index to name
  return list(class_names[0][0].keys())
```

Fine tuning the model
```
results = model.train(data='/content/Sample_Use_Cases/data.yaml', epochs=50)  # fine tune the model
results = model.val()  # evaluate model performance on the validation set
```
### Model 1 Evaluation

model summary
![Chart](https://i.imgur.com/ikOOT4O.jpeg)

![Chart](https://i.imgur.com/2QVgdWK.png)

![Chart](https://i.imgur.com/qXNOIgY.png)

Labels Correlogram![Chart](https://i.imgur.com/iTAfiXv.jpeg)

Image Labels![Chart](https://i.imgur.com/gg1yCLR.jpeg)

![Chart](https://i.imgur.com/yERmWyJ.png)

![Chart](https://i.imgur.com/FLLoYey.png)

![Chart](https://i.imgur.com/bfHTHE2.png)

Results![Chart](https://i.imgur.com/O70GnFL.png)

#### Sample outputs
![image](https://i.imgur.com/XPw9px9.jpeg)

![image](https://i.imgur.com/15V4vK0.jpeg)

![image](https://i.imgur.com/FNGhTll.jpeg)

![image](https://i.imgur.com/MM3liH0.jpeg)

## Sample Predictions - Real World Data


![](https://i.imgur.com/OfWvVa2.jpeg)

![](https://i.imgur.com/u8RYXaD.jpeg)

![](https://i.imgur.com/7Eh3RbT.jpeg)

#### The model demonstrates a capability to identify objects, however it still needs further training in order to be reliable.
### Model 2 (YOLOv5)

This model was trained using [YOLOv5](https://github.com/ultralytics/yolov5). The model was trained on a dataset containing 810 images that was developed on roboflow. The model takes in an input image and gives an output images demarcated with bounding boxes to signify the class of food present.

### Dataset
The dataset consists of food images pertaining to 12 classes namely:
- Apple
- Banana
- Burger
- Chapathi
- Chicken Gravy
- Idli
- Fries
- Rice
- Soda
- Tomato
- Vada
- Pizza

This [dataset](https://www.kaggle.com/datasets/calebstephen/food-images-and-labels-dataset-for-yolov5) is publicly available on Kaggle (usability score - 8.13) for the public's use. The dataset was created using roboflow. This dataset was prepared on Roboflow. You can find the roboflow link [here](https://universe.roboflow.com/intel-n2yjd/food-detector-acbog)

#### Data Preprocessing:
Auto-Orient:  Applied
Resize:  Stretch to 640x640
#### Data Augmentation
90° Rotate:  Clockwise, Counter-Clockwise
Saturation:  Between -16% and +16%
#### Final Dataset Size
Train: 704 images
Test: 41 images
Validate: 65 images
#### Yaml File:
names:
- Apple
- Chapati
- Chicken Gravy
- Fries
- Idli
- Pizza
- Rice
- Soda
- Tomato
- Vada
- banana
- burger
nc: 12
roboflow:
  license: CC BY 4.0
  project: food-detector-acbog
  url: https://universe.roboflow.com/intel-n2yjd/food-detector-acbog/dataset/4
  version: 4
  workspace: intel-n2yjd
test: ../test/images
train: /content/datasets/food-detector-4/train/images
val: /content/datasets/food-detector-4/valid/images

### Initial setup
The first step is to clone the github repository of YoloV5.
```  
!git clone https://github.com/ultralytics/yolov5
```
Once the cloning is complete, basic imports of libraries can be done.
```  
%cd yolov5
%pip install -r requirements.txt roboflow
import torch
import os
from IPython.display import Image, clear_output
```
The training of YoloV5 works best on a gpu. To check if a GPU is being used, run the following code:
```
print(f"Setup complete Using torch {torch.version}({torch.cuda.get_device_properties(0).name if torch.cuda.is_available()  else  'CPU'})")
```  
Import roboflow to get the dataset.
```
!pip install -q roboflow
from roboflow import Roboflow
```
Create a new directory to store the dataset.
```
os.environ["DATASET_DIRECTORY"] = "/content/datasets"
```
Get the dataset from roboflow using the code snippet provided by roboflow.
```
from roboflow import Roboflow
rf = Roboflow(api_key="RJvCd32Q84ptl2ONZj6z")
project = rf.workspace("intel-n2yjd").project("food-detector-acbog")
dataset = project.version(4).download("yolov5")
```
### Training the model
Training the model is the most vital part of the creation of the model. Since the image size is set to 640, the image size parameter of training should be set to the same. The number of batches and epochs are set here as well.
```
!python train.py --img 640 --batch 16 --epochs 100 --data {dataset.location}/data.yaml --weights yolov5s.pt --cache
```
The results of the training process are shown below:
![](https://i.imgur.com/wTmwiJc.png)
```
from utils.plots import plot_results
Image(filename='/content/yolov5/runs/train/exp/results.png', width=5000)
```
![](https://i.imgur.com/R0VbZxe.png)
Tensorboard is used to track the training process and give interactive graphs, charts and data. Run it after testing using the following code:
```
%load_ext tensorboard
%tensorboard --logdir runs
```
Some of the charts shown in tensorboard is as follows:
![](https://i.imgur.com/cAVoW9C.png)
![](https://i.imgur.com/qlNWuJU.png)
![](https://i.imgur.com/SSu4CFi.png)
![](https://i.imgur.com/pRYlC1d.png)

The testing of the dataset is done by running the following the code:
```
!python detect.py --weights runs/train/exp/weights/best.pt --img 640 --conf 0.1 --source {dataset.location}/test/images
```
Some sample output images after testing:
Test Image 1 :
image 1/41 /content/datasets/food-detector-4/test/images/10_jpg.rf.cb.jpg: 640x640 1 Chicken Gravy, 1 Rice, 12.7ms
![](https://i.imgur.com/M8S4r4B.png)
Test Image 2:
image 2/41 /content/datasets/food-detector-4/test/images/11_jpg.rf.f.jpg: 640x640 1 Chapathi, 12.7ms
![](https://i.imgur.com/czTNWAJ.png)

### Making a Prediction using the model

In order to make a prediction, create a new directory to store the new test pictures. after that run the following line of code.
The file 'download.jpg' is the image we wish to predict and its directory must be after --source.
```
!python detect.py --weights runs/train/exp/weights/best.pt --img 640 --conf 0.3 --source data/images/download.jpg
```
Results:
image 1/1 /content/yolov5/data/images/download.jpg: 480x640
1 burger, 13.3ms
![](https://i.imgur.com/4XfocZ4.png)

![](https://i.imgur.com/qXrxZy6.png)

### Deploying from Roboflow
If you have a model trained from roboflow, then in order to deploy it, the following lines of code would be sufficient.
```
from roboflow import Roboflow
rf = Roboflow(api_key="RJvCd32Q84ptl2ONZj6z")
project = rf.workspace().project("food-detector-acbog")
model = project.version(4).model

# infer on a local image
print(model.predict("your_image.jpg", confidence=40, overlap=30).json())

# visualize your prediction
# model.predict("your_image.jpg", confidence=40, overlap=30).save("prediction.jpg")
```
## Inspector Diet - The Application

The deployment side of this project involves two projects, the Django web-server and the Android Application.

## Django Webserver
The YOLO models mentioned above have been deployed on the Django webserver, which exposes its services as REST APIs to the user.

These REST APIs are used to perform authentication of the user, classify images and identify the nutritional values of the foods involved.

An advantage of this method is that the user need not use an Android device. The server can also be accessed by a webpage as well using the exposed APIs mentioned below.

Moreover, it removes all computational requirement on the user as we can built strong, robust models on the server and we need not have these be stored on edge devices.

The server accesses a MongoDB database where all the information about nutrition and users are stored. The authentication of the users happens in the Django Database.

## The Android Application
The Android application was built using Android Studio. This provides a user interface between the user and the application.

The end goal of this application is for the user to be able to take a photo of whatever they are eating and log them into the app to track calories and nutrition.

The purpose of the app is to enable the Nutrition tracking service and at the same time, make it easy for a non-technical user to interact with the model and use it real-time.

## How to use the Server?

First, download the django project. Open the project directory in any terminal. The project structure should look like the following

#### Data storage and organization
```
Webserver
|-->myproject
|   |-->myapp
|   |   |-->urls.py
|   |   |-->...
|   |-->myproject
|   |   |-->settings.py
|   |   |-->...
|   |-->manage.py
|   |-->...
|
|-->requirements.txt
|-->...
```

Locate the requirements.txt file which is in the root of this application(./Webserver) and run the following command.

```
pip install -r requirements.txt
```
This should resolve all dependencies required for the Django server to run.

If you are using a virtual environment, please make sure to create and activate the virtual environment beforehand. You can look up the following online resources for how to do this.

[Virtual Environment Help](https://www.geeksforgeeks.org/create-virtual-environment-using-venv-python/)

Go to the path
```  
cd ./myproject
python manage.py runserver http://<ip-address>:<port>
```
Substitue <ip-address> with whichever IP address you are hosting this service and substitue <port> with whichever port you want the application to listen to.

For example:
```
python manage-py runserver http://192.168.56.1:8000
```
Note: Make sure you have python set to PATH variables for smooth operation.

Now, if everything went fine, your server should be running and listening to requests on the specified ip address and the specified port.

Technically, now you can use either Postman or any other application to interact with this server and still get your needs met.

## How to use the Android Application?

As explained earlier, to make utilization of the service easier, we have developed an Android Application that uses the exposed server endpoint.

To use the Android Application, separate the Inspector Diet App from the other applications.

```
Open Android Studio -> Go to File -> Open... -> Select the App folder
```

This should open the application and you can build it.

As this Application is still in development, we have not yet included an apk for this. To do this, the server will have to be deployed on the internet with full access to anyone who needs it.

Now, to make the application work, go to the string.xml file which is located in 

```
./res/values/strings.xml
```

Change the localhost and the port to whichever you <ip-address> and <port> you mentioned above and this change will be reflected.

Finally, you can now run this model on any virtual device or connect to an actual device. When you are connecting to an actual physical device, it would be recommended NOT to use the localhost ip-address of 127.0.0.1. Instead, use the IP address of your laptop.

Furthermore, make sure both the laptop and the phone are connected on the same wifi network.

#### How to use the webserver as a standalone unit?

There are mainly 7 API requests that the user can make against the server. These are the list of exposed endpoints that the Android application uses.

These are:
- /signup/ - Create a new user.
- /login/ - Login to get an Authentication token to perform all other operations.
- /logout/ - Logout and terminate the Authentication token's validity.
- /myimage/ - Predicts the food contained within the image.
- /history/ - Gets recent history of meals saved by the user.
- /getfoods/ - Get a list of foods stored in the MongoDB Database.
- /confirmentry/ - Save a meal and its classes into Database.

Everything besides login requires the Token to be in the header for the application to return a valid response.
## Demo

You can see a youtube demonstration [here](https://youtu.be/RlXGoAlhHj8)

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/RlXGoAlhHj8/0.jpg)](https://www.youtube.com/watch?v=RlXGoAlhHj8)

## Acknowledgements

 - [Intel oneAPI](https://www.intel.com/content/www/us/en/developer/tools/oneapi/overview.html)
 - [Intel oneAPI distribution for python](https://www.intel.com/content/www/us/en/developer/tools/oneapi/distribution-for-python.html)
 - [Ultralytics YOLOv8](https://github.com/ultralytics/ultralytics)
 - [Ultralytics YOLOv5](https://github.com/ultralytics/yolov5)
 - [Roboflow](https://roboflow.com)
 - [Android Studio](https://developer.android.com/studio)
 - [Django Rest Framework](https://www.django-rest-framework.org)

