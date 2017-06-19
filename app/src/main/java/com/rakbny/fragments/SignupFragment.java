package com.rakbny.fragments;


import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rakbny.R;
import com.rakbny.data.Models.UserModel;
import com.rakbny.data.app.AppController;
import com.rakbny.data.app.Config;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {
    private String TAG = SignupFragment.class.getSimpleName();
    private EditText inputName, inputPass,inputMatchPass,inputPhone,inputSerial,inputEmail;
    private TextInputLayout inputLayoutName,inputLayoutPhone, inputLayoutPass,inputLayoutMatchPass , inputLayoutSerial,inputLayoutEmail;
    private Button btn_register;
    private View layout_resource ;
    String request = Config.SIGNUP;
    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if (layout_resource == null){

            layout_resource =  inflater.inflate(R.layout.fragment_signup, container, false);

        }

        inputLayoutName = (TextInputLayout) layout_resource.findViewById(R.id.input_layout_name);
        inputLayoutPhone = (TextInputLayout) layout_resource.findViewById(R.id.input_layout_phone);
        inputLayoutPass = (TextInputLayout) layout_resource.findViewById(R.id.input_layout_pass);
        inputLayoutSerial = (TextInputLayout) layout_resource.findViewById(R.id.input_layout_serial);
        inputLayoutMatchPass = (TextInputLayout) layout_resource.findViewById(R.id.input_layout_confirm_pass);
        inputLayoutEmail = (TextInputLayout) layout_resource.findViewById(R.id.input_layout_email);

        inputName = (EditText) layout_resource.findViewById(R.id.input_name);
        inputPass = (EditText) layout_resource.findViewById(R.id.input_pass);
        inputPhone = (EditText) layout_resource.findViewById(R.id.input_phone);
        inputSerial = (EditText) layout_resource.findViewById(R.id.input_serial);
        inputEmail = (EditText) layout_resource.findViewById(R.id.input_email);
        inputMatchPass = (EditText) layout_resource.findViewById(R.id.input_confirm_pass);
        btn_register = (Button) layout_resource.findViewById(R.id.btn_register);

        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputPass.addTextChangedListener(new MyTextWatcher(inputPass));
        inputPhone.addTextChangedListener(new MyTextWatcher(inputPhone));
        inputSerial.addTextChangedListener(new MyTextWatcher(inputSerial));
        inputMatchPass.addTextChangedListener(new MyTextWatcher(inputMatchPass));
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();

            }
        });

        try{
            if (getArguments().getString("opp").equals("edit")){
                UserModel current = AppController.getInstance().getPrefManager().getUser();
                inputName.setText(current.getName());
                inputPhone.setText(current.getPhone());
                inputEmail.setText(current.getEmail());
                inputSerial.setText("update");
                inputLayoutSerial.setVisibility(View.GONE);
                request= Config.UPDATE_USER ;
                btn_register.setText("update");

            }
        }catch (Exception e){
            Log.e("no signup arguments", e.toString() );
        }
        return layout_resource ;

    }

    private void register() {
        if (!validateName()) {
            return;
        }
        if (!validatePhone()) {
            return;
        }

        if (!validatePass()) {
            return;
        }
        if (!validateMatchPass()) {
            return;
        }
        if (!validateSerial()) {
            return;
        }
        layout_resource.findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        final String name = inputName.getText().toString();
        final String pass = inputPass.getText().toString();
        final String phone = inputPhone.getText().toString();
        final String email = inputEmail.getText().toString();
        final String serial = inputSerial.getText().toString();
        StringRequest strReq = new StringRequest(Request.Method.POST,
                Config.OPERATION, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                layout_resource.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                Log.e(TAG, "REGISTER response: " + response);

                try {
                    JSONObject obj = new JSONObject(response);
                    String res =   obj.getString("operation");
                    if (res.equals("done") ){
                            //GO TO LOGIN
                        getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        String message ;
                        if (request == Config.SIGNUP){
                             message = "Sigh up success" ;
                        }else {
                            UserModel current = AppController.getInstance().getPrefManager().getUser();
                            current.setName(name);
                            current.setPhone(phone);
                            current.setEmail(email);
                            AppController.getInstance().getPrefManager().storeUser(current);
                            getActivity().onBackPressed();
                            message = "update success";
                        }

                        Toast.makeText(getContext(),message , Toast.LENGTH_LONG).show();

                    }else if(res.equals("not_executed")) {
                        String message =   obj.getString("message");
                        Toast.makeText(getContext(), "" + message, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "json parsing error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError){
                   // register();
                }
                if (error instanceof NoConnectionError) {
                    layout_resource.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    String message = getString(R.string.NoConnection);
                    Toast.makeText(getContext(), "" + message, Toast.LENGTH_LONG).show();
                }
                NetworkResponse networkResponse = error.networkResponse;
                Log.e(TAG, "Volley error: " + error.getMessage() + ", code: " + networkResponse);
            }
        }) {
            //sending your email and pass
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("request", request);
                params.put("name", name);
                params.put("pass", pass);
                params.put("phone", phone);
                params.put("email", email);
                if (request == Config.SIGNUP){
                    params.put("serial", serial);
                }else {
                    params.put("user_id", AppController.getInstance().getPrefManager().getUser().getId());
                    if (AppController.getInstance().getPrefManager().getUser().getPhone().equals(phone)){
                        params.put("check","0");
                    }else {
                        params.put("check","1");
                    }
                }


                Log.e(TAG, "params: " + params.toString());
                return params;
            }
        };
        int socketTimeout = 5000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                12,
                1);

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
                case R.id.input_phone:
                    validatePhone();
                    break;
                case R.id.input_pass:
                    validatePass();
                    break;
                case R.id.input_confirm_pass:
                    validateMatchPass();
                    break;
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_serial:
                    validateSerial();
                    break;
            }
        }


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
 // Validating serial
    private boolean validateSerial() {
        if (inputSerial.getText().toString().trim().isEmpty()) {
            incorrectSerial();
            return false;
        } else {
            inputLayoutSerial.setErrorEnabled(false);
        }

        return true;
    }


    private void incorrectSerial() {
        inputLayoutName.setError(getString(R.string.Enter_valid_serial));
        requestFocus(inputSerial);
    }


    // Validating email
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email) ) {
            inputLayoutEmail.setError("Enter valid Email");
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }
    private static boolean isValidEmail(String email) {
        //not empty and vaild with email patternex@ex.ex
        return  android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    // Validating phone
    private boolean validatePhone() {
        if (inputPhone.getText().toString().trim().isEmpty()) {
            incorrectPhone();
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }

        return true;
    }

    private void incorrectPhone() {
        inputLayoutPhone.setError(getString(R.string.Enter_valid_PHONE));
        requestFocus(inputPhone);
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

    // Validating Match Pass
    private boolean validateMatchPass() {
        String Pass = inputPass.getText().toString().trim();
        String PassMatch = inputMatchPass.getText().toString().trim();

        if (PassMatch.isEmpty() || !(Pass.equals(PassMatch))) {
            incorrectMatchPass();
            return false;
        } else {
            inputLayoutMatchPass.setErrorEnabled(false);
        }

        return true;
    }

    private void incorrectMatchPass() {
        inputLayoutMatchPass.setError(getString(R.string.Enter_Match_PASS));
        requestFocus(inputMatchPass);
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


}
