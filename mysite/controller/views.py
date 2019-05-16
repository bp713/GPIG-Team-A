from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse
from .models import Controller

def controller(request, controller_id):
    controller = get_object_or_404(Controller, pk=controller_id)
    context = { 'controller' : controller}
    return render(request, 'controller/controller.html', context)


