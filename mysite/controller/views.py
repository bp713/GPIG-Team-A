from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse, HttpResponseRedirect
from .models import Controller, Route, RouteComponent
from .forms import RouteForm
import os
from django.conf import settings
import django
import json
import controller.Routetest as rt

def controller(request, controller_id):
    controller = get_object_or_404(Controller, pk=controller_id)
    if request.method == 'POST':
        form = RouteForm(request.POST)
        if form.is_valid():
            
            return HttpResponseRedirect('/route/' + str(controller_id))
    else:
        form = RouteForm()
    context = { 'controller' : controller, 'form': form}
    return render(request, 'controller/controller.html', context)

def route(request):
    json_file = open(os.path.join(settings.BASE_DIR, 'routejson.txt'))
    data = json.load(json_file)
    return HttpResponse(json.dumps(data), content_type="application/json")


def routeCalc(request):
    lang1 = 1
    long1 = 1
    point1 = '%s,%s' %(lang1,long1)

    lang2 = 1
    long2 = 1
    point2 = '%s,%s' %(lang1,long1)
    points = [point1, point2]
    
    rt.makeroute(rt.point, rt.key, rt.maxtraveltime)

