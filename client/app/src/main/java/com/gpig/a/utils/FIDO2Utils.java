package com.gpig.a.utils;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.Attachment;
import com.google.android.gms.fido.fido2.api.common.AuthenticationExtensions;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria;
import com.google.android.gms.fido.fido2.api.common.FidoAppIdExtension;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialType;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.gpig.a.settings.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class FIDO2Utils {

    public static final int REQUEST_CODE_REGISTER = 0;
    public static final int REQUEST_CODE_SIGN = 1;
    private static final String TAG = "FIDO2";
    private final Activity activity;

    private final Map<String, String> sessionIds = new HashMap<>();

    private final Fido2ApiClient mfido2ApiClient;
    public FIDO2Utils(Activity activity){
        mfido2ApiClient = Fido.getFido2ApiClient(activity);
        this.activity = activity;
    }

    public void sign(){
        PublicKeyCredentialRequestOptions.Builder builder =
                new PublicKeyCredentialRequestOptions.Builder();

        builder.setRpId("");//TODO from server

        byte[] challenge = new byte[20];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                SecureRandom.getInstanceStrong().nextBytes(challenge);
            } catch (NoSuchAlgorithmException e) {
                SecureRandom random = new SecureRandom();
                random.nextBytes(challenge);
            }
        }else{
            SecureRandom random = new SecureRandom();
            random.nextBytes(challenge);
        }
        builder.setChallenge(challenge);

        builder.setRequestId(FIDO2Utils.REQUEST_CODE_SIGN);
        //TODO add FIDO2 keys
        List<String> allowedKeys = new ArrayList<>();
        List<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();
//        for (String allowedKey : allowedKeys) {
//            sessionIds.put(allowedKey, sessionId);
//            PublicKeyCredentialDescriptor publicKeyCredentialDescriptor =
//                    new PublicKeyCredentialDescriptor(
//                            PublicKeyCredentialType.PUBLIC_KEY.toString(),
//                            Base64.decode(allowedKey, Base64.URL_SAFE),
//                            /* transports= */ null);
//            descriptors.add(publicKeyCredentialDescriptor);
//        }
        builder.setAllowList(descriptors);
        //TODO add FIDO2 extensions?
//        builder.setAuthenticationExtensions();
        //TODO add FIDO2 timeout
//        builder.setTimeoutSeconds();
        //TODO add FIDO2 token binding?
//        builder.setTokenBinding();


        PublicKeyCredentialRequestOptions pko = builder.build();
        sendSignRequestToClient(pko);
    }


    private void sendSignRequestToClient(PublicKeyCredentialRequestOptions options) {
        Task<Fido2PendingIntent> result = mfido2ApiClient.getSignIntent(options);

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


    private static String getStringFromUrl(String serverUrl){
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            try {
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                in.close();
                reader.close();
            } finally {
                urlConnection.disconnect();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            Log.e(TAG, ex.toString());
        }

        return result.toString();
    }

    public void register(String email){
        String serverOptions = getStringFromUrl("http://" + Settings.ServerIP + ":" + Settings.ServerPort + "/authentication/getRegistrationOptions/?courier_email=" + email);
        JSONObject options;
        try {
            options = new JSONObject(serverOptions);
            PublicKeyCredentialCreationOptions.Builder builder =
                    new PublicKeyCredentialCreationOptions.Builder();
            // Parse challenge
            builder.setChallenge(Base64.decode(options.getString("challenge"), Base64.DEFAULT));

            // Parse RP
            JSONObject rpObj =  options.getJSONObject("rp");
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
            for(int i = 0; i < pubKeyCredParams.length(); i++) {
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
            for(int i = 0; i < excludedKeys.length(); i++) {
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

//            AuthenticationExtensions.Builder extensions = new AuthenticationExtensions.Builder(); //TODO extension loc:true is hardcoded into server why? do we need it?
//            extensions.setFido2Extension(new FidoAppIdExtension("loc"));
//            builder.setAuthenticationExtensions(extensions.build());
            PublicKeyCredentialCreationOptions pko = builder.build();
            sendRegisterRequestToClient(pko);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Attachment.UnsupportedAttachmentException e) {
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
                        }else{
                            Log.i(TAG, "Activity cant launch intent!");
                        }
                    }
                });
    }

}
