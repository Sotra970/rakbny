package com.rakbny.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rakbny.R;
import com.rakbny.fragments.SignupFragment;

public class Update_user extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SignupFragment signupFragment = new SignupFragment();
        Bundle bundle = new Bundle();
        bundle.putString("opp","edit");
        signupFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.update_fragment_container , signupFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.b_exit, R.anim.b_enter);
    }
}
