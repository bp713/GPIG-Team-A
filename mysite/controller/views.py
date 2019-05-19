from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse
from .models import Controller
import os
from django.conf import settings
import django
import json

def controller(request, controller_id):
    controller = get_object_or_404(Controller, pk=controller_id)
    context = { 'controller' : controller}
    return render(request, 'controller/controller.html', context)

def route(request):
    json_file = open(os.path.join(settings.BASE_DIR, 'routejson.txt'))
    data = json.load(json_file)
    return HttpResponse(json.dumps(data), content_type="application/json")