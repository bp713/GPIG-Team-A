package com.gpig.a.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AttestationConveyancePreference;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.gpig.a.PollServer;
import com.gpig.a.settings.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public final class FIDO2Utils {

    public static final int REQUEST_CODE_REGISTER = 0;
    public static final int REQUEST_CODE_SIGN = 1;
    private static final String TAG = "FIDO2";
    private final Activity activity;

    private final Map<String, String> sessionIds = new HashMap<>();

    private final Fido2ApiClient mfido2ApiClient;

    public FIDO2Utils(Activity activity) {
        mfido2ApiClient = Fido.getFido2ApiClient(activity);
        this.activity = activity;
    }

    public void sign(String email) {

        AsyncTask<String, String, String> serverTask = ServerUtils.getFromServer("authentication/getAuthenticationOptions/?courier_email=" + email);
        JSONObject options;
        String serverOptions = "";
        try {
            serverOptions = serverTask.get();
            options = new JSONObject(serverOptions);
            PublicKeyCredentialRequestOptions.Builder builder =
                    new PublicKeyCredentialRequestOptions.Builder();

            builder.setChallenge(Base64.decode(options.getString("challenge"), Base64.DEFAULT));
            builder.setTimeoutSeconds((double)options.getInt("timeout"));
            builder.setRpId(options.getString("rpId"));

            builder.setRequestId(FIDO2Utils.REQUEST_CODE_SIGN);
            List<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();
            JSONArray allowedKeys = options.getJSONArray("allowCredentials");
            for (int i =0; i<allowedKeys.length(); i++) {
                JSONObject key = allowedKeys.getJSONObject(i);
                String allowedKey = key.getString("id");
//                sessionIds.put(allowedKey, sessionId);
                PublicKeyCredentialDescriptor publicKeyCredentialDescriptor =
                        new PublicKeyCredentialDescriptor(
                                PublicKeyCredentialType.PUBLIC_KEY.toString(),
                                Base64.decode(allowedKey, Base64.DEFAULT),
                                /* transports= */ null);
                descriptors.add(publicKeyCredentialDescriptor);
            }
            builder.setAllowList(descriptors);


            PublicKeyCredentialRequestOptions pko = builder.build();
            sendSignRequestToClient(pko);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(activity.getApplicationContext(), "Failed: " + serverOptions, Toast.LENGTH_LONG).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    private void sendSignRequestToClient(PublicKeyCredentialRequestOptions options) {
        Task<Fido2PendingIntent> result = mfido2ApiClient.getSignIntent(options);
        Log.i(TAG, "sendSignRequestToClient: ");

        result.addOnSuccessListener(
                new OnSuccessListener<Fido2PendingIntent>() {
                    @Override
                    public void onSuccess(Fido2PendingIntent fido2PendingIntent) {
                        if (fido2PendingIntent.hasPendingIntent()) {
                            try {
                                fido2PendingIntent.launchPendingIntent(activity, REQUEST_CODE_SIGN);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(TAG, "Error launching pending intent for sign request", e);
                            }
                        }
                    }
                });
    }

    public static void sendVerifyCompleteToClient(AuthenticatorAssertionResponse response, String email, Activity activity) {
        try {
            String data = "authenticator_data=" + Base64.encodeToString(response.getAuthenticatorData(), Base64.URL_SAFE);
            data += "&client_data_json=" + new String(response.getClientDataJSON(), StandardCharsets.UTF_8);
            data += "&signature=" + Base64.encodeToString(response.getSignature(), Base64.URL_SAFE);
            data += "&courier_email=" + URLEncoder.encode(email, "UTF-8");
            if(StatusUtils.canCheckIn(activity)){
                Location location = StatusUtils.getLastKnownLocation(activity, true);
                if(location != null)
                    data += "&check_in=" + location.getLatitude() + "," + location.getLongitude();
            }
            AsyncTask<String, String, String> task = ServerUtils.postToServer("authentication/authenticate/", data);
            String result = task.get();
            JSONObject options = new JSONObject(result);
            if(options.getBoolean("verified")) {
                Settings.SessionKey = options.getString("session_key");
                Settings.userID = options.getString("user_id");
                Settings.writeToFile(activity);
//                ServerUtils.pollServer = new PollServer();
                ServerUtils.pollServer.setAlarm(activity.getApplicationContext());
                if(StatusUtils.canCheckIn(activity) || StatusUtils.hasNewRoute(activity)) {
                    data = "one_time_key=" + options.getString("one_time_key");
                    Location location = StatusUtils.getLastKnownLocation(activity, true);
                    assert location != null;
                    AsyncTask<String, String, String> updateTask = ServerUtils.getFromServer("controller/update/" + location.getLatitude() + "/" + location.getLongitude() + "/" + Settings.userID + "/");
                    updateTask.get();//make sure the server has current location
                    data += "&check_in=" + location.getLatitude() + "," + location.getLongitude();
                    task = ServerUtils.postToServer("controller/checkin/" + Settings.userID + "/", data);
                    String json = task.get();
                    PollServer.areUpdatesAvailable = false;
                    if (RouteUtils.hasRouteChanged(activity, RouteUtils.routeFilename, json)) {
                        FileUtils.writeToInternalStorage(activity, RouteUtils.routeFilename, json);
                    }
                }
            }else{
                Toast.makeText(activity.getApplicationContext(), "Verification Failed! Is your email correct?", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(String email) {

        AsyncTask<String, String, String> serverTask = ServerUtils.getFromServer("authentication/getRegistrationOptions/?courier_email=" + email);
        JSONObject options;
        String serverOptions = "";
        try {
            serverOptions = serverTask.get();
            options = new JSONObject(serverOptions);
            PublicKeyCredentialCreationOptions.Builder builder =
                    new PublicKeyCredentialCreationOptions.Builder();
            // Parse challenge
            builder.setChallenge(Base64.decode(options.getString("challenge"), Base64.DEFAULT));

            // Parse RP
            JSONObject rpObj = options.getJSONObject("rp");
            String rpId = rpObj.getString("id");
            String rpName = rpObj.getString("name");
            String rpIcon = null;
            PublicKeyCredentialRpEntity entity = new PublicKeyCredentialRpEntity(rpId, rpName, rpIcon);
            builder.setRp(entity);

            // Parse user
            JSONObject userObj = options.getJSONObject("user");
            String displayName = userObj.getString("displayName");//this is not implemented on server so returns dummy value
            PublicKeyCredentialUserEntity userEntity =
                    new PublicKeyCredentialUserEntity(
                            Base64.decode(userObj.getString("id"), Base64.DEFAULT),
                            userObj.getString("name"),
                            userObj.getString("icon"),
                            displayName);
            builder.setUser(userEntity);

            // Parse parameters
            JSONArray pubKeyCredParams = options.getJSONArray("pubKeyCredParams");
            List<PublicKeyCredentialParameters> parameters = new ArrayList<>();
            for (int i = 0; i < pubKeyCredParams.length(); i++) {
                JSONObject pubKeyCredParam = pubKeyCredParams.getJSONObject(i);
                PublicKeyCredentialParameters parameter =
                        new PublicKeyCredentialParameters(pubKeyCredParam.getString("type"), pubKeyCredParam.getInt("alg"));
                parameters.add(parameter);
            }
            builder.setParameters(parameters);

            // Parse timeout
            builder.setTimeoutSeconds((double) (options.getInt("timeout")));

            // Parse exclude list
            JSONArray excludedKeys = options.getJSONArray("excludeCredentials");
            List<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();//TODO check this with a functioning server (don't think ours supports it)
            for (int i = 0; i < excludedKeys.length(); i++) {
                PublicKeyCredentialDescriptor publicKeyCredentialDescriptor = new PublicKeyCredentialDescriptor(
                        PublicKeyCredentialType.PUBLIC_KEY.toString(),
                        Base64.decode(excludedKeys.getString(i), Base64.URL_SAFE),
                        /* transports= */ null);
                descriptors.add(publicKeyCredentialDescriptor);
            }
            builder.setExcludeList(descriptors);

            AuthenticatorSelectionCriteria.Builder criteria =
                    new AuthenticatorSelectionCriteria.Builder();
            if (options.has("attachment")) {
                criteria.setAttachment(
                        Attachment.fromString(options.getString("attachment")));
            }
            builder.setAuthenticatorSelection(criteria.build());

            builder.setAttestationConveyancePreference(AttestationConveyancePreference.DIRECT); // TODO parameterise this?

//            AuthenticationExtensions.Builder extensions = new AuthenticationExtensions.Builder(); //TODO extension loc:true is hardcoded into server why? do we need it?
//            extensions.setFido2Extension(new FidoAppIdExtension("loc"));
//            builder.setAuthenticationExtensions(extensions.build());
            PublicKeyCredentialCreationOptions pko = builder.build();
            sendRegisterRequestToClient(pko);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Attachment.UnsupportedAttachmentException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void sendRegisterRequestToClient(PublicKeyCredentialCreationOptions options) {
        Task<Fido2PendingIntent> result = mfido2ApiClient.getRegisterIntent(options);
        result.addOnSuccessListener(
                new OnSuccessListener<Fido2PendingIntent>() {
                    @Override
                    public void onSuccess(Fido2PendingIntent fido2PendingIntent) {
                        if (fido2PendingIntent.hasPendingIntent()) {
                            try {
                                fido2PendingIntent.launchPendingIntent(
                                        activity, REQUEST_CODE_REGISTER);
                                Log.i(TAG, "Register request is sent out");
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(TAG, "Error launching pending intent for register request", e);
                            }
                        } else {
                            Log.i(TAG, "Activity cant launch intent!");
                        }
                    }
                });
    }

    public static void sendRegisterCompleteToClient(AuthenticatorAttestationResponse response, String email) {
        try {
            String data = "attestation_object=" + Base64.encodeToString(response.getAttestationObject(), Base64.URL_SAFE);
            data += "&client_data_json=" + new String(response.getClientDataJSON(), StandardCharsets.UTF_8);//Base64.encodeToString(response.getClientDataJSON(), Base64.URL_SAFE);
            data += "&courier_email=" + URLEncoder.encode(email, "UTF-8");
            ServerUtils.postToServer("authentication/register/", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data, String email, Activity activity) {
        switch (resultCode) {
            case RESULT_OK:
                if (data.hasExtra(Fido.FIDO2_KEY_ERROR_EXTRA)) {
                    Log.d(TAG, "Received error response from Google Play Services FIDO2 API");
                    AuthenticatorErrorResponse response =
                            AuthenticatorErrorResponse.deserializeFromBytes(
                                    data.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA));
                    Toast.makeText(
                            activity, "Operation failed\n" + response.getErrorMessage(), Toast.LENGTH_SHORT)
                            .show();
                    Log.d(TAG, "Received error: " + response.getErrorMessage());
                    Log.d(TAG, "Received error: " + response.getErrorCode());
                    Log.d(TAG, "Received error: " + response.getErrorCodeAsInt());
                } else if (requestCode == FIDO2Utils.REQUEST_CODE_REGISTER) {
                    Log.d(TAG, "Received register response from Google Play Services FIDO2 API");
                    AuthenticatorAttestationResponse response =
                            AuthenticatorAttestationResponse.deserializeFromBytes(
                                    data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA));
                    FIDO2Utils.sendRegisterCompleteToClient(response, email);
                } else if (requestCode == FIDO2Utils.REQUEST_CODE_SIGN) {
                    Log.d(TAG, "Received sign response from Google Play Services FIDO2 API");
                    AuthenticatorAssertionResponse response =
                            AuthenticatorAssertionResponse.deserializeFromBytes(
                                    data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA));
                    FIDO2Utils.sendVerifyCompleteToClient(response, email, activity);
                }
                break;

            case RESULT_CANCELED:
                Toast.makeText(activity, "Operation is cancelled", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(
                        activity,
                        "Operation failed, with resultCode " + resultCode,
                        Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }

}
