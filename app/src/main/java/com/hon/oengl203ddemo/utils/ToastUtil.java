package com.hon.oengl203ddemo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Frank_Hon on 2017/4/23.
 * e-mail:frank_hon@foxmail.com
 */

public class ToastUtil {
    private static Toast sToast;

    public static void showToast(Context context,String content){
        if(sToast==null){
            synchronized (ToastUtil.class){
                if(sToast==null){
                    sToast=Toast.makeText(context,content,Toast.LENGTH_SHORT);
                }
            }
        }else{
            sToast.setText(content);
        }
        sToast.show();
    }
}
