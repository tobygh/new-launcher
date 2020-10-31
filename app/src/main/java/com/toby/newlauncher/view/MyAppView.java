package com.toby.newlauncher.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MyAppView extends LinearLayout {
    ImageView iv;
    Context ctx;
    MyAppView(Context _ctx){
        super(_ctx);
        ctx=_ctx;
        iv=new ImageView(ctx);
        iv.layout(0,0,0,10);
        //iv.setOnClickListener(new );
        //this.layout(0,0,0,10);

    }

}
