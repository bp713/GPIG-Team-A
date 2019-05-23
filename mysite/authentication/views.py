from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse
from .models import Courier
from pywarp import RelyingPartyManager, Credential
from .demoBackend import MyDBBackend
import json

rp_id = "tg0.uk"  # This must match the origin domain of your app, as seen by the browser.
rp = RelyingPartyManager("GPIG A", rp_id=rp_id, credential_storage_backend=MyDBBackend())

def get_registration_options(request):
    opts = rp.get_registration_options(email=request.GET.get('courier_email'))#custom display name support in github version of pywarp so not using it
    return HttpResponse(json.dumps(opts), content_type="application/json")

def register(request):
    return HttpResponse(json.dumps(data), content_type="application/json")

def get_authentication_options(request):
    pass

def authenticate(request):
    pass
