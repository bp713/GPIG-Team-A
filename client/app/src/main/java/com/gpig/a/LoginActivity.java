package com.gpig.a;

import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gpig.a.utils.BiometricCallback;
import com.gpig.a.utils.BiometricUtils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button b = findViewById(R.id.login_button);
        b.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button){
            if(!BiometricUtils.isSdkVersionSupported()){
                Toast.makeText(getApplicationContext(), "SDK Version not Supported", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isHardwareSupported(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "No Hardware Support", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isPermissionGranted(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "Permission is not Granted", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isFingerprintAvailable(getApplicationContext())){
                Toast.makeText(getApplicationContext(), "No Fingerprints Registered", Toast.LENGTH_LONG).show();
            }
            else if(!BiometricUtils.isBiometricPromptEnabled()){
                Toast.makeText(getApplicationContext(), "Biometric Prompt Disabled", Toast.LENGTH_LONG).show();
            }else {
                BiometricCallback bc = new BiometricCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        myIntent.putExtra("username", ((EditText)findViewById(R.id.username)).getText().toString());
                        startActivity(myIntent);
                        //TODO check login somehow
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String login_success = getString(R.string.login_success);
                                Toast.makeText(LoginActivity.this, login_success, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, final CharSequence helpString) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String welcome = getString(R.string.login_help) + helpString;
                                Toast.makeText(LoginActivity.this, welcome, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, final CharSequence errString) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String welcome = getString(R.string.login_error) + errString;
                                Toast.makeText(LoginActivity.this, welcome, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String welcome = getString(R.string.login_failed);
                                Toast.makeText(LoginActivity.this, welcome, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationCancelled() {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                String welcome = getString(R.string.login_cancelled);
                                Toast.makeText(LoginActivity.this, welcome, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                };
                BiometricUtils.displayBiometricPrompt(bc, getApplicationContext());
                return;
            }
            //TODO support other local login methods? see second half of
            // https://proandroiddev.com/5-steps-to-implement-biometric-authentication-in-android-dbeb825aeee8
        }
    }
}