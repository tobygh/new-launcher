package com.toby.newlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.fonts.Font;
import android.util.AttributeSet;

import com.toby.newlauncher.AllController;
import com.toby.newlauncher.R;

import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.Timer;
import java.util.TimerTask;



import androidx.annotation.Nullable;


public class MyDragbarView  extends androidx.appcompat.widget.AppCompatImageView{
    public AllController ctr;
    boolean isNeedInit=true;
    Paint paint;
    Handler theHd;
    Timer theTm;
    TimerTask tTask;
    String barText=getResources().getString(R.string.barText);
    int curGl,lastGl;
    final int barW=100;
    //PathMeasure pathMeasure=new PathMeasure();
    Path bezierPath;
    private class TextConf{
        float[]tx;
        float[]ty;
        float[]ts;
        TextConf(){
            tx=new float[nn];
            ty=new float[nn];
            ts=new float[nn];
        }
    }
    TextConf[] textConf;
    boolean needInit = true;
    public MyDragbarView(Context context){
        this(context,null);
    }
    public MyDragbarView(Context context, AttributeSet atts){
        this(context,atts,0);
    }
    public MyDragbarView(Context context,AttributeSet atts, int defStyleAttr){
        super(context,atts,defStyleAttr);
        theHd = new Handler();
        theTm = new Timer();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }
    float bw=0;
    int ww=0,hh=0,nn=1;
    float ca, baseX;

    private void interpolation(){
        float[] x=new float[nn*2+1];
        float[] y=new float[nn*2+1];
        int ci=nn;
        for(int i=0;i<nn*2+1;i++){
            int off=Math.abs(ci-i);
            y[i]=(i+0.5f)*bw;
            x[i]=baseX-120f/(1f+off*off*0.8f);
        }
        float[]k=new float[nn*2+1];
        k[0]=0;
        for(int i=1;i<2*nn;i++){
            k[i]=(x[i+1]-x[i-1])/2f/bw;
        }
        k[2*nn]=0;
        bezierPath=new Path();
        bezierPath.moveTo(x[0],y[0]);
        for(int i=0;i<2*nn;i++){
            bezierPath.cubicTo(x[i]+k[i]*bw*ca,y[i]+bw*ca,x[i+1]-k[i+1]*bw*ca,y[i+1]-bw*ca,x[i+1],y[i+1]);
        }
        bezierPath.lineTo(ww+50,y[2*nn]);
        bezierPath.lineTo(ww+50,y[0]);
    }


    public void init(){
        if(ww==getWidth()&&hh==getHeight())
            return;
        //if(!isNeedInit)

        ww=getWidth();
        hh=getHeight();
        nn=barText.length();
        Log.d("draw",1/ww+","+1/hh+","+1/nn);
        bw=1f*hh/nn;
        baseX=ww-barW;
        ca=0.5f;
        interpolation();
        curGl=-1;
        lastGl=-1;
        Rect hr=new Rect();
        textConf = new TextConf[nn+1];
        for(int i=0;i< nn;i++){
            textConf[i]=new TextConf();
            //when choosing i'th cluster
            int off;
            for(int j=0;j<nn;j++){
                off=Math.abs(j-i);
                textConf[i].ts[j]=50f+40f/(1+off*off*5);
                paint.setTextSize(textConf[i].ts[j]);
                paint.getTextBounds(barText,j,j+1,hr);
                //Log.d("text",textConf[i].ts[j]+","+hr.width());
                textConf[i].ty[j]=(j+0.5f)*bw+hr.height()/2f;
                //textConf[i].tx[j]= baseX+hr.width()/2f+30f-100f/(1f+off*off);
                textConf[i].tx[j]= baseX+(barW+hr.width())/2f-100f/(1f+off*off);
                }
        }
        textConf[nn]=new TextConf();
        for(int j=0;j<nn;j++){

            textConf[nn].ts[j]=50f;
            paint.setTextSize(textConf[nn].ts[j]);
            paint.getTextBounds(barText,j,j+1,hr);
            //Log.d("text",textConf[nn].ts[j]+","+hr.width());
            textConf[nn].ty[j]=(j+0.5f)*bw+hr.height()/2f;
            //textConf[nn].tx[j]= baseX+hr.width()/2f+30f;
            textConf[nn].tx[j]= baseX+(barW+hr.width())/2f;
        }
        isNeedInit=false;
    }
    @Override
    public boolean performClick(){
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action=event.getAction();
        if (ctr==null)return performClick();

        if(action==MotionEvent.ACTION_DOWN){
            ctr.beginDrag();
            //this.bringToFront();
        }

        Log.d("bar",event.getX()+","+event.getY());
        int blockNum=barText.length();
        float blockH=1f*getHeight()/blockNum,blockW=getWidth();
        curGl=(int)Math.floor(event.getY()/blockH);

        if (curGl>=blockNum)curGl=blockNum-1;
        else if (curGl<0)curGl=0;
        //if(curGl!=lastGl){
        invalidate();
        Log.i("debug","selected "+curGl);
        //}
        if(curGl!=lastGl)
        ctr.scrollTable(curGl,(curGl+0.5f)*blockH);
        lastGl=curGl;
        //ctr.endDrag();
        if (action==MotionEvent.ACTION_UP){
            curGl=-1;
            invalidate();
            ctr.endDrag();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas){
        Log.d("draw",ww+","+hh);
        init();
        //if (isNeedInit)init();


        /*java.lang.NullPointerException: Attempt to read from field 'android.graphics.Path com.toby.newlauncher.view.MyDragbarView$DrawingSet.bezierPath' on a null object reference*/
        //canvas.drawPath(ds[curGl].bezierPath,paint);
        int innerColor=Color.parseColor("#537D7170");
        int outerColor=Color.parseColor("#50888888");
        int borderWidth=3;
        if(curGl>-1){
            bezierPath.offset(0,(curGl-nn)*bw);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(innerColor);
            canvas.drawPath(bezierPath,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderWidth);
            paint.setColor(outerColor);
            canvas.drawPath(bezierPath,paint);
            bezierPath.offset(0,-(curGl-nn)*bw);
        }
        else{
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(innerColor);
            canvas.drawRect(baseX,-50,ww+50,hh+50,paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderWidth);
            paint.setColor(outerColor);
            canvas.drawRect(baseX,-50,ww+50,hh+50,paint);
        }
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.RIGHT);
        if(curGl>-1){
            for(int i=0;i<nn;i++){
                //int off=curGl>-1?Math.abs(curGl-i):nn;
                //float tx=baseX+40f-100f/(1f+off*off*0.8f);
                paint.setTextSize(textConf[curGl].ts[i]);
                canvas.drawText(barText.substring(i,i+1),textConf[curGl].tx[i],textConf[curGl].ty[i],paint);
            }
        }
        else{
            paint.setTextSize(50f);
            for(int i=0;i<nn;i++){
                //int off=curGl>-1?Math.abs(curGl-i):nn;
                //float tx=baseX+40f-100f/(1f+off*off*0.8f);
                //paint.setTextSize(textConf[curGl].ts[i]);
                canvas.drawText(barText.substring(i,i+1),textConf[nn].tx[i],textConf[nn].ty[i],paint);
            }
        }
        super.onDraw(canvas);
    }

}
