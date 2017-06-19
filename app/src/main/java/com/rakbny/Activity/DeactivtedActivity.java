package com.rakbny.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.rakbny.R;

public class DeactivtedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deactivted);
    }

    public void go_to_contact_us(View view) {
        startActivity(new Intent(getApplicationContext(),Contact_Us.class));
        finish();
    }
}
