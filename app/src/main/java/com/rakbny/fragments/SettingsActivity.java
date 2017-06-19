package com.rakbny.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.rakbny.Activity.Get_my_location_activity;
import com.rakbny.Activity.LoginActivity;
import com.rakbny.Activity.Update_user;
import com.rakbny.R;
import com.rakbny.data.app.AppController;

public class SettingsActivity extends AppCompatActivity {
    View rootLayoutEmulat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rootLayoutEmulat = findViewById(R.id.em_rootLayout);
        if (savedInstanceState == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                findViewById(R.id.root_layout).setAlpha(0);
            }

            ViewTreeObserver viewTreeObserver = rootLayoutEmulat.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            rootLayoutEmulat.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            rootLayoutEmulat.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
           String type =AppController.getInstance().getPrefManager().getUser().getType();
            if (type.equals("parent") || type.equals("student")){
                findViewById(R.id.change_station_layout).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
           onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
            alphaAnimation.setDuration(300);
            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    findViewById(R.id.root_layout).setVisibility(View.GONE);
                    exit_reveal();

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            findViewById(R.id.root_layout).startAnimation(alphaAnimation);
        }else {
            supportFinishAfterTransition();
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exit_reveal() {
        Point point = (Point) getIntent().getExtras().get("point");
        Log.e("point" , "x" +point.x +"  y"+point.y);
        float finalRadius = Math.max(rootLayoutEmulat.getWidth(), rootLayoutEmulat.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayoutEmulat, point.x, point.y, finalRadius, 0);
        circularReveal.setDuration(500);
        circularReveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rootLayoutEmulat.setVisibility(View.GONE);
                supportFinishAfterTransition();
                overridePendingTransition(0,0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            supportFinishAfterTransition();
                overridePendingTransition(0,0);
            }
        });
        // make the view visible and start the animation

        circularReveal.start();
    }


    private void circularRevealActivity() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){

                Point point = (Point) getIntent().getExtras().get("point");
                Log.e("point" , "x" +point.x +"  y"+point.y);
                float finalRadius = Math.max(rootLayoutEmulat.getWidth(), rootLayoutEmulat.getHeight());

                // create the animator for this view (the start radius is zero)
                Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayoutEmulat, point.x, point.y, 0, finalRadius);
                circularReveal.setDuration(500);
                circularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        findViewById(R.id.root_layout).animate().alpha(1).setDuration(500);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        findViewById(R.id.root_layout).animate().alpha(1).setDuration(500);
                    }
                });
                // make the view visible and start the animation

                circularReveal.start();

            } else{

        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onPause() {
        super.onPause();
    }

    public void logout(View view) {
        AppController.getInstance().getPrefManager().clear();
        onBackPressed();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        ComponentName cn = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
        startActivity(mainIntent);
    }

    public void capture_location(View view) {

        startActivity(new Intent(getApplicationContext(), Get_my_location_activity.class));
        overridePendingTransition(R.anim.products_enter, R.anim.products_exit);
    }

    public void edit_profile(View view) {


        startActivity(new Intent(getApplicationContext(), Update_user.class));
        overridePendingTransition(R.anim.products_enter, R.anim.products_exit);
    }
}
