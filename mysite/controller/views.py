from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse, HttpResponseRedirect
from .models import Controller, Route, RouteComponent, Courier
from authentication.models import Courier as Auth_Courier
from django.views.decorators.csrf import csrf_exempt
from .forms import RouteForm
import os
from django.conf import settings
import django
import json
import time
import controller.Routetest as rt
from controller.assign_route import assign_route

def controller(request, controller_id):
    controller = get_object_or_404(Controller, pk=controller_id)
    if request.method == 'POST':
        form = RouteForm(request.POST)
        if form.is_valid():
            start_long = form.cleaned_data['start_long']
            start_lat = form.cleaned_data['start_lat']
            end_long = form.cleaned_data['end_long']
            end_lat = form.cleaned_data['end_lat']
            courier_id = form.cleaned_data['courier_id']
            point1 = '%s,%s' %(start_lat,start_long)
            point2 = '%s,%s' %(end_lat,end_long)
            assign_route(courier_id, point1, point2)
            return HttpResponseRedirect('couriers/')
    else:
        form = RouteForm()
    context = { 'controller' : controller, 'form': form}
    return render(request, 'controller/controller.html', context)

def route(request):
    json_file = open(os.path.join(settings.BASE_DIR, 'routejson.txt'))
    data = json.load(json_file)
    return HttpResponse(json.dumps(data), content_type="application/json")

def couriers(request, controller_id):
    controller = get_object_or_404(Controller, pk=controller_id)
    context = { 'controller' : controller}
    return render(request, 'controller/couriers.html', context)

def routeCalc(request):
    lang1 = 1
    long1 = 1
    point1 = '%s,%s' %(lang1,long1)

    lang2 = 1
    long2 = 1
    point2 = '%s,%s' %(lang1,long1)
    points = [point1, point2]

    rt.makeroute(rt.point, rt.key, rt.maxtraveltime)

@csrf_exempt #TODO: This should be removed and proper CSRFs used in the android app
def checkin(request, courier_id):
    courier = get_object_or_404(Courier, pk=courier_id)
    # if 'one_time_key' in request.POST:
    #     auth_courier = get_object_or_404(Auth_Courier,controller_model=courier)
    #     assert time.time() < int(auth_courier.one_time_key.split(',')[1])
    #     assert request.POST.get('one_time_key') == auth_courier.one_time_key
    #     auth_courier.one_time_key = ''
    #     auth_courier.save()
    route = courier.route
    if route.length == route.current:
        route.delete()
        courier.route_ready = False
        courier.save()
        return HttpResponse('no route')
    else:
        route.current += 1
        route.save()
        comp = RouteComponent.objects.filter(route=route, position=route.current)[0]
        return HttpResponse(json.dumps(comp.json), content_type="application/json")

def update(request, lattitude, longitude, courier_id):
    courier = get_object_or_404(Courier, pk=courier_id)
    courier.lattitude = lattitude
    courier.longitude = longitude
    courier.save()
    if courier.route_ready:
        route = courier.route
        if route.current >=0: # only respond true if the courier hasnt checked in yet
            return HttpResponse('False')
        old_comp = RouteComponent.objects.filter(route=route, position=0).delete()
        first_stage = RouteComponent.objects.filter(route=route, position=1)[0].json
        first_stage = json.loads(first_stage)
        first_point = first_stage['paths'][0]['points']['coordinates'][0]
        point1 = '%s,%s' %(lattitude,longitude)
        first_point_string = '%s,%s' %(first_point[1],first_point[0])
        first_comp_json = rt.makeroute([point1, first_point_string], rt.key, rt.maxtraveltime)
        comp = RouteComponent(route=route, position = 0)
        comp.save()
        return HttpResponse('True')
    else:
        return HttpResponse('False')
