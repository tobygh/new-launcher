package com.toby.newlauncher;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.github.promeg.pinyinhelper.Pinyin;

public class MyAppHolder{
    Drawable drawable;
    String label;
    String rank;
    String pkgName;
    String cat;
    int id;
    public MyAppHolder(int _id,String _label,String _pkg,Drawable _drawable){
        id=_id;
        label=_label;
        rank=getPinyin(label);
        pkgName=_pkg;
        drawable=_drawable;

    }

    private String getPinyin(String str){
        StringBuilder res= new StringBuilder();
        char ch;
        for(int i=0;i<str.length();i++){
            ch=str.charAt(i);
            if(Pinyin.isChinese(ch)){
                res.append(Pinyin.toPinyin(ch));
            }
            else if('a'<=ch&&ch<='z') res.append((char) (ch - 'a' + 'A'));
            else if('A'<=ch&&ch<='Z') res.append(ch);
            else if('0'<=ch&&ch<='9') res.append(ch);
        }
        ch=res.charAt(0);

        if('A'<=ch&&ch<='Z')
            cat= ""+(ch);
        else cat="#";
        return res.toString();
    }
}
