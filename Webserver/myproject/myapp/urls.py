from django.urls import path
from . import views, login_handler

urlpatterns = [
    path('myview/', views.my_view, name='my-view'),
    path('myimage/', views.predict_image, name='my-image'),
    path('getfoods/', views.get_foods, name='get-foods'),
    path('gethistory/', views.get_history),
    path('confirmentry/', views.confirm_request, name='confirm-entry'),
    path('login/', login_handler.signin),
    path('logout/', login_handler.signout),
    path('signup/', login_handler.signup),
]