from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse
from .models import Courier
from pywarp import RelyingPartyManager, Credential
from .demoBackend import MyDBBackend
import json

rp_id = "tg0.uk:49300"  # This must match the origin domain of your app, as seen by the browser.
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

def assetlinks(request):
    data = [{
          "relation": ["delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds"],
          "target": {
            "namespace": "web",
            "site": "https://tg0.uk:49300"
          }
        }, {
          "relation": ["delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds"],
          "target": {
            "namespace": "android_app",
            "package_name": "com.gpig.a",
            "sha256_cert_fingerprints": ["2E:22:70:2C:26:9E:87:5D:E4:60:5E:A5:FC:85:90:CC:AC:C3:8A:85:E4:9D:67:96:2A:BB:CC:0D:EC:F1:4B:E8"]
          }
        }]
    return HttpResponse(json.dumps(data), content_type="application/json")
