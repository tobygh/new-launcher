package com.toby.newlauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;


import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.toby.newlauncher.view.MyDragbarView;
import com.toby.newlauncher.view.MyScrollView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.constraintlayout.widget.Guideline;

public class MainActivity extends Activity {
    Handler theHd;
    Timer theTm;
    MyDragbarView drView;
    Guideline btry;
    TextView tm;
    Set<String> pkg2del;
    MyScrollView scr;
    LinearLayout table;
    Calendar cal;
    ImageView bg,extraMenu;
    View menuView;
    //Spinner menuSpin;
    //a del from launcher
    public void launcherDel(){

    }

    AllController ctr;
    String fav_file;//=getString(R.string.fav_file);
    final String[] week=new String[]{"未知","星期日","星期一","星期二","星期三","星期四","星期五","星期六"};

    private class ApkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent){
            //Log.i("debug",intent.getAction()+intent.getData().getSchemeSpecificPart());
            //if (intent.getPackage()==getPackageName()) return ;
            String action=intent.getAction();
            //String pkg=intent.getDataString();
            Log.d("apk","apk"+action);
            assert action != null;
            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                    String pkg = intent.getDataString();
                    if (pkg2del.contains(pkg)) {
                        pkg2del.remove(pkg);
                        if (pkg2del.isEmpty())
                            theTm.cancel();
                    } else ctr.handleAdd(intent.getDataString());
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    if (ctr.nextDelForSure) {
                        ctr.handleDel(intent.getDataString());
                        ctr.nextDelForSure = false;
                    } else {
                        //not so sure whether it's del or update
                        pkg2del.add(intent.getDataString());
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                theHd.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (String pkg : pkg2del)
                                            ctr.handleDel(pkg);
                                        pkg2del.clear();
                                    }
                                });
                            }
                        };
                        theTm = new Timer();
                        theTm.schedule(task, 1000);
                    }
                    //ctr.handleDel(intent.getDataString());
                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    ctr.handleRep(intent.getDataString());
                    break;
            }

        }
    }
    private class BatteryReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            int current= intent.getExtras().getInt("level");
            int total=intent.getExtras().getInt("scale");
            float ratio=1.0f*current/total;
            btry.setGuidelinePercent(ratio);
            //btry.setPadding(0,0,(int)offset,0);

        }
    }
    private class TimeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            cal=Calendar.getInstance();
            String str=""+
                    cal.get(Calendar.YEAR)+"/"+
                    (int)(cal.get(Calendar.MONTH)+1)+"/"+
                    cal.get(Calendar.DAY_OF_MONTH)+" "+
                    week[cal.get(Calendar.DAY_OF_WEEK)]+"\n"+
                    cal.get(Calendar.HOUR_OF_DAY)+":";
            int sec=cal.get(Calendar.MINUTE);
            if (sec<10)str+=("0"+sec);
            else str+=sec;
            tm.setText(str);

        }
    }
    private class HomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            ctr.scrollToTop();
            //ctr.scrollTable(0,0f);

        }
    }
    ApkReceiver ar;
    BatteryReceiver br;
    TimeReceiver tr;
    HomeReceiver hr;
    void regReceiver(){
        ar=new ApkReceiver();
        IntentFilter apkFilter=new IntentFilter();
        apkFilter.addAction("android.intent.action.PACKAGE_ADDED");
        apkFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        apkFilter.addAction("android.intent.action.PACKAGE_REPLACED");
        apkFilter.addDataScheme("package");
        registerReceiver(ar,apkFilter);
        br=new BatteryReceiver();
        IntentFilter batteryFilter=new IntentFilter();
        batteryFilter.addAction("android.intent.action.BATTERY_CHANGED");
        registerReceiver(br,batteryFilter);
        tr=new TimeReceiver();
        IntentFilter timeFilter=new IntentFilter();
        timeFilter.addAction(Intent.ACTION_TIME_TICK);//每分钟变化
        timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);//设置了系统时区
        timeFilter.addAction(Intent.ACTION_TIME_CHANGED);//设置了系统时间
        registerReceiver(tr,timeFilter);
        hr=new HomeReceiver();
        IntentFilter homeFilter=new IntentFilter();
        homeFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(hr,homeFilter);

    }
    void unregReceiver(){
        unregisterReceiver(ar);
        unregisterReceiver(br);
        unregisterReceiver(tr);
        unregisterReceiver(hr);
    }

    void regComp()   {
        fav_file=getString(R.string.fav_file);
        theHd=new Handler();
        //theTm=new Timer();
        pkg2del=new HashSet<>();
        drView=findViewById(R.id.dragBar);
        table=findViewById(R.id.iconTable);
        bg=findViewById(R.id.background);
        tm=findViewById(R.id.easyTime);
        scr=findViewById(R.id.plane);
        btry=findViewById(R.id.battery_guideLine);
        //menuSpin=findViewById(R.id.menuSpin);
        extraMenu=findViewById(R.id.extraMenu);
        //create one if none
        try {
            FileOutputStream opt=openFileOutput(fav_file,Context.MODE_APPEND);
            opt.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }


        ctr=new AllController();

        //ctr.spPkgs=getSharedPreferences(fav_file,Context.MODE_PRIVATE);

        //ctr.editor=ctr.spPkgs.edit();
        ctr.appTable=table;
        ctr.ctx=MainActivity.this;
        ctr.pm=getPackageManager();
        ctr.res=getResources();
        ctr.wm=getWindowManager();
        ctr.wpm= WallpaperManager.getInstance(MainActivity.this);
        ctr.ms = scr;//findViewById(R.id.plane);
        ctr.md= drView;
        ctr.imBg = bg;//findViewById(R.id.background);
        ctr.inflater = this.getLayoutInflater();
        ctr.main=this;
        ctr.init();
        Log.d("init","controller ok");
        drView.ctr=ctr;
        scr.ctr=ctr;
        //more
        //.setSelection(3);
        final PopupMenu pmn=new PopupMenu(MainActivity.this,extraMenu);
        pmn.getMenuInflater().inflate(R.menu.epop,pmn.getMenu());
        pmn.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.wallppChange:
                        Intent chooseIntent = new Intent(Intent.ACTION_SET_WALLPAPER);
                        Intent intent0 = new Intent(Intent.ACTION_CHOOSER);
                        intent0.putExtra(Intent.EXTRA_INTENT, chooseIntent);
                        intent0.putExtra(Intent.EXTRA_TITLE, "选择壁纸");
                        startActivityForResult(intent0,R.id.wallppChange);

                        break;
                    case R.id.refresh:
                        ctr.reLoadApps();
                        break;
                    case R.id.sysSetting:
                        Intent intent2=new Intent(Settings.ACTION_SETTINGS);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);
                        break;
                }
                return true;
            }
        });
        extraMenu.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                pmn.show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menuView=v;
        if((Boolean) v.getTag(R.id.isShortcut))
            getMenuInflater().inflate(R.menu.spop, menu);
        else
            getMenuInflater().inflate(R.menu.apop, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(menuView==null)
            return super.onContextItemSelected(item);
        String pkgName=(String)menuView.getTag(R.id.pkgName);
        switch (item.getItemId()){
            case R.id.menuFavorite:
                ctr.addShortCut(pkgName);
                break;
            case R.id.menuDetail:
                Intent it=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                it.setData(Uri.parse("package:"+pkgName));
                startActivity(it);
                break;
            case R.id.menuDeleteApp:
                ctr.deleteApp(pkgName);
                break;
            case R.id.menuDeleteShortcut:
                ctr.deleteShortCut(pkgName);
                break;
            default:
                break;
        }
        menuView=null;
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode==KeyEvent.KEYCODE_BACK){
            //scr.smoothScrollTo(0,0);
            ctr.scrollToTop();
            Log.d("key","return");
            return true;
        }
        else Log.d("key",""+keyCode);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case R.id.wallppChange:
                ctr.reLoadBackground();
                Log.d("result", "wallpp"+resultCode + "-" + data);
                break;
            case R.id.sysSetting:
                Log.d("result", "setting"+resultCode + "-" + data);
                break;
        }

    }

    @Override
    protected void onResume(){
    super.onResume();
    BatteryManager bmg=(BatteryManager)getSystemService(BATTERY_SERVICE);
    int left=bmg.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    btry.setGuidelinePercent(left/100.0f);
    cal=Calendar.getInstance();
    String str=""+
            cal.get(Calendar.YEAR)+"/"+
            (int)(cal.get(Calendar.MONTH)+1)+"/"+
            cal.get(Calendar.DAY_OF_MONTH)+" "+
            week[cal.get(Calendar.DAY_OF_WEEK)]+"\n"+
            cal.get(Calendar.HOUR_OF_DAY)+":";
    int sec=cal.get(Calendar.MINUTE);
    if (sec<10)str+=("0"+sec);
    else str+=sec;
    tm.setText(str);
    //drView.init();


}

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        try {
            drView.init();

            Log.d("init","dragger ok");
        }
        catch (Exception e){
            Log.e("init",e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        regReceiver();
        regComp();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregReceiver();
        //mOrientationListener.disable();

    }

}
