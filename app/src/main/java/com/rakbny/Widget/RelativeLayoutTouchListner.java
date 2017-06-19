package com.rakbny.Widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Sotraa on 4/15/2016.
 */
public class RelativeLayoutTouchListner extends RelativeLayout {
    public static final String TAG = "sotra";
public      int Tx;

    private   int Ty;

    public RelativeLayoutTouchListner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutTouchListner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RelativeLayoutTouchListner(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean b = super.dispatchTouchEvent(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "MyLayout dispatchTouchEvent DOWN");
                break;
            case MotionEvent.ACTION_MOVE:

                Log.d(TAG, "MyLayout dispatchTouchEvent MOVE");
                break;
            case MotionEvent.ACTION_UP:
                if (b) {
                    Tx= (int) ev.getX();
                    Ty= (int) ev.getY();

                    Log.d(TAG, "MyLayout dispatchTouchEvent UP" + b + ev.getX() + "  " + ev.getY() );
                    break;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "MyLayout dispatchTouchEvent CANCEL");
                break;
        }

        Log.d(TAG, "MyLayout dispatchTouchEvent RETURNS " + b);

        return b;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "MyLayout onInterceptTouchEvent DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "MyLayout onInterceptTouchEvent MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "MyLayout onInterceptTouchEvent UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "MyLayout onInterceptTouchEvent CANCEL");
                break;
        }
        boolean b = super.onInterceptTouchEvent(ev);
        Log.d(TAG, "MyLayout onInterceptTouchEvent RETURNS " + b);
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "MyLayout onTouchEvent DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "MyLayout onTouchEvent MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "MyLayout onTouchEvent UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "MyLayout onTouchEvent CANCEL");
                break;
        }
        boolean b = super.onTouchEvent(event);
        Log.d(TAG, "MyLayout onTouchEvent RETURNS " + b);
        return b;
    }

    public int getTouchedx() {
        return Tx;
    }

    public int getTouchedy() {
        return Ty;
    }
}
