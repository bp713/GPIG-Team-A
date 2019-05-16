package com.gpig.a.ui.login;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.fido.Fido;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAttestationResponse;
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse;
import com.gpig.a.R;
import com.gpig.a.fido.CustomFIDO2;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "com.gpig.a";
    private LoginViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        CustomFIDO2 fido2 = new CustomFIDO2(this);
        fido2.sign();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                if (data.hasExtra(Fido.FIDO2_KEY_ERROR_EXTRA)) {
                    Log.d(TAG, "Received error response from Google Play Services FIDO2 API");
                    AuthenticatorErrorResponse response =
                            AuthenticatorErrorResponse.deserializeFromBytes(
                                    data.getByteArrayExtra(Fido.FIDO2_KEY_ERROR_EXTRA));
                    Toast.makeText(
                            this, "Operation failed\n" + response, Toast.LENGTH_SHORT)
                            .show();
                } else if (requestCode == CustomFIDO2.REQUEST_CODE_REGISTER) {
                    Log.d(TAG, "Received register response from Google Play Services FIDO2 API");
                    AuthenticatorAttestationResponse response =
                            AuthenticatorAttestationResponse.deserializeFromBytes(
                                    data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA));
                    Toast.makeText(
                            this,
                            "Registration key handle:\n"
                                    + Base64.encodeToString(response.getKeyHandle(), Base64.DEFAULT),
                            Toast.LENGTH_SHORT)
                            .show();
                } else if (requestCode == CustomFIDO2.REQUEST_CODE_SIGN) {
                    Log.d(TAG, "Received sign response from Google Play Services FIDO2 API");
                    AuthenticatorAssertionResponse response =
                            AuthenticatorAssertionResponse.deserializeFromBytes(
                                    data.getByteArrayExtra(Fido.FIDO2_KEY_RESPONSE_EXTRA));
                    Toast.makeText(
                            this,
                            "Sign key handle:\n" + Base64.encodeToString(response.getKeyHandle(), Base64.DEFAULT),
                            Toast.LENGTH_SHORT)
                            .show();
//                    updateSignResponseToServer(response);
                }
                break;

            case RESULT_CANCELED:
                Toast.makeText(this, "Operation is cancelled", Toast.LENGTH_SHORT).show();
                break;

            default:
                Toast.makeText(
                        this,
                        "Operation failed, with resultCode " + resultCode,
                        Toast.LENGTH_SHORT)
                        .show();
                break;
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
