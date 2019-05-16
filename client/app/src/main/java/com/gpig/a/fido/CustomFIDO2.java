package com.gpig.a.fido;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.Fido2ApiClient;
import com.google.android.gms.fido.fido2.Fido2PendingIntent;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor;
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.gpig.a.ui.login.LoginActivity;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CustomFIDO2 {

    public static final int REQUEST_CODE_REGISTER = 0;
    public static final int REQUEST_CODE_SIGN = 1;
    private final Activity activity;

    private final Fido2ApiClient mfido2ApiClient;
    public CustomFIDO2(Activity activity){
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

        builder.setRequestId(CustomFIDO2.REQUEST_CODE_SIGN);

        List<PublicKeyCredentialDescriptor> descriptors = new ArrayList<>();
        //TODO add FIDO2 keys
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
                                Log.e("com.gpig.a", "Error launching pending intent for sign request", e);
                            }
                        }
                    }
                });
    }
}
