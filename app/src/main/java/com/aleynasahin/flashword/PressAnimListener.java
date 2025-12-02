package com.aleynasahin.flashword;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class PressAnimListener implements View.OnTouchListener{

    private final Animation scaleUp;
    private final Animation scaleDown;

    // Constructor: context ile animasyonları yüklüyoruz
    public PressAnimListener(Context context) {
        scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.startAnimation(scaleUp);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                v.startAnimation(scaleDown);
                break;
        }
        return false; // buton click olayını da çalıştırmak için false
    }
}
