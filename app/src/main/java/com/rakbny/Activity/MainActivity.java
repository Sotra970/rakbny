/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.rakbny.Activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.RoadsApi;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.SpeedLimit;
import com.rakbny.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.LongSparseArray;
import android.util.Xml;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Roads API Demo App.
 *
 * Before you can start, you will need to obtain the relevant keys and add them to the api_keys.xml
 * file. The steps are detailed in the README file in the top level of this package.
 *
 * This app will load a map with 3 buttons. Press each of the buttons in sequence to demonstrate
 * various features of the Roads API and the supporting demo snippets.
 *
 * Find out more about the Roads API here: https://developers.google.com/maps/documentation/roads
 */
public class MainActivity extends AppCompatActivity {
    View school_bus ,school_container;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        school_bus = findViewById(R.id.school_bus) ;
        school_container = findViewById(R.id.schoolContaine) ;
        school_bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int[] deltaY = {(school_container.getMeasuredHeight() / 2) };

                ObjectAnimator translateY = ObjectAnimator.ofFloat(school_bus, "translationY", deltaY[0]);
                translateY.setDuration(1500);
                translateY.start();
                translateY.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        deltaY[0] = deltaY[0] + (int) (school_container.getMeasuredHeight() * 0.5) - school_bus.getMeasuredHeight()/2;
                        int deltax = (int) (school_container.getMeasuredWidth()) - school_container.getLeft();

                        ObjectAnimator translateY = ObjectAnimator.ofFloat(school_bus, "translationY", deltaY[0]);
                        translateY.setDuration(1300);
                        ObjectAnimator translatex = ObjectAnimator.ofFloat(school_bus, "translationX", deltax);
                        translatex.setDuration(2500);
                        ObjectAnimator rotate = ObjectAnimator.ofFloat(school_bus, "rotation", -90);
                        rotate.setDuration(1900);
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.playTogether(translatex,translateY,rotate);
                        animatorSet.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        });
    }
}

