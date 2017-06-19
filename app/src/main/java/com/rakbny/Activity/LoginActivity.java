package com.rakbny.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rakbny.R;
import com.rakbny.fragments.SignupFragment;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;
import com.rakbny.data.Models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private String TAG = LoginActivity.class.getSimpleName();
    private EditText inputName, inputPass;
    private TextInputLayout inputLayoutName, inputLayoutPass;
    private Button btnEnter , btnReg;
    private View layout_resource ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inputLayoutName = (TextInputLayout) findViewById(R.id.input_layout_name);
        inputLayoutPass = (TextInputLayout) findViewById(R.id.input_layout_pass);
        inputName = (EditText) findViewById(R.id.input_name);
        inputPass = (EditText) findViewById(R.id.input_pass);
        btnEnter = (Button) findViewById(R.id.btn_enter);
        btnReg = (Button) findViewById(R.id.btn_register);

        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputPass.addTextChangedListener(new MyTextWatcher(inputPass));

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();

            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputLayoutName.setErrorEnabled(false);
                inputLayoutPass.setErrorEnabled(false);
                replaceFragment(new SignupFragment());
            }
        });
    }
    private void login() {
        if (!validateName()) {
            return;
        }

        if (!validatePass()) {
            return;
        }
        findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final String name = inputName.getText().toString();
        final String pass = inputPass.getText().toString();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.OPERATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                String deactivate ;
                Log.e(TAG, "Login response: " + response);
                findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                try {
                    JSONObject obj = new JSONObject(response);
                    String res =   obj.getString("operation");
                    if (res.equals("done") ){
                        JSONObject userObj = new JSONObject(obj.getString("response"));

                        UserModel user = new UserModel();
                        user.setId(userObj.getString("user_id"));
                        user.setPhone(userObj.getString("phone"));
                        user.setName(userObj.getString("name"));
                        user.setEmail(userObj.getString("email"));
                        user.setToken(userObj.getString("token"));
                        user.setType(userObj.getString("type"));
                        deactivate = userObj.getString("deactivate") ;

                        try {

                            if (!userObj.getString("st_bus_id").equals("null")) {
                                user.setBus_id(userObj.getString("st_bus_id"));
                                user.setStation_id(userObj.getString("station_id"));
                                FirebaseMessaging.getInstance().subscribeToTopic("station"+user.getStation_id());
                                user.setLat(Double.valueOf(userObj.getString("lat")));
                                user.setLung(Double.valueOf(userObj.getString("lung")));

                            }

                            else
                                user.setBus_id(userObj.getString("ed_bus_id"));


                            FirebaseMessaging.getInstance().subscribeToTopic(user.getBus_id());
                            Log.e("subscribe to " ,user.getBus_id());
                        }catch (Exception e){
                            Log.e("not subscribe to " ,"bus");
                        }


                       // storing user in shared preferences
                        AppController.getInstance().getPrefManager().clear();
                       AppController.getInstance().getPrefManager().storeUser(user);
                        Intent home = null;
                        FirebaseMessaging.getInstance().subscribeToTopic("logout");
                        if (deactivate.equals("1")){
                            home = new Intent(getApplicationContext(),DeactivtedActivity.class);

                        }else {

                        switch (user.getType()){
                            case "admin" : {
                                home = new Intent(getApplicationContext(),AdminPanel.class);
                            }
                                break;
                            case "student" :
                            case "parent" : {
                                home = new Intent(getApplicationContext(),FollowerActivity.class);
                            }
                            break;
                            case "driver" :
                            case "supervisor" : {
                                home = new Intent(getApplicationContext(),Trace_Ride_Activity.class);
                            }
                            break;
                        }
                        }
                        startActivity(home);
                        finish();
                    }else if(res.equals("not_executed")) {
                        String message =   obj.getString("message");
                        if (message.equals("deactivated")) {
                            startActivity(new Intent(getApplicationContext(),DeactivtedActivity.class));
                            finish();
                        }else
                        Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    login();
                }
                if (error instanceof NoConnectionError) {
                    findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    String message =  getString(R.string.NoConnection);
                    Toast.makeText(getApplicationContext(), "" + message, Toast.LENGTH_LONG).show();
                }
            }
        }) {
            //sending your email and pass
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("request", Config.SIGNIN);
                params.put("name", name);
                params.put("pass", pass);

                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
        int socketTimeout = 10000; // 10 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                10,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        strReq.setRetryPolicy(policy);
        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq);
    }





    private class MyTextWatcher implements TextWatcher {

        private View view;
        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_name:
                    validateName();
                    break;
                case R.id.input_pass:
                    validatePass();
                    break;
            }
        }


    }

    private void clear(){
        inputPass.setText(null);
        inputName.setText(null);
        inputLayoutName.setErrorEnabled(false);
        inputLayoutPass.setErrorEnabled(false);
    }
    // Validating name
    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            incorrectUserName();
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
        }

        return true;
    }

    private void incorrectUserName() {
        inputLayoutName.setError(getString(R.string.Enter_valid_Username));
        requestFocus(inputName);
    }

    // Validating Pass
    private boolean validatePass() {
        String Pass = inputPass.getText().toString().trim();

        if (Pass.isEmpty()) {
            incorrectPass();
            return false;
        } else {
            inputLayoutPass.setErrorEnabled(false);
        }

        return true;
    }

    private void incorrectPass() {
        inputLayoutPass.setError(getString(R.string.Enter_valid_PASS));
        requestFocus(inputPass);
    }

    private void requestFocus(View view) {
        Log.d("requestFocus",view.requestFocus()+"");
        //foucus on view
        if (view.requestFocus()) {
            /*m7taga sho3'l
          im = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            Log.d("inputmetod", im.isAcceptingText() + "");
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
*/
        }
    }




    @Override
    public void onPause() {
        super.onPause();
        Log.e("Fragment Login" , "onPause");

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e("Fragment Login" , "onResume");
        clear();
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(LoginActivity.this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(LoginActivity.this, resultCode, Config.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {

                AlertDialog alertDialog = new AlertDialog.Builder(
                        LoginActivity.this).create();

                // Setting Dialog Title
                alertDialog.setTitle("Alert Dialog");

                // Setting Dialog Message
                alertDialog.setMessage("This device is not supported. Google Play Services not installed");



                // Setting OK Button
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog closed
                    }
                });

                // Showing Alert Message
                alertDialog.show();

                Log.i(TAG, "This device is not supported. Google Play Services not installed!");

            }
            return false;
        }
        return true;
    }
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentMAin ,fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        Log.e("fragmentCounts",getSupportFragmentManager().getBackStackEntryCount() + "");
    }

}
