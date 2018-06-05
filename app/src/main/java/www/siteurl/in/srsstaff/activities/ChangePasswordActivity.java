package www.siteurl.in.srsstaff.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import www.siteurl.in.srsstaff.R;
import www.siteurl.in.srsstaff.api.Constants;

public class ChangePasswordActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private TextInputLayout mOldPassword, mNewPassword, mConfirmPassword;
    private RelativeLayout mRootLayout;
    String sessionId, userId;
    SharedPreferences loginPref;
    SharedPreferences.Editor editor;
    private Toolbar mToolbar;
    Dialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        alertDialog = new Dialog(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //initializing views
        mOldPassword = findViewById(R.id.srsOldPassword);
        mNewPassword = findViewById(R.id.srsNewPassword);
        mConfirmPassword = findViewById(R.id.srsConfirmPassword);
        mRootLayout = findViewById(R.id.srsChangePassword);

        loginPref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionId = loginPref.getString("loginSid", null);
        userId = loginPref.getString("loginUserId", null);
        editor = loginPref.edit();

        //initializing toolbar
        mToolbar = findViewById(R.id.srsChangePasswordToolbar);
        mToolbar.setTitle("Change Password");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        alertDialog = new Dialog(this);

        checkInternetConnection();
    }

    //validating change password credentials
    public void validateSrsStaffPassword(View view) {

        if (TextUtils.isEmpty(mOldPassword.getEditText().getText().toString().trim())) {
            mOldPassword.setError("Old Password");
            return;
        }

        if (TextUtils.isEmpty(mNewPassword.getEditText().getText().toString().trim())) {
            mNewPassword.setError("New Password");
            return;
        }

        if (TextUtils.isEmpty(mConfirmPassword.getEditText().getText().toString().trim())) {
            mConfirmPassword.setError("Confirm Password");
            return;
        }

        if (mOldPassword.getEditText().getText().toString().length() < 5) {
            mOldPassword.setError("Password should be minimum 5 characters");
            Snackbar.make(mRootLayout, "Password should be minimum 5 characters", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (mNewPassword.getEditText().getText().toString().length() < 5) {
            mNewPassword.setError("Password should be minimum 5 characters");
            Snackbar.make(mRootLayout, "Password should be minimum 5 characters", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (mConfirmPassword.getEditText().getText().toString().length() < 5) {
            mConfirmPassword.setError("Password should be minimum 5 characters");
            Snackbar.make(mRootLayout, "Password should be minimum 5 characters", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        if (!mNewPassword.getEditText().getText().toString().equals(mConfirmPassword.getEditText().getText().toString())) {
            mNewPassword.setError("Password Didn't Match");
            mConfirmPassword.setError("Password Didn't Match");
            Snackbar.make(mRootLayout, "Password Didn't Match", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        // checkInternetConnection();
        changeSrsStaffPassword(mOldPassword.getEditText().getText().toString().trim(), mNewPassword.getEditText().getText().toString().trim(), mConfirmPassword.getEditText().getText().toString().trim());

    }


    //To check internet connection
    private void checkInternetConnection() {
        boolean isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    // Showing the status in Snackbar
    private void showSnack(boolean isConnected) {
        if (isConnected) {
            //  changeSrsStaffPassword(mOldPassword.getEditText().getText().toString().trim(), mNewPassword.getEditText().getText().toString().trim(), mConfirmPassword.getEditText().getText().toString().trim());
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        } else {
            showNoInternetDialog();
        }
    }

    //To show no internet dialog
    private void showNoInternetDialog() {

        alertDialog.setContentView(R.layout.check_internet);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(true);
        Button retry = alertDialog.findViewById(R.id.tryAgain);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                //checkConnection();
                System.exit(0);
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SRSStaffApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }

    //this is the method for change staff password in server
    private void changeSrsStaffPassword(final String oldPassword, final String newPassword, final String confirmPassword) {

        final AlertDialog loadingDialog = new SpotsDialog(ChangePasswordActivity.this, R.style.Loading);
        loadingDialog.show();

        StringRequest changepasswordrequest = new StringRequest(Request.Method.POST, Constants.changePassword, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                loadingDialog.dismiss();
                try {
                    JSONObject responsefromserver = new JSONObject(response);
                    String error = responsefromserver.getString("Error");
                    String message = responsefromserver.getString("Message");

                    showAlertDialog(error, message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingDialog.dismiss();
                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    Toast.makeText(ChangePasswordActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(ChangePasswordActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(ChangePasswordActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(ChangePasswordActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(ChangePasswordActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(ChangePasswordActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", userId);
                params.put("sid", sessionId);
                params.put("api_key", Constants.APIKEY);
                params.put("current_password", oldPassword);
                params.put("confirm_password", confirmPassword);
                params.put("new_password", newPassword);
                return params;
            }
        };
        changepasswordrequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SRSSingleton.getInstance(getApplicationContext()).addtorequestqueue(changepasswordrequest);
    }

    //To show alert of change password response
    private void showAlertDialog(final String error, String message) {
        android.support.v7.app.AlertDialog.Builder errorbuilder = new android.support.v7.app.AlertDialog.Builder(ChangePasswordActivity.this);
        errorbuilder.setIcon(R.mipmap.ic_launcher);
        errorbuilder.setTitle("Service Request System");
        errorbuilder.setMessage(message);
        errorbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (error.equals("true")) {
                    dialogInterface.dismiss();
                } else {
                    dialogInterface.dismiss();
                    startActivity(new Intent(ChangePasswordActivity.this, StaffLogin.class).
                            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    //editor.clear();
                    // editor.commit();
                    finish();
                }
            }
        });
        errorbuilder.setCancelable(false);
        errorbuilder.show();
    }

    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(ChangePasswordActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(ChangePasswordActivity.this);
            loginErrorBuilder.setTitle("Error");
            loginErrorBuilder.setMessage(message);
            loginErrorBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            loginErrorBuilder.show();
        } catch (JSONException e) {
        } catch (UnsupportedEncodingException errorr) {
        }
    }
}


