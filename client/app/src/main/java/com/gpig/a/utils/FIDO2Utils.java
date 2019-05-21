package com.gpig.a.utils;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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

        builder.setRpId("com.gpig.a");

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

    public void register(){
        //TODO get options off server
        PublicKeyCredentialCreationOptions.Builder builder =
                new PublicKeyCredentialCreationOptions.Builder();
        // Parse challenge
        builder.setChallenge("".getBytes());

        // Parse RP
        String rpId = "";
        String rpName = "";
        String rpIcon = null;
        PublicKeyCredentialRpEntity entity = new PublicKeyCredentialRpEntity(rpId, rpName, rpIcon);
        builder.setRp(entity);

        // Parse user
        String displayName = "";//TODO use server values
        PublicKeyCredentialUserEntity userEntity =
                new PublicKeyCredentialUserEntity(
                        displayName.getBytes() /* id */,//TODO use server values
                        displayName /* name */,//TODO use server values
                        null /* icon */,//TODO use server values
                        displayName);
        builder.setUser(userEntity);

        // Parse parameters
        List<PublicKeyCredentialParameters> parameters = new ArrayList<>();
        PublicKeyCredentialParameters parameter =
                new PublicKeyCredentialParameters("public-key", -7);//TODO use server values
        parameters.add(parameter);
        builder.setParameters(parameters);

        // Parse timeout
        builder.setTimeoutSeconds(60000d);//TODO use server values

        // Parse exclude list
        List<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();
//        descriptors.add(
//                new PublicKeyCredentialDescriptor(
//                                                PublicKeyCredentialType.PUBLIC_KEY.toString(),//TODO use server pub keys
//                                                Base64.decode(k, Base64.URL_SAFE),//TODO use server pub keys
//                                                /* transports= */ null));
        builder.setExcludeList(descriptors);

//        AuthenticatorSelectionCriteria.Builder criteria =
//                new AuthenticatorSelectionCriteria.Builder();
//        if (registerRequestJson.has(KEY_ATTACHMENT)) {
//            criteria.setAttachment(
//                    Attachment.fromString(registerRequestJson.getString(KEY_ATTACHMENT)));
//        }
//        builder.setAuthenticatorSelection(criteria.build());
//        builder.setAttestationConveyancePreference(); //TODO from server
//        builder.setAuthenticationExtensions() //TODO from server
        PublicKeyCredentialCreationOptions pko = builder.build();
        sendRegisterRequestToClient(pko);
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
                        }
                    }
                });
}

}
