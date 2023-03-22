from django.http import JsonResponse
from django.contrib.auth.models import User
from django.contrib.auth import authenticate, logout
from rest_framework.authentication import TokenAuthentication
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view, authentication_classes, permission_classes
from rest_framework.authtoken.models import Token
from django.contrib.auth.hashers import make_password


@api_view(['POST'])
def signup(request):
    print("\n\n\n")
    print(request.data)
    print("\n\n\n")
    username = request.data.get("username")
    password = request.data.get("password")
    cpassword = request.data.get("cpassword")
    fname = request.data.get("fname")
    lname = request.data.get("lname")
    email = request.data.get("email")

    if User.objects.filter(username=username):
        return JsonResponse({"message" : "User already exists"}, status = 400)
    
    if len(username) > 14:
        return JsonResponse({"message" : "Username over 14 characters"}, status=400)
    
    if password != cpassword:
        return JsonResponse({"message" : "Passwords do not match"}, status=400)
        

    myUser = User.objects.create(username=username, email=email, password=make_password(password))
    myUser.first_name = fname
    myUser.last_name = lname
    myUser.save()

    return JsonResponse({"message" : "Your account has been created."}, status=201)
    

@api_view(['POST'])
def signin(request):
    username = request.headers["username"]
    password = request.headers["password"]
    print("\n\n")
    print(username)
    print(password)
    print("\n\n")
    user = authenticate(username=username, password=password)
    if user is not None:
        token, _ = Token.objects.get_or_create(user=user)
        return Response({'token': token.key})
    else:
        return Response(status=status.HTTP_401_UNAUTHORIZED)
    

@api_view(['POST'])
def signout(request):
    print(request.headers)
    ctoken = request.headers.get("Token")
    print("\n\n")
    if ctoken != None:
        token = Token.objects.get(key=request.headers.get("Token"))
        token.delete()
    return Response(status=status.HTTP_204_NO_CONTENT)
    
