from django.urls import path

from . import views

urlpatterns = [
    path('<int:controller_id>/', views.controller, name='controller'),
]
