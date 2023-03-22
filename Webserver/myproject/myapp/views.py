import datetime
from django.shortcuts import render
import os

#Custom imports
from django.http import JsonResponse

#Imports 2
from django.shortcuts import render, get_object_or_404
from django.http import JsonResponse
from django.contrib.auth import authenticate, login, logout
import base64
from django.core.files.storage import default_storage
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.authentication import TokenAuthentication
from rest_framework.permissions import IsAuthenticated
from rest_framework.authtoken.models import Token

from . import ml_models, nutrition
from myapp.config import model

# Create your views here.
@api_view(['GET'])
@authentication_classes([TokenAuthentication])
@permission_classes([IsAuthenticated])
def my_view(request):
    if request.user.is_authenticated:
        user = request.user
        print("\n\n")
        print(user)
        print("\n\n")
        data = {'message': 'Hello, world!'}
        return JsonResponse(data)
    else:
        return JsonResponse({"message" : "Unauthenticated Access Attempt"})

@api_view(['POST'])
def predict_image(request):
    f=request.FILES['sentFile'] # here you get the files needed
    response = {}
    file_name = "pic.jpg"
    if os.path.exists(file_name):
        os.remove(file_name)
    default_storage.save(file_name, f,)
    foods = ml_models.predict(model, file_name)
    #print(foods)
    res = nutrition.get_nutrition(foods)
    units = nutrition.get_units()
    # print(units)
    # print(type(units))
    # print(dict(units))
    print(nutrition.get_nutrition_each(foods))
    print("\n\n")
    for k,v in res.items():
        res[k] = str(v) + " " + units[k]
    
    return JsonResponse({"classes" : foods, "results" : res})

@api_view(['GET'])
def get_foods(request):
    if request.data != None:
        exclude = request.data.get("exclude")
    if exclude != None:
        return JsonResponse({"foods" : nutrition.get_foods(exclude)})
    else:
        return JsonResponse({"foods" : nutrition.get_foods()})

@api_view(['GET'])
def get_history(request):
    token = request.headers.get('Token')
    print(token)
    if token == None:
        return JsonResponse({"status" : "Invalid token"})
    else:
        user = get_object_or_404(Token, pk=token).user
        hist = nutrition.get_user_history(user.username)
        l = []
        for food in hist["foods"]:
            l.append(nutrition.get_nutrition(food))
            print("Nutrition in views", l)
        # nutrition.get_calories_of_foods(hist)
        hist["nutrition"] = l
        return JsonResponse({"data" : hist})


@api_view(['POST'])
def confirm_request(request):
    data = request.data
    token = request.headers.get("Token")
    user = get_object_or_404(Token, pk = token).user
    rec = {
        "user" : user.username,
        "foods" : request.data.get("foods"),
        "effectiveTS" : str(datetime.datetime.now())
    }
    nutrition.add_to_user_db(rec)
    nutRes = nutrition.get_nutrition(request.data.get("foods"))
    return JsonResponse({"user" : user.username, "nutrition" : nutRes})