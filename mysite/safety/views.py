from django.shortcuts import render, HttpResponse
import json

# Create your views here.
def safety(request, locations):
    locations = locations.split(',')
    jsonfile = open('safety\data\scores.json', 'r')
    data = json.load(jsonfile)
    out = {}
    total = 0 
    for location in locations:
        out[location] = data.get(location)
        total += data.get(location, 0)
    out['total'] = total
    return HttpResponse(json.dumps(out), content_type="application/json")
