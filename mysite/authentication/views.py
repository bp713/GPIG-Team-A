from django.shortcuts import  get_object_or_404, render
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from .models import Courier
from pywarp import RelyingPartyManager, Credential
from .demoBackend import MyDBBackend
import json
import base64

rp_id = "tg0.uk:49300"  # This must match the origin domain of your app, as seen by the browser.
rp = RelyingPartyManager("GPIG A", rp_id=rp_id, credential_storage_backend=MyDBBackend())

def get_registration_options(request):
    if "display_name" in request.GET:
        opts = rp.get_registration_options(email=request.GET.get('courier_email'), display_name=request.GET.get('display_name'))
    else:
        opts = rp.get_registration_options(email=request.GET.get('courier_email'))
    return HttpResponse(json.dumps(opts), content_type="application/json")

def b64decodeURL(coded):
    coded = coded.replace('_', '/')
    coded = coded.replace('-', '+')
    coded += '=' * (-len(coded) % 4)
    return base64.b64decode(coded)

@csrf_exempt #TODO: This should be removed and proper CSRFs used
def register(request):
    # result = rp.register(attestation_object=bytes, client_data_json=bytes, email="tg736@york.ac.uk")
    print(request.GET)
    print(request.POST.get('attestation_object'))
    attestation_object = b64decodeURL(request.POST.get('attestation_object'))
    client_data_json = b64decodeURL(request.POST.get('client_data_json'))
    result = rp.register(attestation_object=attestation_object, client_data_json=client_data_json, email=request.POST.get('courier_email'))
    return HttpResponse(json.dumps(result), content_type="application/json")

def get_authentication_options(request):
    opts = rp.get_authentication_options(email=request.GET.get('courier_email'))
    return HttpResponse(json.dumps(opts), content_type="application/json")


def authenticate(request):
    result = rp.verify(authenticator_data=bytes, client_data_json=bytes, signature=bytes, user_handle=bytes, raw_id=bytes, email=bytes)
    return HttpResponse(json.dumps(result), content_type="application/json")

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
