package com.toby.newlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.toby.newlauncher.AllController;

import java.io.IOException;

public class MyScrollView extends ScrollView {
    public
    boolean dontDraw=false;
    public AllController ctr;
    public MyScrollView(Context context){
        super(context);
    }
    public MyScrollView(Context context, AttributeSet atts){
        super(context,atts);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*if(needUpdate) {
            ctr.update();
        }
        needUpdate=false;*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        //
        if(!dontDraw) {
            super.onWindowFocusChanged(hasWindowFocus);
        }
        //needUpdate=false;
    }
}
