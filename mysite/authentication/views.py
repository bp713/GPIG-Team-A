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
    print(request.POST.get('client_data_json'))
    attestation_object = b64decodeURL(request.POST.get('attestation_object'))
    client_data_json = request.POST.get('client_data_json')
    result = rp.customRegister(attestation_object=attestation_object, client_data_json=client_data_json, email=request.POST.get('courier_email').encode('utf-8'))
    return HttpResponse(json.dumps(result), content_type="application/json")

def get_authentication_options(request):
    opts = rp.get_authentication_options(email=request.GET.get('courier_email'))
    return HttpResponse(json.dumps(opts), content_type="application/json")


def authenticate(request):
    result = rp.customVerify(authenticator_data=bytes, client_data_json=bytes, signature=bytes, user_handle=bytes, raw_id=bytes, email=bytes)
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

def customVerify(authenticator_data, client_data_json, signature, user_handle, raw_id, email):
    "Ascertain the validity of credentials supplied by the client user agent via navigator.credentials.get()"
    email = email.decode()
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        raise Exception("Invalid email address")
    client_data_hash = hashlib.sha256(client_data_json.encode('utf-8')).digest()
    client_data = json.loads(client_data_json)
    assert client_data["type"] == "webauthn.get"
    expect_challenge = rp.storage_backend.get_challenge_for_user(email=email, type="authentication")
    assert b64url_decode(client_data["challenge"]) == expect_challenge
    print("expect RP ID:", rp.rp_id)
    if rp.rp_id:
        assert "https://" + rp.rp_id == client_data["origin"]
    # Verify that the value of C.origin matches the Relying Party's origin.
    # Verify that the RP ID hash in authData is indeed the SHA-256 hash of the RP ID expected by the RP.
    authenticator_data = AuthenticatorData(authenticator_data)
    assert authenticator_data.user_present
    credential = rp.storage_backend.get_credential_by_email(email)
    credential.verify(signature, authenticator_data.raw_auth_data + client_data_hash)
    # signature counter check
    return {"verified": True}

def customRegister(client_data_json, attestation_object, email):
    "Store the credential public key and related metadata on the server using the associated storage backend"
    authenticator_attestation_response = cbor2.loads(attestation_object)
    email = email.decode()
    if not re.match(r"[^@]+@[^@]+\.[^@]+", email):
        raise Exception("Invalid email address")
    client_data_hash = hashlib.sha256(client_data_json.encode('utf-8')).digest()
    client_data = json.loads(client_data_json)
    assert client_data["type"] == "webauthn.create"
    print("client data", client_data)
    expect_challenge = rp.storage_backend.get_challenge_for_user(email=email, type="registration")
    assert b64url_decode(client_data["challenge"]) == expect_challenge
    print("expect RP ID:", rp.rp_id)
    if rp.rp_id:
        assert "https://" + rp.rp_id == client_data["origin"]
    # Verify that the value of C.origin matches the Relying Party's origin.
    # Verify that the RP ID hash in authData is indeed the SHA-256 hash of the RP ID expected by the RP.
    authenticator_data = AuthenticatorData(authenticator_attestation_response["authData"])
    assert authenticator_data.user_present
    # If user verification is required for this registration,
    # verify that the User Verified bit of the flags in authData is set.
    assert authenticator_attestation_response["fmt"] == "fido-u2f"
    att_stmt = FIDOU2FAttestationStatement(authenticator_attestation_response['attStmt'])
    attestation = att_stmt.validate(authenticator_data,
                                    rp_id_hash=authenticator_data.rp_id_hash,
                                    client_data_hash=client_data_hash)
    credential = attestation.credential
    # TODO: ascertain user identity here
    rp.storage_backend.save_credential_for_user(email=email, credential=credential)
    return {"registered": True}
