package com.wzt.yolov5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.wzt.yolov5.MainActivity.advanced_func_key;
import static com.wzt.yolov5.MainActivity.app_version;
import static com.wzt.yolov5.MainActivity.deviceid;
import static com.wzt.yolov5.MainActivity.key_create_time;
import static com.wzt.yolov5.MainActivity.mTempSensor;
import static com.wzt.yolov5.MainActivity.mactivity;
import static com.wzt.yolov5.MainActivity.openid;
import static com.wzt.yolov5.MainActivity.toast_msg;

public class Utils {

    public static void check_app_update(){

        try {
            if(new Date().getTime() - key_create_time > (long)368 * 86400 *1000) {

                advanced_func_key = 0;
                key_create_time = new Date().getTime();
                SharedPreferences userInfo = MainActivity.mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putLong("advanced_func_key", advanced_func_key);
                editor.putLong("key_create_time", key_create_time);
                editor.apply();
                toast_msg("激活码已失效");

            }else if(new Date().getTime() - key_create_time > (long)335 * 86400 *1000){
                toast_msg("激活码即将失效，请及时更换");
            }

        }catch (Exception e){}

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                try {
                    String url = "http://hayoou.com/safedriving/upgrade.php?openid="+openid+"&key="+advanced_func_key+"&deviceid="+deviceid+"&version="+app_version;
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        String resp = response.body().string();

                        String[] update_info = resp.split("\n");
                        if(update_info.length <3){
                            return;
                        }
                        String app_v = update_info[0];
                        String urlpage = update_info[1];
                        String info = update_info[2];

                        if(info.equals("key_expire")){
                            advanced_func_key = 0;
                            SharedPreferences userInfo = MainActivity.mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                            SharedPreferences.Editor editor = userInfo.edit();
                            editor.putLong("advanced_func_key",  advanced_func_key);
                            editor.apply();
                        }

                        if ( Float.parseFloat(app_version) < Float.parseFloat(app_v)) {
                            mactivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mactivity);
                                    builder.setTitle("APP更新");
                                    builder.setMessage("APP 有更新，是否需要更新？\n" + info);
                                    builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Uri uri = Uri.parse(urlpage);
                                            Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                            i.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                            mactivity.startActivity(i);
                                            dialog.cancel();
                                        }
                                    });
                                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.show();
                                }

                            });
                        }
                    }

                }catch(Exception e){
                    ((MainActivity) mactivity).toast_msg("升级信息获取失败，请检查网络状态");
                }
            }
        }).start();
    }

    public static void check_app_news(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                try {
                    String url = "http://hayoou.com/safedriving/news.php?openid="+openid+"&key="+advanced_func_key+"&deviceid="+deviceid+"&version="+app_version;
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        String resp = response.body().string();
                        if(resp.equals("")){
                            return;
                        }
                        String[] news_info = resp.split("\n");
                        if(news_info.length <3){
                            return;
                        }
                        String title = news_info[0];
                        String urlpage = news_info[1];
                        if(urlpage.contains("?")){

                        }else {
                            urlpage += "?";
                        }
                        urlpage += "&openid="+openid+"&key="+advanced_func_key+"&token="+advanced_func_key+"&create_time="+key_create_time;

                        String info = news_info[2];
                        if ( urlpage.length() > 5) {
                            String finalUrlpage = urlpage;
                            mactivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mactivity);
                                    builder.setTitle(title);
                                    builder.setMessage(info);
                                    builder.setPositiveButton("查看", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Uri uri = Uri.parse(finalUrlpage);
                                            Intent i = new Intent(Intent.ACTION_VIEW, uri);
                                            i.setFlags(FLAG_ACTIVITY_NEW_TASK);
                                            mactivity.startActivity(i);
                                            dialog.cancel();
                                        }
                                    });
                                    builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });
                                    builder.show();
                                }

                            });
                        }
                    }

                }catch(Exception e){
                    ((MainActivity) mactivity).toast_msg("信息获取失败，请检查网络状态");
                }
            }
        }).start();
    }

}
