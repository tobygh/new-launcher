package com.toby.newlauncher;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.toby.newlauncher.view.MyDragbarView;
import com.toby.newlauncher.view.MyScrollView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.util.TreeSet;

public class AllController {
    public
    boolean nextDelForSure=false;
    MyScrollView ms;
    MyDragbarView md;
    ImageView imBg;
    LinearLayout appTable;
    GridLayout[] appDrawers;
    //need to be initialized outside
    Activity main;
    WindowManager wm;
    WallpaperManager wpm;
    PackageManager pm;
    Resources res;
    Context ctx;
    LayoutInflater inflater;
    int scIndex=0;

    class appdatabase {
        HashMap<Integer,MyAppHolder>allApps;
        //List<MyAppHolder> allApps;
        HashMap<String, TreeSet<Pair<String, Integer>>> appCategory; //<rank,pkgName>
        HashMap<String, Integer> pkg2Idx;
        HashMap<String, Boolean> pkgHasSc;
        appdatabase(){
            appCategory=new HashMap<>();
            allApps =new HashMap<>();
            pkg2Idx=new HashMap<>();
            pkgHasSc=new HashMap<>();
        }
        public void clear(){
            appCategory.clear();
            pkg2Idx.clear();
            allApps.clear();
            pkgHasSc.clear();
        }
        public void init(){
            for(int i=0;i<bartext.length();i++){
                appCategory.put(bartext.substring(i,i+1), new TreeSet<>(
                                new Comparator<Pair<String, Integer>>() {
                                    @Override
                                    public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
                                        return o1.first.compareTo(o2.first);
                                    }

                                    @Override
                                    public boolean equals(Object obj) {
                                        return false;
                                    }
                                }
                        )
                );
            }
        }
        public boolean addApp(MyAppHolder appHolder){
            //String cat=appHolder.label.substring(0,1);
            //if(!bartext.contains(cat))cat="#";
            if(pkg2Idx.containsKey(appHolder.pkgName)){
                return false;
            }
            scIndex+=1;
            allApps.put(scIndex,appHolder);
            Objects.requireNonNull(appCategory.get(appHolder.cat)).add(Pair.create(appHolder.rank,scIndex));
            pkg2Idx.put(appHolder.pkgName,scIndex);
            pkgHasSc.put(appHolder.pkgName,false);
            commitChange(bartext.indexOf(appHolder.cat));
            return true;
            //Toast.makeText(main, "新安装了"+appHolder.label,Toast.LENGTH_SHORT).show();

        }
        public void delApp(String pkgName) {
            if(!pkg2Idx.containsKey(pkgName)){
                return ;
            }

            int index=pkg2Idx.get(pkgName);

            MyAppHolder holder=allApps.get(index);
            //assert holder != null;
            String cat=""+holder.cat;
            //allApps.remove(index);
            allApps.remove(index);
            //more
            Log.d("del",holder.cat);
            appCategory.get(holder.cat).remove(Pair.create(holder.rank,index));
            pkg2Idx.remove(pkgName);
            if(pkgHasSc.get(pkgName)){
                spDel(pkgName);
                pkgHasSc.remove(pkgName);
                //pkgHasSc.put(pkgName,false);
                appCategory.get(bartext.substring(0,1)).remove(Pair.create(holder.rank,index));
                commitChange(0);
            }
            commitChange(bartext.indexOf(cat));
            if(index==scIndex)
                scIndex-=1;
            Toast.makeText(main, "卸载成功",Toast.LENGTH_SHORT).show();

        }
        public String addShortcut(String pkgName){
            if(!pkg2Idx.containsKey(pkgName)||pkgHasSc.get(pkgName))
                return "";
            int index=pkg2Idx.get(pkgName);

            pkgHasSc.put(pkgName,true);
            MyAppHolder holder=allApps.get(index);
            appCategory.get(bartext.substring(0,1)).add(Pair.create(holder.rank,index));
            spAdd(pkgName);
            commitChange(0);
            return holder.label;
        }
        public String delShortcut(String pkgName){
            if(!pkg2Idx.containsKey(pkgName)||!pkgHasSc.get(pkgName))
                return "";
            pkgHasSc.put(pkgName,false);
            spDel(pkgName);
            int index=pkg2Idx.get(pkgName);
            MyAppHolder holder=allApps.get(index);
            appCategory.get(bartext.substring(0,1)).remove(Pair.create(holder.rank,index));
            commitChange(0);
            return holder.label;
        }
        public List<MyAppHolder> getApps(int index){
            List<MyAppHolder> res=new ArrayList<>();
            for(Pair<String,Integer>s:appCategory.get(bartext.substring(index, index + 1))){

                res.add(allApps.get(s.second));
            }
            return res;
        }
        public List<MyAppHolder> getApps(String sIndex){
            List<MyAppHolder> res=new ArrayList<>();
            for(Pair<String,Integer>s:appCategory.get(sIndex)){
                res.add(allApps.get(s.second));
            }
            return res;
        }
        void spAdd(String pkgName)  {
            try{
                FileInputStream ipt=ctx.openFileInput(fav_file);

                byte[] cc=new byte[ipt.available()];
                ipt.read(cc);ipt.close();
                String res=new String(cc);
                res+=" "+pkgName;
                FileOutputStream opt=ctx.openFileOutput(fav_file,Context.MODE_PRIVATE);
                opt.write(res.getBytes());
                opt.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
        void spDel(String pkgName) {
            try {
                FileInputStream ipt = ctx.openFileInput(fav_file);
                byte[] cc=new byte[ipt.available()];
                ipt.read(cc);ipt.close();
                String res=new String(cc);
                Log.d("file",res);
                String[] pkgs=res.split(" ");
                StringBuilder ans= new StringBuilder();
                for(String str:pkgs){
                    if(!str.equals(pkgName)) ans.append(" ").append(str);
                }
                FileOutputStream opt=ctx.openFileOutput(fav_file,Context.MODE_PRIVATE);
                opt.write(ans.toString().getBytes());
                opt.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        String[] spGet()  {
            try {
                FileInputStream ipt = ctx.openFileInput(fav_file);
                byte[] cc = new byte[ipt.available()];
                ipt.read(cc);
                ipt.close();
                String res = new String(cc);
                String[] pkgs = res.split(" ");
                return pkgs;
            }
            catch (IOException e){
                e.printStackTrace();
                return new String[0];
            }
        }
        void loadSp(){
            for(String spPkg:spGet()){
                if(pkg2Idx.containsKey(spPkg)) {
                    pkgHasSc.put(spPkg, true);
                    int index=pkg2Idx.get(spPkg);
                    MyAppHolder holder=allApps.get(index);
                    appCategory.get(bartext.substring(0,1)).add(Pair.create(holder.rank,index));
                }
            }
        }
    }
    appdatabase appdatabase;

    String bartext;//"★#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String fav_file;//favoriteApps
    //boolean updating=false;

    final int nCol=5;
    Handler theHd;
    Timer theTm;
    int nApps=0;
    //private List<Integer>updateList;
    //private List<String>toastList;

    AllController(){
        appdatabase=new appdatabase();

        theHd=new Handler();
        theTm=new Timer();

    }
    public void reLoadApps()  {
        //Toast.makeText(main, "正在重新加载桌面",Toast.LENGTH_LONG).show();

        appTable.removeAllViews();
        nApps=0;
        loadApps();
        reLoadBackground();
        Toast.makeText(main, "桌面焕然一新啦",Toast.LENGTH_SHORT).show();

    }
    public void reLoadBackground(){
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        Drawable wallpaper=wpm.getDrawable();
        Bitmap bm_wallpaper=((BitmapDrawable)wallpaper).getBitmap();
        int Sw=dm.widthPixels,Sh=dm.heightPixels,Pw=bm_wallpaper.getWidth(),Ph=bm_wallpaper.getHeight();
        //Log.i("debug","Sw"+Sw+" Sh"+Sh+" Pw"+Pw+"Ph"+Ph);
        double scaleK1=1.0,scaleK2=1.0,scale;
        if (Sw>Pw)scaleK1=1.0*Sw/Pw;
        if(Sh>Ph)scaleK2=1.0*Sh/Ph;
        scale=Math.max(scaleK1,scaleK2);
        Matrix opMatrix=new Matrix();
        opMatrix.postScale((float) scale,(float) scale);

        bm_wallpaper=Bitmap.createBitmap(bm_wallpaper,0,0,Pw,Ph,opMatrix,true);
        bm_wallpaper=Bitmap.createBitmap(bm_wallpaper,0,0,Sw,Sh);
        BitmapDrawable bd_wallpaper=new BitmapDrawable(res,bm_wallpaper);
        imBg.setImageDrawable(bd_wallpaper);
    }
    private LinearLayout genAppView(MyAppHolder appInfo,GridLayout root,int jIndex){
        LinearLayout ll=genEmpView(root,jIndex);
        ImageView iv = (ImageView) ll.getChildAt(0);
        iv.setImageDrawable(appInfo.drawable);
        TextView tv = (TextView) ll.getChildAt(1);
        tv.setText(appInfo.label);
        ll.setTag(R.id.pkgName,appInfo.pkgName);
        ll.setTag(R.id.isShortcut,true);
        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pkgName=(String)v.getTag(R.id.pkgName);
                Intent itt=pm.getLaunchIntentForPackage(pkgName);
                ctx.startActivity(itt);
            }
        });
        main.registerForContextMenu(ll);
        return ll;
    }
    private LinearLayout genEmpView(GridLayout root,int jIndex){
        inflater.inflate(R.layout.appholderlayout,root,true);
        LinearLayout ll=(LinearLayout)root.getChildAt(jIndex);
        GridLayout.LayoutParams grlp=new GridLayout.LayoutParams();
        grlp.setMargins(0,0,0,5);
        grlp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1.0f);
        //快安装好时退回桌面会崩溃
        //Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'void android.widget.LinearLayout.setLayoutParams(android.view.ViewGroup$LayoutParams)' on a null object reference
        ll.setLayoutParams(grlp);
        return ll;
    }
    public void init() {
        bartext=ctx.getString(R.string.barText);

        if (appTable==null||
                ctx==null||
                pm==null||
                res==null||
                wm==null||
                wpm==null||
                ms ==null||
                imBg ==null||
                inflater ==null||
                main==null||
                bartext==null
                )
            throw new java.lang.IllegalArgumentException();
        appdatabase.init();
        loadApps();
        reLoadBackground();
    }
    public void loadApps() {
        fav_file=ctx.getString(R.string.fav_file);
        
        LinearLayout.LayoutParams lllp= new LinearLayout.LayoutParams(-1,-2);
        lllp.bottomMargin=10;
        //init app containers/database
        appDrawers=new GridLayout[bartext.length()];
        for(int i=0;i<bartext.length();i++){

            int idd=res.getIdentifier("table_row_"+i,"id",ctx.getPackageName());
            inflater.inflate(R.layout.rowholderlayout,appTable,true);//return root
            LinearLayout ll =(LinearLayout)appTable.getChildAt(i);
            appDrawers[i]=(GridLayout) ll.getChildAt(1);
            TextView title=(TextView)ll.getChildAt(0);
            title.setText(bartext.substring(i,i+1));
            ll.setId(idd);
            ll.setLayoutParams(lllp);
            //appTable.addView(ll);
        }

        //load apps
        Intent mainIntent=new Intent(Intent.ACTION_MAIN,null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo>resList=pm.queryIntentActivities(mainIntent,0);
        MyAppHolder curApp;
        //put all apps into their categories/containers
        for(ResolveInfo rs:resList){

            curApp= new MyAppHolder(nApps,""+rs.loadLabel(pm),rs.activityInfo.packageName,rs.loadIcon(pm));
            boolean res=appdatabase.addApp(curApp);
            //allApps.add(curApp);
            //pkg2Idx.put(curApp.pkgName,nApps);
            //Log.d("pkg",curApp.pkgName);
            //nApps+=1;
            //char rk=curApp.rank.charAt(0);
            //if('A'<=rk&&rk<='Z'){
            //    appCategory.get(""+rk).add(Pair.create(curApp.rank,curApp.pkgName));
            //}
            //else{
            //    appCategory.get("#").add(Pair.create(curApp.rank,curApp.pkgName));
            //}
        }
        //load favorite apps
        appdatabase.loadSp();
        //String[] pkgs=spGet();
        //for (String pkgName : pkgs) {
        //    appdatabase.addShortcut(pkgName);
            //if(pkg2Idx.containsKey(pkgName)){
            //    Log.d("debug","allapps"+allApps.size());
            //    String rank = allApps.get(pkg2Idx.get(pkgName)).rank;
            //    appCategory.get(bartext.substring(0, 1)).add(Pair.create(rank, pkgName));
            //    pkgHasSc.put(pkgName, true);
            //}
        //}

        //generate all views
        for(int i=0;i<bartext.length();i++){
            refillGridview(i);
        }
    }
    public void commitChange(int index){
        ms.dontDraw=true;
        //if(null==updateList)updateList=new ArrayList<>();
        //updateList.add(index);
        //ms.invalidate();
        refillGridview(index);
        ms.dontDraw=false;
        ms.invalidate();
    }
    public void refillGridview(int index) {
        List<MyAppHolder> apps=appdatabase.getApps(index);//appCategory.get(bartext.substring(index,index+1));
        //assert pkgSet != null;
        //LinearLayout ll=(LinearLayout) appTable.getChildAt(index);
        //GridLayout gl=(GridLayout) ll.getChildAt(1);
        GridLayout gl=appDrawers[index];
        gl.removeAllViews();
        gl.setColumnCount(nCol);
        int sz=apps.size();
        int nRow=sz/nCol;if (sz%nCol!=0)nRow++;
        gl.setRowCount(nRow);
        int full=nRow*nCol;
        //MyAppHolder appHolder;
        int jIndex=0;
        for (MyAppHolder app: apps) {
            //appHolder=allApps.get(idx);
            LinearLayout appview=genAppView(app,gl,jIndex);
            if(index==0)
                appview.setTag(R.id.isShortcut,true);
            else
                appview.setTag(R.id.isShortcut,false);
            //gl.addView(appview);
            jIndex+=1;
        }
        for(int j=sz;j<full;j++){
            genEmpView(gl,j);
            //gl.addView();
        }
    }
    public void handleAdd(String pkg)   {
        //reLoadApps();
        pkg=pkg.substring(8);
        ApplicationInfo appInfo=null;
        try{appInfo=pm.getApplicationInfo(pkg,0);}
        catch (PackageManager.NameNotFoundException e){e.printStackTrace();}
        if(appInfo==null)return;
        MyAppHolder holder=new MyAppHolder(nApps,""+appInfo.loadLabel(pm),appInfo.packageName,appInfo.loadIcon(pm));
        /*allApps.add(holder);
        pkg2Idx.put(holder.pkgName,nApps);
        String idx=holder.rank.substring(0,1);
        if(!bartext.contains(idx))
            idx="#";
        //Log.d("num",Integer.getInteger(idx)+","+idx);
        appCategory.get(idx).add(Pair.create(holder.rank,holder.pkgName));
        nApps+=1;
        //ms.needUpdate=true;
        //refillGridview(bartext.indexOf(idx));*/
        boolean res=appdatabase.addApp(holder);
        //commitChange(bartext.indexOf(holder.cat));
        if(res)Toast.makeText(main, "新安装了"+holder.label,Toast.LENGTH_SHORT).show();


    }
    public void handleDel(String pkg) {
        pkg = pkg.substring(8);
        //reLoadApps();
        /*
        String pkgName=pkg.substring(8);
        if(!pkg2Idx.containsKey(pkgName))
            return;
        //Caused by: java.lang.IndexOutOfBoundsException: Index: 92, Size: 92
        int idx=pkg2Idx.get(pkgName);

        MyAppHolder holder=allApps.get(idx);
        String sIdx=holder.rank.substring(0,1);

        if(!bartext.contains(sIdx))
            sIdx="#";
        // Caused by: java.lang.NullPointerException: Attempt to invoke virtual method 'boolean java.util.TreeSet.remove(java.lang.Object)' on a null object reference
        //Log.d("num",Integer.getInteger(sIdx)+","+sIdx);
        appCategory.get(sIdx).remove(Pair.create(holder.rank,holder.pkgName));
        allApps.remove(idx);
        pkg2Idx.remove(pkg);
        //refillGridview(bartext.indexOf(sIdx));
        putChange(bartext.indexOf(sIdx));
        if(pkgHasSc.containsKey(pkgName)&&pkgHasSc.get(pkgName))
        {
            pkgHasSc.put(pkgName,false);
            String rank=holder.rank;
            appCategory.get(bartext.substring(0,1)).remove(Pair.create(rank,pkgName));
            spDel(pkgName);
            //refillGridview(0);
            putChange(bartext.indexOf(0));

        }*/
        appdatabase.delApp(pkg);

    }
    public void handleRep(String pkg) {
            //pkg = pkg.substring(8);
        Toast.makeText(main, "更新成功",Toast.LENGTH_SHORT).show();

    }

    public void scrollTable(int index,float y){
        View selected=appTable.getChildAt(index);
        if(selected==null)return ;

        float sy=selected.getY(),sh=selected.getHeight(),msy=ms.getHeight(),aimScrollTo;
        aimScrollTo=sy-y+sh/2;
        //  sy+sh-msy< aimScrollTo < sy
        aimScrollTo=Math.max(aimScrollTo,sy+sh-msy);
        aimScrollTo=Math.min(aimScrollTo,sy);
        /*if(index>0)
            y+=selected.getHeight()/2f;
        if(index+1==bartext.length())
            y+=selected.getHeight()/2f;*/

        ms.smoothScrollTo(0,Math.round(aimScrollTo));
    }
    public void scrollBar(int index){

    }
    public void beginDrag(){
        md.bringToFront();

    }
    public void endDrag(){
        ms.bringToFront();
    }

    public void addShortCut(String pkgName) {

        /*if(!pkg2Idx.containsKey(pkgName))
            return;
        MyAppHolder holder=allApps.get(pkg2Idx.get(pkgName));
        if (pkgHasSc.containsKey(pkgName)&&pkgHasSc.get(pkgName)){
            Toast.makeText(main, holder.label+"已经在常用中！",Toast.LENGTH_SHORT).show();
            return;
        }
        pkgHasSc.put(pkgName,true);

        String rank=holder.rank;
        appCategory.get(bartext.substring(0,1)).add(Pair.create(rank,pkgName));
        spAdd(pkgName);
        //refillGridview(0);
        putChange(0);
        */
        String label=appdatabase.addShortcut(pkgName);
        //commitChange(0);
        Toast.makeText(main, "已将"+label+"添加至常用",Toast.LENGTH_SHORT).show();
    }
    public void deleteShortCut(String pkgName) {
        /*if(!pkg2Idx.containsKey(pkgName))
            return;
        MyAppHolder holder=allApps.get(pkg2Idx.get(pkgName));
        if (pkgHasSc.containsKey(pkgName)){
            if(pkgHasSc.get(pkgName)){
                pkgHasSc.put(pkgName,false);
                String rank=holder.rank;
                appCategory.get(bartext.substring(0,1)).remove(Pair.create(rank,pkgName));
                spDel(pkgName);
                //refillGridview(0);
                putChange(0);
                Toast.makeText(main, "已将"+holder.label+"从常用移除",Toast.LENGTH_SHORT).show();

            }
        }*/
        String label=appdatabase.delShortcut(pkgName);

        Toast.makeText(main, "已将"+label+"从常用移除",Toast.LENGTH_SHORT).show();
    }
    public void deleteApp(String pkgName){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + pkgName));
        nextDelForSure=true;
        main.startActivityForResult(intent, 0);

    }
}
