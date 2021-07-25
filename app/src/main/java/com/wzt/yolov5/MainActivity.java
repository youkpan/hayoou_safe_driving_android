package com.wzt.yolov5;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.ExifInterface;
import android.media.SoundPool;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import wseemann.media.FFmpegMediaMetadataRetriever;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.umeng.commonsdk.UMConfigure.DEVICE_TYPE_PHONE;
import static com.wzt.yolov5.SafeDetect.direction_center_x_offset;
import static com.wzt.yolov5.SafeDetect.get_safe_zone_detect;
import static com.wzt.yolov5.SafeDetect.hit_predict;
import static com.wzt.yolov5.SafeDetect.hit_predict_time;
import static com.wzt.yolov5.SafeDetect.near_object_distance;
import static com.wzt.yolov5.SafeDetect.near_object_speed;
import static com.wzt.yolov5.SafeDetect.screen_auto_off;

public class MainActivity extends AppCompatActivity {
    public static int YOLOV5S = 1;
    public static int YOLOV4_TINY = 2;
    public static int MOBILENETV2_YOLOV3_NANO = 3;
    public static int SIMPLE_POSE = 4;
    public static int YOLACT = 5;
    public static int ENET = 6;
    public static int FACE_LANDMARK = 7;
    public static int DBFACE = 8;
    public static int MOBILENETV2_FCN = 9;
    public static int MOBILENETV3_SEG = 10;
    public static int YOLOV5_CUSTOM_LAYER = 11;
    public static int NANODET = 12;
    public static int YOLO_FASTEST_XL = 13;
    public static int LANE_LSTR = 14;
    public static int YOLOX = 14;

    public static boolean CONFIG_MULTI_DETECT = true;
    public static String app_version="2.7";
    public static boolean ultra_fast_mode = false;

    public static int USE_MODEL = LANE_LSTR;

    public static boolean USE_GPU = false;

    public static CameraX.LensFacing CAMERA_ID = CameraX.LensFacing.BACK;

    public static Sensor mTempSensor =null;
    public static float device_temperature = 25;

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_PICK_VIDEO = 3;
    private static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    private Toolbar toolbar;
    private ImageView resultImageView;
    private ResultView mResultView;
    private ImageView iv_detect_input;
    private ImageView iv_lane_input;
    private TextView tvNMS;
    private TextView tvThreshold;
    private TextView tvFront;
    private TextView tvAlarmWait;
    private TextView tvLaneDetectHeight;
    private SeekBar nmsSeekBar;
    private SeekBar thresholdSeekBar;
    private SeekBar frontSeekBar;
    private SeekBar alarmWaitSeekBar;
    private SeekBar laneDetectHeightSeekBar;

    private TextView tvNMNThreshold;
    private TextView tvInfo;
    private TextView tvDetectWidth;
    private TextView tvCityDetectHeight;

    private Button btnPhoto;
    private Button btnInputSize;
    private Button btnAbout;
    private Button btnZhangying;
    private Button btnVideo;
    private Button btnSetting;
    private Button btnCamera;
    private Button btnAdvanced_key;
    private Button btnSwitch_alarm_mode;
    private Button btnParam_setting;
    private Button btnRoadType ;
    private Button btnDirectionAutoAdjust ;
    private Button btnPersonDetectFocus ;
    private Button btnMute ;
    private Button btnDistance_setting;
    private Button btnHitPredict;
    private Button btnScreenAutoOff;
    private Button btnUltra_fast;

    private  boolean param_toggle = false;
    public static double threshold = 0.3, nms_threshold = 0.7;
    public static double front_detect=0.5;
    public static float DetectWidth = 1.05f;
    public static float CityDetectHeight = 0.5f;
    public static float LaneDetectHeight = 0.5f;

    private TextureView viewFinder;
    private SeekBar sbVideo;
    private SeekBar sbVideoSpeed;
    private SeekBar DetectWidthSeekBar;
    private SeekBar CityDetectHeightSeekBar;

    protected float videoSpeed = 1.0f;
    protected long videoCurFrameLoc = 0;
    public static int VIDEO_SPEED_MAX = 20 + 1;
    public static int VIDEO_SPEED_MIN = 1;
    public static int frame_count = 0;

    private int current_rotation_degree = 0;

    private AtomicBoolean detectCamera = new AtomicBoolean(false);
    private AtomicBoolean detectPhoto = new AtomicBoolean(false);
    private AtomicBoolean detectVideo = new AtomicBoolean(false);
    private AtomicBoolean detectYolov4 = new AtomicBoolean(false);

    private long startTime = 0;
    private long endTime = 0;
    private int width;
    private int height;

    public double total_fps = 0;
    public int fps_count = 0;
    public double avg_fps = 0;

    public static float recent_fps =0 ;

    protected Bitmap mutableBitmap;
    protected Bitmap resizedBitmap;
    protected Bitmap bitmapsrc;

    private long last_press_back_time =0 ;

    ExecutorService detectService = Executors.newSingleThreadExecutor();

    FFmpegMediaMetadataRetriever mmr;

    public static boolean view_setting_lines = false;
    public static int USE_DEBUG_PHOTO_ID = 0;
    public static boolean USE_FAST_EXP = false;

    public int screen_width =1920;
    public int screen_height = 1080;
    public SoundPool mSoundPool;
    public int alarm_voiceId;
    public int alarm_voiceId1;
    public int alarm_voiceId2;
    public int alarm_voiceId3;
    public int highway_voiceId;
    public int cityroad_voiceId;
    public int outskirts_voiceId;
    public int direction_off_voiceId;
    public int direction_on_voiceId;
    public int person_focus_off_voiceId;
    public int person_focus_on_voiceId;
    public static int front_car_start_voiceId;
    public static int mute_2min_voiceId;
    public static int mute_voiceId;
    public static int sound_on_voiceId;
    public static int welcome_voiceId;
    public static int rest_2hour_voiceId;
    public static int toohot_voiceId;
    public static int alarm_wait_time = 20;

    public static long mute_end=0;
    public static int mute_func_idx=0;

    public SafeDetect msafeDetect = new SafeDetect();

    public static String detect_msg = "";
    public static Context mcontext;
    public static Activity mactivity;
    public static long advanced_func_key = 0;
    public static long key_create_time = 0;
    public static int input_size_idx = 2 ;
    //0 静音 ，1 开启，2，关闭车道提示 3，关闭物体提示 4,关闭前车起步提示
    public static int alarm_mode = 1;
    public static int last_alarm_mode = alarm_mode;
    public static String openid = "";
    public static String deviceid = "";
    //0 不确定 ，1 高速，2，城市 3，郊外
    public static int road_type = 1;
    //0 关闭 ，1 自动，2，自动水平 3，自动前后
    public static int auto_adjust_detect_area = 0;
    public static int person_detect_focus = 0;
    public static boolean far_enhanced_detect = true;
    public static  int USE_YOLOV4_DETECT = 0;
    public static  float carmera_height = 1.2f;
    public static  float distance_fix = 1.f;
    public static  float vertical_distance_rate = 1.f;
    public static long sys_start_time = 0;
    public static long reset_notice_start_time = 0;
    public static long toohot_alarm_time =0;
    public static Box[] detect_full_result = null;
    public static Box[] detect_far_result = null;
    public static Box[] lane_result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mcontext = this;
        mactivity = this;
        setContentView(R.layout.activity_main);
        sys_start_time = SystemClock.elapsedRealtime();
        reset_notice_start_time = sys_start_time;
        init_param();

        initModel();
        initViewID();
        initViewListener();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        screen_width = outMetrics.widthPixels;
        screen_height = outMetrics.heightPixels;

        initSoundPool();

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA
            );
            startTime = new Date().getTime();
        }else{
            startCamera();
        }

        try {
            init_youmeng_SDK();
            Utils.check_app_update();
        }catch (Exception e) {

        }
        try {
            Utils.check_app_news();
        }catch (Exception e) {

        }

        try{
            SensorManager mSmanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            //List<Sensor> allSensors = mSmanager.getSensorList(Sensor.TYPE_ALL);
            mTempSensor =   mSmanager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            // 温度传感器的监听器
            SensorEventListener mSensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (event.sensor.getStringType().toUpperCase().indexOf("TEMP") > 0) {
                        /*温度传感器返回当前的温度，单位是摄氏度（°C）。*/
                        device_temperature = event.values[0];
                        //Log.e("temperature: ", String.valueOf(device_temperature));
                        //mSmanager.unregisterListener(mSensorEventListener, mTempSensor);
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                }
            };

            if (mTempSensor != null) {
                mSmanager.registerListener(mSensorEventListener, mTempSensor
                        , SensorManager.SENSOR_DELAY_GAME);
            }
        }catch (Exception e) {

        }
        toast_msg("安全辅助驾驶启动中，请将音量调到最大，点击设置按钮 对齐摄像头地平线到水平参考线，垂直线对准道路中心");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                    play_sound(welcome_voiceId);
                } catch (Exception e) {
                }
            }}).start();
}

    protected void init_youmeng_SDK(){
        UMConfigure.init(mcontext, "60215b0b425ec25f10f301a4", "release", DEVICE_TYPE_PHONE, "");
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    protected void checkCamera(){

            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        mactivity,
                        PERMISSIONS_CAMERA,
                        REQUEST_CAMERA
                );
            }else {
                startCamera();
            }

    }

    protected void initSoundPool(){

        //sdk版本21是SoundPool 的一个分水岭
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入最多播放音频数量,
            builder.setMaxStreams(10);
            //AudioAttributes是一个封装音频各种属性的方法
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适的属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            //加载一个AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            mSoundPool = builder.build();

        } else {
            /**
             * 第一个参数：int maxStreams：SoundPool对象的最大并发流数
             * 第二个参数：int streamType：AudioManager中描述的音频流类型
             *第三个参数：int srcQuality：采样率转换器的质量。 目前没有效果。 使用0作为默认值。
             */
            mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }
        alarm_voiceId = mSoundPool.load(this, R.raw.alarm, 1);
        alarm_voiceId1 = mSoundPool.load(this, R.raw.alarm1, 1);
        alarm_voiceId2 = mSoundPool.load(this, R.raw.alarm2, 1);
        alarm_voiceId3 = mSoundPool.load(this, R.raw.alarm3, 1);

        highway_voiceId = mSoundPool.load(this, R.raw.highway, 1);
        cityroad_voiceId = mSoundPool.load(this, R.raw.cityroad, 1);
        outskirts_voiceId = mSoundPool.load(this, R.raw.outskirts, 1);
        direction_off_voiceId = mSoundPool.load(this, R.raw.direction_adjust_off, 1);
        direction_on_voiceId = mSoundPool.load(this, R.raw.direction_adjust_on, 1);
        person_focus_off_voiceId = mSoundPool.load(this, R.raw.person_focus_off, 1);
        person_focus_on_voiceId = mSoundPool.load(this, R.raw.person_focus_on, 1);
        front_car_start_voiceId = mSoundPool.load(this, R.raw.front_car_start, 1);
        mute_2min_voiceId = mSoundPool.load(this, R.raw.mute2min, 1);
        mute_voiceId = mSoundPool.load(this, R.raw.mute, 1);
        sound_on_voiceId = mSoundPool.load(this, R.raw.sound_on, 1);
        welcome_voiceId = mSoundPool.load(this, R.raw.welcome, 1);
        rest_2hour_voiceId = mSoundPool.load(this, R.raw.rest_at_2hour, 1);
        toohot_voiceId = mSoundPool.load(this, R.raw.rest_at_2hour, 1);
    }

    public void play_alarm(float rate){
        //第一个参数soundID
        //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
        //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
        //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
        //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
        //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
        mSoundPool.play(alarm_voiceId2, 1, 1, 1, 0, 1);
    }
    public void play_alarm1(float rate){
        //第一个参数soundID
        //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
        //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
        //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
        //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
        //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
        mSoundPool.play(alarm_voiceId1, 1, 1, 1, 0, 1);
    }
    public void play_alarm3(){
        //第一个参数soundID
        //第二个参数leftVolume为左侧音量值（范围= 0.0到1.0）
        //第三个参数rightVolume为右的音量值（范围= 0.0到1.0）
        //第四个参数priority 为流的优先级，值越大优先级高，影响当同时播放数量超出了最大支持数时SoundPool对该流的处理
        //第五个参数loop 为音频重复播放次数，0为值播放一次，-1为无限循环，其他值为播放loop+1次
        //第六个参数 rate为播放的速率，范围0.5-2.0(0.5为一半速率，1.0为正常速率，2.0为两倍速率)
        mSoundPool.play(alarm_voiceId3, 1, 1, 1, 0, 1.5f);
    }
    public void stop_alarm(){
        mSoundPool.stop(alarm_voiceId2);
    }
    public void stop_alarm3(){
        mSoundPool.stop(alarm_voiceId3);
    }
    public void play_sound(int resid){
        mSoundPool.play(resid, 1, 1, 1, 0, 1f);
    }

    protected void initViewListener() {
        toolbar.setNavigationIcon(R.drawable.actionbar_dark_back_icon);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (USE_MODEL != YOLOV5S  && USE_MODEL != DBFACE && USE_MODEL != NANODET && USE_MODEL != YOLOV5_CUSTOM_LAYER) {
            nmsSeekBar.setEnabled(false);
            laneDetectHeightSeekBar.setEnabled(false);
            thresholdSeekBar.setEnabled(false);
            frontSeekBar.setEnabled(false);
            DetectWidthSeekBar.setEnabled(false);
            tvDetectWidth.setVisibility(View.GONE);
            DetectWidthSeekBar.setVisibility(View.GONE);
            tvCityDetectHeight.setVisibility(View.GONE);
            CityDetectHeightSeekBar.setVisibility(View.GONE);
            laneDetectHeightSeekBar.setVisibility(View.GONE);
            tvFront.setVisibility(View.GONE);
            tvLaneDetectHeight.setVisibility(View.GONE);
            frontSeekBar.setVisibility(View.GONE);
            tvNMS.setVisibility(View.GONE);
            tvThreshold.setVisibility(View.GONE);
            nmsSeekBar.setVisibility(View.GONE);
            thresholdSeekBar.setVisibility(View.GONE);
            tvNMNThreshold.setVisibility(View.GONE);
            tvAlarmWait.setVisibility(View.GONE);
            alarmWaitSeekBar.setVisibility(View.GONE);
            btnParam_setting.setVisibility(View.GONE);
            btnDistance_setting.setVisibility(View.GONE);
            btnHitPredict.setVisibility(View.GONE);
            btnScreenAutoOff.setVisibility(View.GONE);
            btnUltra_fast.setVisibility(View.GONE);
        } else if (USE_MODEL == YOLOV5S) {
            threshold = 0.3f;
            nms_threshold = 0.7f;
        } else if (USE_MODEL == DBFACE || USE_MODEL == NANODET) {
            threshold = 0.4f;
            nms_threshold = 0.6f;
        } else if (USE_MODEL == YOLOV5_CUSTOM_LAYER) {
            threshold = 0.25f;
            nms_threshold = 0.45f;
        }

        nmsSeekBar.setProgress((int) (nms_threshold * 100));
        thresholdSeekBar.setProgress((int) (threshold * 100));
        frontSeekBar.setProgress((int) (front_detect * 100));
        alarmWaitSeekBar.setProgress((int) (alarm_wait_time ));
        DetectWidthSeekBar.setProgress((int) ( (DetectWidth-0.2f)/1.8f *100));
        CityDetectHeightSeekBar.setProgress((int) ( CityDetectHeight *100));
        laneDetectHeightSeekBar.setProgress((int) ( LaneDetectHeight *100));
        final String format = "重叠: %.2f，阈值：%.2f";
        final String format2 = "高速检测距离: %.2f";
        tvFront.setText(String.format(Locale.ENGLISH, format2, front_detect));
        frontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                front_detect = i / 100.f;
                tvFront.setText(String.format(Locale.ENGLISH, format2, front_detect));

                SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("front_detect", (float)front_detect);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        final String format6 = "道路检测距离: %.2f";
        tvLaneDetectHeight.setText(String.format(Locale.ENGLISH, format6, LaneDetectHeight));
        laneDetectHeightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                LaneDetectHeight = i / 100.f;
                tvLaneDetectHeight.setText(String.format(Locale.ENGLISH, format6, LaneDetectHeight));

                SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("LaneDetectHeight", (float)LaneDetectHeight);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        final String format4 = "检测区域宽度: %.2f";
        tvDetectWidth.setText(String.format(Locale.ENGLISH, format4, DetectWidth));
        DetectWidthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(advanced_func_key==0 || ! LSTR.advancedKeyCheck(advanced_func_key)){
                    toast_msg("请获取高级权限");
                    set_advanced_key();
                    return;
                }
                DetectWidth = 1.8f* i / 100.f + 0.2f ;
                tvDetectWidth.setText(String.format(Locale.ENGLISH, format4, DetectWidth));

                SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("DetectWidth", (float)DetectWidth);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        final String format5 = "城市检测距离: %.2f";
        tvCityDetectHeight.setText(String.format(Locale.ENGLISH, format5, CityDetectHeight));
        CityDetectHeightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                if(advanced_func_key==0 || ! LSTR.advancedKeyCheck(advanced_func_key)){
                    toast_msg("请获取高级权限");
                    set_advanced_key();
                    return;
                }
                CityDetectHeight =   i / 100.f   ;
                tvCityDetectHeight.setText(String.format(Locale.ENGLISH, format5, CityDetectHeight));
                if(road_type!=2){
                    road_type = 2;
                    show_road_type();
                }

                SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("CityDetectHeight", (float)CityDetectHeight);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        final String format3 = "报警时间间隔: %d S";
        tvAlarmWait.setText(String.format(Locale.ENGLISH, format3, alarm_wait_time));
        alarmWaitSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                alarm_wait_time = i ;
                tvAlarmWait.setText(String.format(Locale.ENGLISH, format3, alarm_wait_time));

                SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("alarm_wait_time", (float)alarm_wait_time);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        tvNMNThreshold.setText(String.format(Locale.ENGLISH, format,nms_threshold, threshold));
        nmsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                nms_threshold = i / 100.f;
                tvNMNThreshold.setText(String.format(Locale.ENGLISH, format, nms_threshold,threshold));

                SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("nms_threshold", (float)nms_threshold);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        thresholdSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                threshold = i / 100.f;
                tvNMNThreshold.setText(String.format(Locale.ENGLISH, format, nms_threshold,threshold));

                SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("threshold", (float)threshold);
                editor.commit();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        TextView road_text = findViewById(R.id.txtRoadType);
        int soundid;
        switch (road_type){
            case 1:
                road_text.setText("高速");
                break;
            case 2:
                road_text.setText("城市");
                break;
            case 3:
                road_text.setText("郊外");
                break;
        }
        tvInfo.setLongClickable(true);
        tvInfo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(tvInfo.getTextSize() <=16){
                    tvInfo.setTextSize(20);
                }else{
                    tvInfo.setTextSize(14);
                }

                return true;
            }
        });
        btnUltra_fast.setLongClickable(true);
        btnUltra_fast.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                long time_now = SystemClock.elapsedRealtime();
                while(detectCamera.get()){
                    if(SystemClock.elapsedRealtime() - time_now >3000){
                        toast_msg("设置失败，等待图像处理完毕过长" );
                        return true;
                    }
                }
                detectCamera.set(true);
                USE_YOLOV4_DETECT ++;
                if(USE_YOLOV4_DETECT >4){
                    USE_YOLOV4_DETECT = 0;
                }
                if(USE_YOLOV4_DETECT == 0){
                    NcnnYolox.loadModel(getAssets(),1, USE_GPU?1:0);
                    toast_msg("使用 NcnnYolox 检测，准确度最高，速度较快 " );
                }else if(USE_YOLOV4_DETECT <=3) {

                    switch (USE_YOLOV4_DETECT){
                        case 1:
                            YOLOv4.init(getAssets(), 0, USE_GPU);
                            toast_msg("使用 yoloV4 Tiny 检测 ，较快 约 12FPS" );
                            break;
                        case 2:
                            YOLOv4.init(getAssets(), 1, USE_GPU);
                            toast_msg("使用 yolo-fastest 检测 ，很快 约 27FPS  " );
                            break;
                        case 3:
                            YOLOv4.init(getAssets(), 2, USE_GPU);
                            toast_msg("使用 mobilnetV2 + yolov3 检测，较快 约 25FPS " );
                            break;
                    }

                }else if(USE_YOLOV4_DETECT == 4){
                    YOLOv5.init(getAssets(), USE_GPU);
                    toast_msg("使用 yolov5 检测，准确度最高，但速度较慢 约 3FPS" );
                }
                detectCamera.set(false);
                return  true;
            }
        });
        btnUltra_fast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long time_now = SystemClock.elapsedRealtime();
                while(detectCamera.get()){
                    if(SystemClock.elapsedRealtime() - time_now >3000){
                        toast_msg("设置失败，等待图像处理完毕过长" );
                        return ;
                    }
                }

                ultra_fast_mode = !ultra_fast_mode;
                detectCamera.set(false);

                SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putBoolean("ultra_fast_mode", ultra_fast_mode);
                editor.commit();

                if(ultra_fast_mode){
                    toast_msg("开启极速识别模式，会周期进行图像识别，使得流畅度变高，不是每个图像都检测车道，会降低一点识别正确率");

                    btnUltra_fast.setText("极速识别：开启");
                    if(advanced_func_key != 0){
                        toast_msg("长按极速识别按钮可切换其它识别模型，识别速度会不同，但识别精度会变化");
                    }
                    //USE_YOLOV4_DETECT = 1;
                }else {
                    toast_msg("关闭极速识别");
                    btnUltra_fast.setText("极速识别");
                    //USE_YOLOV4_DETECT = 0;
                }
                //YOLOv4.init(getAssets(), USE_YOLOV4_DETECT, USE_GPU);
                //toast_msg("使用 yolo 检测 v"+ USE_YOLOV4_DETECT);
            }
            });

        btnScreenAutoOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screen_auto_off = !screen_auto_off ;
                SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putBoolean("screen_auto_off", screen_auto_off);
                editor.commit();
                if(screen_auto_off){
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    toast_msg("自动息屏 ：开, APP 可能会退出 ");
                    btnScreenAutoOff.setText("自动息屏 ：开");
                }else {
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    toast_msg("自动息屏 ：关，屏幕一直开启，降低亮度可以让机器减少发热和用电量 ");
                    btnScreenAutoOff.setText("一直显示");
                }

            }
        });
        btnHitPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hit_predict ++;//= !hit_predict;
                if(hit_predict >4){
                    hit_predict = 0;
                }
                SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putInt("hit_predict_i", hit_predict);

                editor.apply();
                show_hit_predict(true);

            }
            });
        btnDistance_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout edits= new LinearLayout(MainActivity.this);
                edits.setOrientation(LinearLayout.VERTICAL);
                final EditText edit = new EditText(MainActivity.this);
                edit.setText(String.valueOf(carmera_height));
                final EditText edit2 = new EditText(MainActivity.this);
                edit2.setText(String.valueOf(distance_fix));
                final EditText edit3 = new EditText(MainActivity.this);
                edit3.setText(String.valueOf(vertical_distance_rate));

                edits.addView(edit);
                edits.addView(edit2);
                edits.addView(edit3);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(edits);
                builder.setTitle( "车距测量参数，请输入摄像头高度(m)、距离修正系数、垂直测距（更精确）和水平测距占比：" );
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String carmera_height_s = edit.getText().toString();
                        String distance_fix_s = edit2.getText().toString();
                        String vertical_distance_rate_s = edit3.getText().toString();
                        SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                        SharedPreferences.Editor editor = userInfo.edit();

                        if(get_float(carmera_height_s)>0){
                            carmera_height = get_float(carmera_height_s);
                            editor.putFloat("carmera_height", carmera_height);
                        }
                        if(get_float(distance_fix_s)>0){
                            distance_fix = get_float(distance_fix_s);
                            editor.putFloat("distance_fix", distance_fix);
                        }
                        if(Math.abs(get_float(vertical_distance_rate_s))<=1){
                            vertical_distance_rate = get_float(vertical_distance_rate_s);
                            editor.putFloat("vertical_distance_rate_s", vertical_distance_rate);
                        }

                        editor.apply();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String openidt = edit.getText().toString();
                        String active_key = edit2.getText().toString();
                    }
                });
                builder.show();

            }
        });

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timenow = SystemClock.elapsedRealtime();
                TextView txt_mute = findViewById(R.id.txt_mute);
                mute_func_idx ++;
                if (mute_func_idx >2){
                    mute_func_idx = 0;
                }

                switch (mute_func_idx){
                    case 0:
                        mute_end = 0;
                        alarm_mode = last_alarm_mode;
                        show_alarm_mode(true);
                        play_sound(sound_on_voiceId);
                        break;
                    case 1:
                        mute_end = timenow + (long)120 *1000 ;

                        play_sound(mute_2min_voiceId);
                        txt_mute.setText("静音2分钟");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i=0 ;i<120;i++) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {

                                    }
                                    if(mute_func_idx!=1){
                                        return;
                                    }
                                }
                                if(mute_func_idx==1) {
                                    mute_end = 0;
                                    mute_func_idx = 0;
                                    alarm_mode = last_alarm_mode;
                                    show_alarm_mode(true);
                                    play_sound(sound_on_voiceId);
                                }
                            }
                        }).start();
                        last_alarm_mode = alarm_mode;
                        alarm_mode = 0;
                        break;
                    case 2:
                        mute_end = 1 ;
                        if(alarm_mode != 0){
                            last_alarm_mode = alarm_mode;
                        }
                        alarm_mode = 0;
                        show_alarm_mode(true);
                        play_sound(mute_voiceId);
                        break;
                }
            }
        });

        show_alarm_mode(true);

        btnRoadType.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if(advanced_func_key==0 || ! LSTR.advancedKeyCheck(advanced_func_key)){
                       toast_msg( "请获取高级权限，激活城市、郊外 道路提醒模式");
                       set_advanced_key();
                       return;
                   }
                   road_type ++;
                   if (road_type > 3){
                       road_type = 1;
                   }
                   show_road_type();
                   SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                   SharedPreferences.Editor editor = userInfo.edit();
                   editor.putInt("road_type", road_type);
                   editor.commit();
               }
           }
        );

        TextView auto_adjust_direction = findViewById(R.id.txt_auto_adjust_direction);
        switch (auto_adjust_detect_area){
            case 0:
                auto_adjust_direction.setText("无");
                break;
            case 1:
                auto_adjust_direction.setText("自动");
                break;
            case 2:
                auto_adjust_direction.setText("自动左右");
                break;
            case 3:
                auto_adjust_direction.setText("自动前后");
                break;
        }

        btnDirectionAutoAdjust.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   if(advanced_func_key==0 || ! LSTR.advancedKeyCheck(advanced_func_key)){
                       toast_msg( "请获取高级权限，激活自动道路方向适应模式，可以防止振动改变安装方向的微弱变化");
                       set_advanced_key();
                       return;
                   }
                   auto_adjust_detect_area ++;
                   if (auto_adjust_detect_area > 1){
                       auto_adjust_detect_area = 0;
                   }
                   TextView auto_adjust_direction = findViewById(R.id.txt_auto_adjust_direction);
                   int soundid;
                   switch (auto_adjust_detect_area){
                       case 0:
                           auto_adjust_direction.setText("无");
                           play_sound(direction_off_voiceId);
                           toast_msg("不自动适应道路方向，参数设为0 ");
                           direction_center_x_offset = 0;
                           break;
                       case 1:
                           auto_adjust_direction.setText("自动");
                           play_sound(direction_on_voiceId);
                           toast_msg( "自动道路方向适应模式，可以防止振动改变安装方向的微弱变化，40秒内道路连续报警会触发，大约20S调整一个像素");
                           break;
                       case 2:
                           auto_adjust_direction.setText("自动左右");
                           break;
                       case 3:
                           auto_adjust_direction.setText("自动前后");
                           break;
                   }

                   SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                   SharedPreferences.Editor editor = userInfo.edit();
                   editor.putInt("auto_adjust_detect_area", auto_adjust_detect_area);
                   editor.commit();
               }
           }
        );
        TextView txt_person_focus = findViewById(R.id.txt_person_focus);
        switch (person_detect_focus){
            case 0:
                txt_person_focus.setText("关闭");
                break;
            case 1:
                if(advanced_func_key !=0) {
                    far_enhanced_detect = true;
                }
                txt_person_focus.setText("开启");
                break;
        }

        btnPersonDetectFocus.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  toast_msg( "增强行人检测，在检测区域前方 左右侧附近的行人也会触发报警，特别适合夜间行车和通过人多路口");
                  if(advanced_func_key==0 || ! LSTR.advancedKeyCheck(advanced_func_key)){
                      set_advanced_key();
                      return;
                  }
                  person_detect_focus ++;
                  if (person_detect_focus > 1){
                      person_detect_focus = 0;
                  }

                  int soundid ;
                  switch (person_detect_focus){
                      case 0:
                          txt_person_focus.setText("关闭");
                          play_sound(person_focus_off_voiceId);
                          break;
                      case 1:
                          txt_person_focus.setText("开启");
                          far_enhanced_detect = true;
                          toast_msg( "已开启");
                          play_sound(person_focus_on_voiceId);
                          break;
                  }

                  SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                  SharedPreferences.Editor editor = userInfo.edit();
                  editor.putInt("person_detect_focus", person_detect_focus);
                  editor.commit();
              }
          }
        );

        btnInputSize.setLongClickable(true);
        btnInputSize.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        btnInputSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(advanced_func_key==0 || ! LSTR.advancedKeyCheck(advanced_func_key)){
                    toast_msg( "请获取高级权限，低分辨率，运算速度更快");
                    set_advanced_key();
                    return;
                }
                toast_msg("分辨率低，运算速度更快，反应越快，但可能降低一点识别准确率");

                input_size_idx ++ ;
                if (input_size_idx>5){
                    input_size_idx = 0;
                }
                SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putInt("input_size_idx", input_size_idx);
                editor.commit();
                startCamera();
            }});

        btnPhoto.setLongClickable(true);
        btnPhoto.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                USE_FAST_EXP = !USE_FAST_EXP;
                LSTR.setFastExp(USE_FAST_EXP);
                if(USE_FAST_EXP){
                    toast_msg("开启 FAST_EXP：检测道路更快");
                }else {
                    toast_msg("关闭 FAST_EXP：检测道路会慢点，但准确度更高一点点");
                }
                return true;
            }
        });
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                USE_DEBUG_PHOTO_ID ++;
                if(USE_DEBUG_PHOTO_ID>6){
                    USE_DEBUG_PHOTO_ID = 0;
                }
                /*int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            777
                    );
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_PICK_IMAGE);
                }*/
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                            checkCamera();
                                             toast_msg( "授予相机权限后，请完全关闭APP 重新打开");
                                         }
                                     });
        btnAdvanced_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_advanced_key();
            }
        });
        btnParam_setting.setLongClickable(true);
        btnParam_setting.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                USE_GPU = !USE_GPU;
                direction_center_x_offset = 0;
                SharedPreferences userInfo = MainActivity.mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("direction_center_x_offset", direction_center_x_offset);
                editor.putBoolean("USE_GPU", USE_GPU);
                editor.commit();
                toast_msg("隐藏参数已经重置,USE_GPU="+(USE_GPU?"True":"False"));
                return true;
            }
        });

        btnParam_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                param_toggle = !param_toggle;
                show_param();
            }
        });

        btnSwitch_alarm_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(advanced_func_key==0 || ! LSTR.advancedKeyCheck(advanced_func_key)){
                    toast_msg("请获取高级权限");
                    set_advanced_key();
                    return;
                }
                alarm_mode ++;
                if (alarm_mode >4){
                    alarm_mode = 0;
                }
                show_alarm_mode(true);
                if(alarm_mode==1){
                    play_sound(sound_on_voiceId);
                }

                /*if(alarm_mode!=0){
                    SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                    SharedPreferences.Editor editor = userInfo.edit();
                    editor.putInt("alarm_mode", alarm_mode);
                    editor.commit();
                }*/
            }
        });

        show_hit_predict(false);


/*
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            777
                    );
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("video/*");
                    startActivityForResult(intent, REQUEST_PICK_VIDEO);
                }
            }
        });
*/
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view_setting_lines = !view_setting_lines;
                far_enhanced_detect = true;
                view_setting();
                param_toggle = false;
                show_param();
                checkCamera();
            }
        });

        resultImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detectVideo.get() || detectPhoto.get()) {
                    detectPhoto.set(false);
                    detectVideo.set(false);
                    sbVideo.setVisibility(View.GONE);
                    sbVideoSpeed.setVisibility(View.GONE);
                    startCamera();
                }
            }
        });
/*
        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                updateTransform();
            }
        });

        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
*/
        sbVideoSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                videoSpeed = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this, "Video Speed:" + seekBar.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });

        sbVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                videoCurFrameLoc = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoCurFrameLoc = seekBar.getProgress();
            }
        });

        if (USE_MODEL == YOLOV4_TINY || USE_MODEL == LANE_LSTR) {
            btnPhoto.setVisibility(View.GONE);
            //btnVideo.setVisibility(View.GONE);
        }
        btnSetting.setVisibility(View.VISIBLE);


    }

    protected void initViewID() {
        toolbar = findViewById(R.id.tool_bar);
        resultImageView = findViewById(R.id.imageView);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);
        tvDetectWidth= findViewById(R.id.txtDetectWidth);
        DetectWidthSeekBar= findViewById(R.id.txtDetectWidthSeek);
        btnAdvanced_key= findViewById(R.id.advanced_btn);
        CityDetectHeightSeekBar = findViewById(R.id.txtCityDetectHeightSeek);
        tvCityDetectHeight = findViewById(R.id.txtCityDetectHeight);
        tvLaneDetectHeight  = findViewById(R.id.txtLaneDetectHeight);
        laneDetectHeightSeekBar  = findViewById(R.id.LaneDetectHeightSeek);
        btnMute = findViewById(R.id.mute_btn);
        iv_detect_input= findViewById(R.id.detect_input);

        iv_detect_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_detect_input.setVisibility(View.GONE);
            }
        });

        iv_detect_input.setLongClickable(true);
        iv_detect_input.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                far_enhanced_detect = false;
                if(!far_enhanced_detect){
                    toast_msg("远处增强检测已关闭，点击设置按钮重新开启");
                }

                return true;
            }
        });
        if(advanced_func_key!=0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    iv_detect_input.setVisibility(View.VISIBLE);
                }
            });
        }else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    iv_detect_input.setVisibility(View.GONE);
                }
            });
        }
        iv_lane_input= findViewById(R.id.lane_input);
        tvNMNThreshold = findViewById(R.id.valTxtView);
        tvFront = findViewById(R.id.txtFront);

        tvInfo = findViewById(R.id.tv_info);
        tvNMS = findViewById(R.id.txtNMS);
        tvThreshold = findViewById(R.id.txtThresh);
        nmsSeekBar = findViewById(R.id.nms_seek);

        thresholdSeekBar = findViewById(R.id.threshold_seek);
        frontSeekBar = findViewById(R.id.txtFront_seek);

        frontSeekBar = findViewById(R.id.txtFront_seek);
        alarmWaitSeekBar = findViewById(R.id.txtAlarm_time_seek);
        tvAlarmWait = findViewById(R.id.txtAlarm_time);

        btnCamera= findViewById(R.id.camera_btn);
        btnSwitch_alarm_mode= findViewById(R.id.switch_alarm_btn);
        btnParam_setting = findViewById(R.id.param_btn);
        btnDirectionAutoAdjust= findViewById(R.id.auto_adjust_direction_btn);
        btnPersonDetectFocus= findViewById(R.id.person_focus_btn);
        btnRoadType= findViewById(R.id.roadType_btn);
        btnPhoto = findViewById(R.id.button);
        btnInputSize = findViewById(R.id.input_size_btn);
        btnDistance_setting = findViewById(R.id.distance_setting_btn);
        btnHitPredict= findViewById(R.id.hit_predict_setting_btn);
        btnScreenAutoOff = findViewById(R.id.screen_off_btn);
        btnUltra_fast = findViewById(R.id.ultra_fast_btn);
        btnAbout = findViewById(R.id.about_btn);
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.check_app_update();
                Uri uri = Uri.parse("http://hayoou.com/safeapp");
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                i.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        btnZhangying = findViewById(R.id.zhangying_btn);
        btnZhangying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://hayoou.com/zy");
                Intent i = new Intent(Intent.ACTION_VIEW,uri);
                i.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        //btnVideo = findViewById(R.id.btn_video);
        btnSetting = findViewById(R.id.btn_setting);
        viewFinder = findViewById(R.id.view_finder);
        /*viewFinder = ((ViewStub) findViewById(R.id.view_finder))
                .inflate()
                .findViewById(R.id.object_detection_texture_view);*/
        sbVideo = findViewById(R.id.sb_video);
        sbVideo.setVisibility(View.GONE);
        sbVideoSpeed = findViewById(R.id.sb_video_speed);
        /*sbVideoSpeed.setMin(VIDEO_SPEED_MIN);
        sbVideoSpeed.setMax(VIDEO_SPEED_MAX);
        sbVideoSpeed.setVisibility(View.GONE);*/
    }

    protected void initModel() {
        if (USE_MODEL == YOLOV5S) {
            YOLOv5.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == YOLOV4_TINY) {
            YOLOv4.init(getAssets(), 0, USE_GPU);
        } else if (USE_MODEL == MOBILENETV2_YOLOV3_NANO) {
            YOLOv4.init(getAssets(), 1, USE_GPU);
        } else if (USE_MODEL == YOLO_FASTEST_XL) {
            YOLOv4.init(getAssets(), 2, USE_GPU);
        } else if (USE_MODEL == SIMPLE_POSE) {
            SimplePose.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == YOLACT) {
            Yolact.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == ENET) {
            ENet.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == FACE_LANDMARK) {
            FaceLandmark.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == DBFACE) {
            DBFace.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == MOBILENETV2_FCN) {
            MbnFCN.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == MOBILENETV3_SEG) {
            MbnSeg.init(getAssets(), USE_GPU);
        } else if (USE_MODEL == YOLOV5_CUSTOM_LAYER) {
            YOLOv5.initCustomLayer(getAssets(), USE_GPU);
        } else if (USE_MODEL == NANODET) {
            NanoDet.init(getAssets(), USE_GPU);
        }else if (USE_MODEL == YOLOX) {

        }

        if(CONFIG_MULTI_DETECT || USE_MODEL == LANE_LSTR){
            //YOLOv5.init(getAssets(), USE_GPU);
            NanoDet.init(getAssets(), USE_GPU);
            //YOLOv4.init(getAssets(), 0, false);
            NcnnYolox.loadModel(getAssets(),1, USE_GPU?1:0);
            LSTR.init(getAssets(), 0, USE_GPU);
        }
    }

    public static void toast_msg(String msg){
        mactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mcontext,msg,Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        // Compute the center of the view finder
        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;
        float height = viewFinder.getHeight();
        float width = viewFinder.getWidth();
        float[] rotations = {0, 90, 180, 270};
        // Correct preview output to account for display rotation
        float rotationDegrees = rotations[viewFinder.getDisplay().getRotation()];

        matrix.postRotate(270, centerX, centerY);
        matrix.postScale(
                width/1680,
                height/1920,
                centerX,
                centerY
        );
        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix);
    }


    private void startCamera() {
        CameraX.unbindAll();
        /*int numberOfCameras = CameraX.getCameraWithLensFacing();// 获取摄像头个数
        //遍历摄像头信息
        for (String cameraId = ""; cameraId < numberOfCameras; cameraId++) {
            CameraX.CameraInfo cameraInfo = CameraX.getCameraInfo(cameraId);
            CameraX.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == CameraX.CameraInfo.CAMERA_FACING_FRONT) {//前置摄像头
                mCamera = CameraX.open(cameraId);//打开摄像头
            }
        }*/
        /*
        Size screen = new Size(1920,1080); //size of the screen
        Rational aspectRatio = new Rational( 1920,1080);
        // 1. preview
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CAMERA_ID)
                .setTargetAspectRatio(aspectRatio)
//                .setTargetAspectRatio(Rational.NEGATIVE_INFINITY)  // 宽高比
                .setTargetResolution(screen)  // 分辨率
                .setTargetRotation(Surface.ROTATION_0)
                //.setTargetResolution(new Size(480, 640))  // 分辨率
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);

                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });
        CameraX.bindToLifecycle((LifecycleOwner) this, preview, gainAnalyzer(detectAnalyzer));
        */

        DetectAnalyzer detectAnalyzer = new DetectAnalyzer();
        CameraX.bindToLifecycle((LifecycleOwner) this, gainAnalyzer(detectAnalyzer));

    }

    private UseCase gainAnalyzer(DetectAnalyzer detectAnalyzer) {
        ImageAnalysisConfig.Builder analysisConfigBuilder = new ImageAnalysisConfig.Builder();
        analysisConfigBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);
        int[][] input_size_a = new int[][] { new int[] {640 ,480},new int[] {960 ,720},
                new int[] {1280 ,960},new int[] {1440 ,1080},new int[] {1920 ,1440},new int[] {2560 ,1920}};
        analysisConfigBuilder.setTargetResolution(new Size(input_size_a[input_size_idx][0], input_size_a[input_size_idx][1]));  // 输出预览图像尺寸
        ImageAnalysisConfig config = analysisConfigBuilder.build();
        ImageAnalysis analysis = new ImageAnalysis(config);
        analysis.setAnalyzer(detectAnalyzer);
        return analysis;
    }

    private Bitmap imageToBitmap(ImageProxy image) {
        byte[] nv21 = imagetToNV21(image);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public void show_alarm_mode(boolean notice){
        TextView txt_mute = findViewById(R.id.txt_mute);
        switch (alarm_mode){
            case 0:
                if(notice) {
                    toast_msg("设置为静音模式");
                }
                txt_mute.setText("静音");
                break;
            case 1:
                //play_alarm(1.0f);
                if(notice) {
                    toast_msg("提示音已经恢复");
                }
                //play_sound(sound_on_voiceId);
                txt_mute.setText("正常");
                break;
            case 2:
                if(notice) {
                    toast_msg("只关闭车道偏离警告提示音");
                }
                txt_mute.setText("关闭车道警告");
                break;
            case 3:
                if(notice) {
                    toast_msg("只关闭物体警告提示音");
                }
                txt_mute.setText("关闭物体警告");
                break;
            case 4:
                if(notice) {
                    toast_msg("只关闭前车起步提示音");
                }
                txt_mute.setText("关闭前车起步");
                break;
        }
    }

    private byte[] imagetToNV21(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ImageProxy.PlaneProxy y = planes[0];
        ImageProxy.PlaneProxy u = planes[1];
        ImageProxy.PlaneProxy v = planes[2];
        ByteBuffer yBuffer = y.getBuffer();
        ByteBuffer uBuffer = u.getBuffer();
        ByteBuffer vBuffer = v.getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        byte[] nv21 = new byte[ySize + uSize + vSize];
        // U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    private class DetectAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy image, final int rotationDegrees) {
            detectOnModel(image, rotationDegrees);
        }
    }


    private void detectOnModel(ImageProxy image, final int rotationDegrees) {
        if (detectCamera.get() || detectPhoto.get() || detectVideo.get()) {
            return;
        }
        frame_count ++;
        detectCamera.set(true);
        /*
        if(road_type == 3) {
            if (!ultra_fast_mode || ultra_fast_mode && frame_count % 2 == 1) {
                detectCamera.set(true);
            }
        }else if(!ultra_fast_mode || ultra_fast_mode && frame_count %3 == 2){
            detectCamera.set(true);
        }*/

        bitmapsrc = imageToBitmap(image);  // 格式转换
        //resizedBitmap = Bitmap.createScaledBitmap(bitmapsrc,  viewFinder.getWidth(), viewFinder.getHeight()+1, true);

        if (detectService == null) {
            detectCamera.set(false);
            return;
        }
        try {
            if(USE_DEBUG_PHOTO_ID>0){
                String filename = "03690.jpg";
                if(USE_DEBUG_PHOTO_ID>1){
                    filename = (USE_DEBUG_PHOTO_ID-1) + ".jpg";
                }
                //startY 320 ,height = 1230
                InputStream open = getAssets().open(filename);
                //白 tensor(2.2489, device='cuda:0') tensor(2.4286, device='cuda:0') tensor(2.6400, device='cuda:0')
                //black color tensor(-2.1179, device='cuda:0') tensor(-2.0357, device='cuda:0') tensor(-1.8044, device='cuda:0')
                //red color tensor(2.2318, device='cuda:0') tensor(-2.0357, device='cuda:0') tensor(-1.8044, device='cuda:0')
                //green olor tensor(-2.1179, device='cuda:0') tensor(2.4286, device='cuda:0') tensor(-1.7870, device='cuda:0')
                //blue olor tensor(-2.1179, device='cuda:0') tensor(-2.0357, device='cuda:0') tensor(2.6226, device='cuda:0')

                bitmapsrc = BitmapFactory.decodeStream(open);
                /*Color testc = bitmapsrc.getColor(1,1);
                float red = testc.red();
                float green = testc.green();
                float blue = testc.blue();*/
            }
        }catch (Exception e){}

        detectService.execute(new Runnable() {
            @Override
            public void run() {
                Matrix matrix = new Matrix();
                current_rotation_degree = rotationDegrees;

                if(rotationDegrees == 90){
                    matrix.postRotate(90);
                    width = bitmapsrc.getWidth();
                    height = bitmapsrc.getHeight();
                    Bitmap bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, width, height, matrix, true);
                    //Log.d("detectOnModel","rotationDegrees:"+rotationDegrees+" width:"+width+ " after width:"+bitmap.getWidth()+ " height:"+bitmap.getHeight());
                    detectAndDraw(bitmap,null,frame_count);
                    showResultOnUI();
                }else {

                    width = bitmapsrc.getWidth();
                    height = bitmapsrc.getHeight();

                    //Bitmap bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, 640, 480, matrix, false);
                    //Bitmap resizedBitmap1 = Bitmap.createScaledBitmap(bitmapsrc, viewFinder.getWidth(), viewFinder.getWidth() * height/width, true);

                    float detect_vertual_height = (float)width ;
                    //Bitmap resizedBitmap1 =Bitmap.createScaledBitmap(bitmapsrc, 640, 480, false);
                    Bitmap resizedBitmap1 = Bitmap.createBitmap(bitmapsrc,  0,2,width, height-2,null, false);
                    float lane_vertual_height = (float)width * 288/800;
                    /*
                    matrix.postRotate(0);
                    matrix.postScale(800/(float)width,288.f/lane_vertual_height);
                    boolean filter = false;
                    if(width < 800){
                        filter = true;
                    }
                    Bitmap lane_Bitmap = Bitmap.createBitmap(bitmapsrc,0 ,(int)(height - lane_vertual_height)/2
                            , width,  (int)lane_vertual_height, matrix, filter);
                    */
                    Bitmap lane_Bitmap = Bitmap.createBitmap(bitmapsrc,0 ,(int)(height - lane_vertual_height)/2
                            , width, (int)lane_vertual_height, null, false);
                    //Log.d("detectOnModel","rotationDegrees:"+rotationDegrees+" width:"+width+ " after width:"+resizedBitmap.getWidth()+ " height:"+resizedBitmap.getHeight());
                    //detectAndDraw(resizedBitmap1,lane_Bitmap,frame_count);
                    if(ultra_fast_mode) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                detectAndDraw(resizedBitmap1, lane_Bitmap, frame_count);
                                showResultOnUI();
                            }
                        }).start();
                    }else{
                        detectAndDraw(resizedBitmap1, lane_Bitmap, frame_count);
                        showResultOnUI();
                    }

                }



            }
        });
    }

    protected void showResultOnUI() {
        //detectCamera.set(false);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Bitmap resizedBitmap = Bitmap.createScaledBitmap(mutableBitmap, viewFinder.getWidth(), viewFinder.getHeight(), true);

                resultImageView.setImageBitmap(mutableBitmap);
                endTime = System.currentTimeMillis();
                long dur = endTime - startTime;
                startTime = endTime;
                float fps = (float) (1000.0 / dur);
                if(recent_fps <0.1){
                    recent_fps = fps;
                }else {
                    recent_fps = 0.95f * recent_fps + 0.05f * fps;
                }
                total_fps = (total_fps == 0) ? fps : (total_fps + fps);
                fps_count++;

                String modelName = getModelName();
                DateFormat df2 = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);
                DateFormat df8 = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.CHINA);
                String date2 = df2.format(new Date());
                String time4 = df8.format(new Date());
                BatteryManager manager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                /*manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);*/
                int battery_current =manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW); //BATTERY_PROPERTY_CURRENT_NOW
                int battery_persent = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);///当前电量百分比
                if(Math.abs(battery_current)>4000){
                    battery_current = battery_current / 1000;
                }
                String outmsg=String.format(Locale.CHINESE,
                        // 温度：%.1f ℃
                        "哈友安全驾驶\nV%s\n%s\n图像: %dx%d\n处理时间: \n%.3f s\nFPS: %.3f\n电量：%d %%\n电流：%d mA\n前方物体速度：%.1f m/s\n距离：%.1f m\n\n",
                        app_version,date2+"\n"+time4, width,height, dur / 1000.0, recent_fps,battery_persent,battery_current,near_object_speed,near_object_distance);
                //(float) total_fps / fps_count
                outmsg += detect_msg;
                tvInfo.setText(outmsg );
            }
        });
    }

    protected Bitmap drawDBFaceLandmark(Bitmap mutableBitmap, KeyPoint[] keyPoints) {
        if (keyPoints == null || keyPoints.length <= 0) {
            return mutableBitmap;
        }
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint keyPointPaint = new Paint();
        keyPointPaint.setAlpha(200);
        keyPointPaint.setStyle(Paint.Style.STROKE);
        keyPointPaint.setColor(Color.BLUE);
//        Log.d("wzt", "dbface size:" + keyPoints.length);
        for (int i = 0; i < keyPoints.length; i++) {
            // 其它随机颜色
            Random random = new Random(i + 2020);
            int color = Color.argb(255, random.nextInt(256), 125, random.nextInt(256));
            keyPointPaint.setColor(color);
            keyPointPaint.setStrokeWidth(9 * mutableBitmap.getWidth() / 800.0f);
            for (int j = 0; j < 5; j++) {
                canvas.drawPoint(keyPoints[i].x[j], keyPoints[i].y[j], keyPointPaint);
            }
            keyPointPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800.0f);
            canvas.drawRect(keyPoints[i].x0, keyPoints[i].y0, keyPoints[i].x1, keyPoints[i].y1, keyPointPaint);
        }
        return mutableBitmap;
    }

    protected Bitmap drawENetMask(Bitmap mutableBitmap, float[] results) {
        if (results == null || results.length <= 0) {
            return mutableBitmap;
        }
        // 0, "road" 1, "sidewalk" 2, "building" 3, "wall" 4, "fence" 5, "pole" 6, "traffic light" 7, "traffic sign" 8, "vegetation"
        // 9, "terrain" 10, "sky" 11, "person" 12, "rider" 13, "car" 14, "truck" 15, "bus" 16, "train" 17, "motorcycle" 18, "bicycle"
        int[][] cityspace_colormap = {
                {128, 64, 128}, {244, 35, 232}, {70, 70, 70}, {102, 102, 156}, {190, 153, 153}, {153, 153, 153},
                {250, 170, 30}, {220, 220, 0}, {107, 142, 35}, {152, 251, 152}, {70, 130, 180}, {220, 20, 60},
                {255, 0, 0}, {0, 0, 142}, {0, 0, 70}, {0, 60, 100}, {0, 80, 100}, {0, 0, 230}, {119, 11, 32}
        };
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint maskPaint = new Paint();
        maskPaint.setStyle(Paint.Style.STROKE);
        maskPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800.0f);
        maskPaint.setTextSize(30 * mutableBitmap.getWidth() / 800.0f);
        float mask = 0;
        int color = 0;
        float tempC = 0;
        int lengthW = 1;
        for (int y = 0; y < mutableBitmap.getHeight(); y++) {
            for (int x = 0; x < mutableBitmap.getWidth(); x++) {
                mask = results[y * mutableBitmap.getWidth() + x];
                if (mask >= cityspace_colormap.length) {
                    continue;
                }
//                color = Color.argb(255,
//                        cityspace_colormap[(int) mask][0],
//                        cityspace_colormap[(int) mask][1],
//                        cityspace_colormap[(int) mask][2]);
//                maskPaint.setColor(color);
//                maskPaint.setAlpha(100);
//                canvas.drawPoint(x, y, maskPaint);
                // fast
                if (mask != tempC) {
                    color = Color.argb(255,
                            cityspace_colormap[(int) tempC][0],
                            cityspace_colormap[(int) tempC][1],
                            cityspace_colormap[(int) tempC][2]);
                    maskPaint.setColor(color);
                    maskPaint.setAlpha(100);
                    canvas.drawLine(x - lengthW, y, x, y, maskPaint);
                    tempC = mask;
                    lengthW = 1;
                } else {
                    lengthW++;
                }
            }
            // fast
            color = Color.argb(255,
                    cityspace_colormap[(int) tempC][0],
                    cityspace_colormap[(int) tempC][1],
                    cityspace_colormap[(int) tempC][2]);
            maskPaint.setColor(color);
            maskPaint.setAlpha(100);
            canvas.drawLine(mutableBitmap.getWidth() - lengthW, y, mutableBitmap.getWidth(), y, maskPaint);
            tempC = mask;
            lengthW = 1;
        }
        return mutableBitmap;
    }

    protected Bitmap drawYolactMask(Bitmap mutableBitmap, YolactMask[] results) {
        if (results == null || results.length <= 0) {
            return mutableBitmap;
        }
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint maskPaint = new Paint();
        maskPaint.setAlpha(200);
        maskPaint.setStyle(Paint.Style.STROKE);
        maskPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800.0f);
        maskPaint.setTextSize(30 * mutableBitmap.getWidth() / 800.0f);
        maskPaint.setColor(Color.BLUE);
        for (YolactMask mask : results) {
            if (mask.prob < 0.4f) {
                continue;
            }
            int index = 0;
            char tempC = 0;
            int lengthW = 1;
            for (int y = 0; y < mutableBitmap.getHeight(); y++) {
                for (int x = 0; x < mutableBitmap.getWidth(); x++) {
//                    if (mask.mask[index] != 0) {
//                        maskPaint.setColor(mask.getColor());
//                        maskPaint.setAlpha(100);
//                        canvas.drawPoint(x, y, maskPaint);
//                    }
//                    index++;
                    // fast
                    if (mask.mask[index] != 0) {
                        if (mask.mask[index] != tempC) {
                            maskPaint.setColor(mask.getColor());
                            maskPaint.setAlpha(100);
                            canvas.drawLine(x - lengthW, y, x, y, maskPaint);
                            tempC = mask.mask[index];
                            lengthW = 1;
                        } else {
                            lengthW++;
                        }
                    } else if (lengthW > 1) {
                        maskPaint.setColor(mask.getColor());
                        maskPaint.setAlpha(100);
                        canvas.drawLine(x - lengthW, y, x, y, maskPaint);
                        tempC = mask.mask[index];
                        lengthW = 1;
                    }
                    index++;
                }
                // fast
                if (lengthW > 1) {
                    maskPaint.setColor(mask.getColor());
                    maskPaint.setAlpha(100);
                    canvas.drawLine(mutableBitmap.getWidth() - lengthW, y, mutableBitmap.getWidth(), y, maskPaint);
                    tempC = mask.mask[index - 1];
                    lengthW = 1;
                }
            }
            // 标签跟框放后面画，防止被 mask 挡住
            maskPaint.setColor(mask.getColor());
            maskPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(mask.getLabel() + String.format(Locale.CHINESE, " %.2f%%",
                    mask.getProb()), mask.left, mask.top - 15 * mutableBitmap.getWidth() / 1000.0f, maskPaint);
            maskPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(new RectF(mask.left, mask.top, mask.right, mask.bottom), maskPaint);

        }
        return mutableBitmap;
    }


    protected Bitmap drawBoxRects(Bitmap mutableBitmap, Box[] results,int lane_offset,int frame_count) {

        Canvas canvas ;
        try {
            /*Bitmap bitmap ;
            Matrix matrix = new Matrix();
            matrix.postRotate(0.0f);
            bitmap = Bitmap.createBitmap(mutableBitmap, 0, 0, mutableBitmap.getWidth(), mutableBitmap.getHeight(), matrix, true);
            */
            canvas = new Canvas(mutableBitmap);

        }catch (Exception e){
            //throw e;
            return resizedBitmap;
        }
        Distance distance = new Distance(canvas,results,lane_offset);
        SafeDetect.safe_region_param sp = get_safe_zone_detect(mutableBitmap.getWidth(),mutableBitmap.getHeight());
        msafeDetect.safeDetect(canvas,results,this,sp,distance,lane_offset,frame_count);

        if (!view_setting_lines &&(  results == null || results.length <= 0)) {
            return mutableBitmap;
        }

        float scaleX = viewFinder.getWidth() / 640;
        float scaleY = viewFinder.getHeight() / 640;
        float canvas_width = canvas.getWidth();
        float canvas_height = canvas.getHeight();
        float lane_scaleX = canvas_width /800;
        float lane_scaleY = lane_scaleX;
        float virtual_height = canvas_width * 288 / 800;
        float startY = (canvas_height - virtual_height) /2;

        final Paint boxPaint = new Paint();
        if(view_setting_lines){
            boxPaint.setAlpha(200);
            boxPaint.setStyle(Paint.Style.STROKE);
            boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800.0f);
            boxPaint.setColor(Color.argb(255,20,255,10));
            float horizon_line_Y = startY + virtual_height*(121)/288;
            canvas.drawRect(new RectF( 0 ,horizon_line_Y ,bitmapsrc.getWidth() ,
                    horizon_line_Y), boxPaint);
            canvas.drawRect(new RectF( (float)bitmapsrc.getWidth()/2 ,0 ,(float)bitmapsrc.getWidth()/2 ,
                    (float)bitmapsrc.getHeight()+1), boxPaint);
        }

        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(2 * mutableBitmap.getWidth() / 800.0f);
        boxPaint.setTextSize(18 * mutableBitmap.getWidth() / 800.0f);


        for (Box box : results) {
            if (USE_MODEL == MOBILENETV2_YOLOV3_NANO) {
                if (box.getScore() < 0.15f) {
                    // 模型比较小，置信度太低就不要了
                    continue;
                }
                // 有时候差太多了，手动改一下
                box.x0 = box.x0 < 0 ? box.x0 / 9 : box.x0;
                box.y0 = box.y0 < 0 ? box.y0 / 9 : box.y0;
            }
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            RectF rect = box.getRect();
            if (box.label <1000){
                int label = box.label;
                if(label>28 && !( label==56 || label==57 ) ){
                    continue;
                }
                canvas.drawText(box.getLabel() + String.format(Locale.CHINESE, " %d%%\n%2.1fm", (int)(box.getScore()*100),
                        distance.getDistance(box,results,sp,canvas,lane_offset)),
                        box.x0 + 3, box.y0 + 30 * canvas_width / 1000.0f, boxPaint);
                rect = box.getRect();
                boxPaint.setStyle(Paint.Style.STROKE);
                //canvas.drawRect(new RectF(rect.left*scaleX ,rect.top*scaleY ,rect.right*scaleX ,
                //       rect.bottom*scaleY ), boxPaint);
                canvas.drawRect(rect, boxPaint);
            }else{
                if(box.score<=0){
                    continue;
                }
                switch (box.label){
                    case 1000:
                        boxPaint.setColor(Color.argb(255,255,255,255));
                        break;
                    case 1001:
                        boxPaint.setColor(Color.argb(255,255,100,100));
                        break;
                    case 1002:
                        boxPaint.setColor(Color.argb(255,20,255,10));
                        break;
                    case 1003:
                        boxPaint.setColor(Color.argb(255,10,100,255));
                        break;
                }
                rect = box.getRect();

                rect.left *= lane_scaleX;
                rect.top *= lane_scaleY;
                rect.top += startY;
                canvas.drawCircle(rect.left,rect.top,3.5f * mutableBitmap.getWidth() / 800.0f, boxPaint);
            }
        }
        return mutableBitmap;
    }

    protected Bitmap drawFaceLandmark(Bitmap mutableBitmap, FaceKeyPoint[] keyPoints) {
        if (keyPoints == null || keyPoints.length <= 0) {
            return mutableBitmap;
        }
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint keyPointPaint = new Paint();
        keyPointPaint.setAlpha(200);
        keyPointPaint.setStyle(Paint.Style.STROKE);
        keyPointPaint.setStrokeWidth(8 * mutableBitmap.getWidth() / 800.0f);
        keyPointPaint.setColor(Color.BLUE);
//        Log.d("wzt", "facePoint length:" + keyPoints.length);
        for (int i = 0; i < keyPoints.length; i++) {
            // 其它随机颜色
            Random random = new Random(i / 106 + 2020);
            int color = Color.argb(255, random.nextInt(256), 125, random.nextInt(256));
            keyPointPaint.setColor(color);
            canvas.drawPoint(keyPoints[i].x, keyPoints[i].y, keyPointPaint);
        }
        return mutableBitmap;
    }

    protected Bitmap drawPersonPose(Bitmap mutableBitmap, KeyPoint[] keyPoints) {
        if (keyPoints == null || keyPoints.length <= 0) {
            return mutableBitmap;
        }
        // draw bone
        // 0 nose, 1 left_eye, 2 right_eye, 3 left_Ear, 4 right_Ear, 5 left_Shoulder, 6 rigth_Shoulder, 7 left_Elbow, 8 right_Elbow,
        // 9 left_Wrist, 10 right_Wrist, 11 left_Hip, 12 right_Hip, 13 left_Knee, 14 right_Knee, 15 left_Ankle, 16 right_Ankle
        int[][] joint_pairs = {{0, 1}, {1, 3}, {0, 2}, {2, 4}, {5, 6}, {5, 7}, {7, 9}, {6, 8}, {8, 10}, {5, 11}, {6, 12}, {11, 12}, {11, 13}, {12, 14}, {13, 15}, {14, 16}};
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint keyPointPaint = new Paint();
        keyPointPaint.setAlpha(200);
        keyPointPaint.setStyle(Paint.Style.STROKE);
        keyPointPaint.setColor(Color.BLUE);
        int color = Color.BLUE;
        // 画线、画框、画点
        for (int i = 0; i < keyPoints.length; i++) {
            // 其它随机颜色
            Random random = new Random(i + 2020);
            color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
            // 画线
            keyPointPaint.setStrokeWidth(5 * mutableBitmap.getWidth() / 800.0f);
            for (int j = 0; j < 16; j++) {  // 17个点连成16条线
                int pl0 = joint_pairs[j][0];
                int pl1 = joint_pairs[j][1];
                // 人体左侧改为红线
                if ((pl0 % 2 == 1) && (pl1 % 2 == 1) && pl0 >= 5 && pl1 >= 5) {
                    keyPointPaint.setColor(Color.RED);
                } else {
                    keyPointPaint.setColor(color);
                }
                canvas.drawLine(keyPoints[i].x[joint_pairs[j][0]], keyPoints[i].y[joint_pairs[j][0]],
                        keyPoints[i].x[joint_pairs[j][1]], keyPoints[i].y[joint_pairs[j][1]],
                        keyPointPaint);
            }
            // 画点
            keyPointPaint.setColor(Color.GREEN);
            keyPointPaint.setStrokeWidth(8 * mutableBitmap.getWidth() / 800.0f);
            for (int n = 0; n < 17; n++) {
                canvas.drawPoint(keyPoints[i].x[n], keyPoints[i].y[n], keyPointPaint);
            }
            // 画框
            keyPointPaint.setColor(color);
            keyPointPaint.setStrokeWidth(3 * mutableBitmap.getWidth() / 800.0f);
            canvas.drawRect(keyPoints[i].x0, keyPoints[i].y0, keyPoints[i].x1, keyPoints[i].y1, keyPointPaint);
        }
        return mutableBitmap;
    }

    protected void set_input_image(Bitmap image,Bitmap image2){
        if(view_setting_lines){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //iv_detect_input.setImageBitmap(image);
                    iv_lane_input.setImageBitmap(image2);
                }
            });
        }

    }

    protected Bitmap detectAndDraw(Bitmap image,Bitmap image2,int frame_count) {
        Box[] result = null;
        Box[] result1 = null;
        Box[] result2= null;

        KeyPoint[] keyPoints = null;
        YolactMask[] yolactMasks = null;
        FaceKeyPoint[] faceKeyPoints = null;
        float[] enetMasks = null;

        set_input_image(image,image2);
        /*
        if (USE_MODEL == YOLOV5S) {
            result = YOLOv5.detect(image, threshold, nms_threshold);
        } else if (USE_MODEL == YOLOV4_TINY || USE_MODEL == MOBILENETV2_YOLOV3_NANO || USE_MODEL == YOLO_FASTEST_XL) {
            result = YOLOv4.detect(image, threshold, nms_threshold);
        } else if (USE_MODEL == SIMPLE_POSE) {
            keyPoints = SimplePose.detect(image);
        } else if (USE_MODEL == YOLACT) {
            yolactMasks = Yolact.detect(image);
        } else if (USE_MODEL == ENET) {
            enetMasks = ENet.detect(image);
        } else if (USE_MODEL == FACE_LANDMARK) {
            faceKeyPoints = FaceLandmark.detect(image);
        } else if (USE_MODEL == DBFACE) {
            keyPoints = DBFace.detect(image, threshold, nms_threshold);
        } else if (USE_MODEL == MOBILENETV2_FCN) {
            enetMasks = MbnFCN.detect(image);
        } else if (USE_MODEL == MOBILENETV3_SEG) {
            enetMasks = MbnSeg.detect(image);
        } else if (USE_MODEL == YOLOV5_CUSTOM_LAYER) {
            result = YOLOv5.detectCustomLayer(image, threshold, nms_threshold);
        } else if (USE_MODEL == NANODET) {
            result = NanoDet.detect(image, threshold, nms_threshold);
        } else
            */
            if (USE_MODEL == LANE_LSTR) {
            if(( !ultra_fast_mode || ultra_fast_mode &&((road_type !=3 && frame_count %3 == 0 ) || (road_type ==3 && frame_count %2 == 0)))) {
                /*while(detectYolov4.get()){
                }
                detectYolov4.set(true);*/
                if (USE_YOLOV4_DETECT == 0){
                    result = NcnnYolox.detect(image, threshold, nms_threshold);
                    //result = NanoDet.detect(image, threshold, nms_threshold);
                }else if (USE_YOLOV4_DETECT <= 3) {
                    result = YOLOv4.detect(image, threshold, nms_threshold);
                } else if (USE_YOLOV4_DETECT == 4){
                    result = YOLOv5.detect(image, threshold, nms_threshold);
                    //result = NanoDet.detect(image, threshold, nms_threshold);
                }
                detectYolov4.set(false);
                detect_full_result = result;
            }else{
                result = detect_full_result;
            }
            //more far
            if(advanced_func_key!=0 && far_enhanced_detect &&
             ( !ultra_fast_mode || ultra_fast_mode &&((road_type !=3 && frame_count %3 == 1 ) || (road_type ==3 && frame_count %2 == 1)))
                ) {
                float cut_scale = 8.f;
                if (image.getWidth() <= 640){
                    cut_scale = 6.0f;
                }
                int small_box_half_width = (int)((float)image.getWidth() /cut_scale);
                int startX = (int)((float)image.getWidth() /2 - small_box_half_width) + (int)direction_center_x_offset;
                int startY = (int)((float)image.getHeight()/2 - small_box_half_width);
                // < 320 to scale with filter
                boolean filter = small_box_half_width *2 < 320;
                Bitmap image1 = Bitmap.createBitmap(image, startX,startY,small_box_half_width*2,small_box_half_width*2,null,filter);
                /*while(detectYolov4.get()){
                }
                detectYolov4.set(true);*/
                if (USE_YOLOV4_DETECT == 0){
                    result1 = NcnnYolox.detect(image1, threshold, nms_threshold);
                    //result = NanoDet.detect(image, threshold, nms_threshold);
                }else if (USE_YOLOV4_DETECT <= 3) {
                    result1 = YOLOv4.detect(image1, threshold, nms_threshold);
                } else if (USE_YOLOV4_DETECT == 4){
                    result1 = YOLOv5.detect(image1, threshold, nms_threshold);
                    //result = NanoDet.detect(image, threshold, nms_threshold);
                }
                detectYolov4.set(false);
                if(result1!=null) {
                    for (int i = 0; i < result1.length; i++) {
                        result1[i].x0 += startX;
                        result1[i].y0 += startY;
                        result1[i].x1 += startX;
                        result1[i].y1 += startY;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //iv_detect_input.setVisibility(View.VISIBLE);
                        iv_detect_input.setImageBitmap(image1);
                    }
                });
                detect_far_result = result1;
            }else{
                //iv_detect_input.setVisibility(View.GONE);
                result1 = detect_far_result;
            }

            if(road_type == 3) {
                lane_result = null;
                result2 = null;
                /*if(!ultra_fast_mode || ultra_fast_mode && frame_count %2==1) {
                    detectCamera.set(false);
                }*/
            }else {

                if ( (!ultra_fast_mode || ultra_fast_mode && frame_count % 3 == 2)
                ) {
                    result2 = LSTR.detect(image2, threshold, nms_threshold);
                    lane_result = result2;
                    //
                } else {
                    result2 = lane_result;
                }
            }


        }

        if (!view_setting_lines && result == null  && result1 == null && result2==null && keyPoints == null && yolactMasks == null && enetMasks == null && faceKeyPoints == null) {
            detectCamera.set(false);
            return image;
        }
        if (USE_MODEL == YOLOV5S || USE_MODEL == YOLOV4_TINY || USE_MODEL == MOBILENETV2_YOLOV3_NANO
                || USE_MODEL == YOLOV5_CUSTOM_LAYER || USE_MODEL == NANODET || USE_MODEL == YOLO_FASTEST_XL) {
            mutableBitmap = drawBoxRects(image, result,0,frame_count);

        } else if (USE_MODEL == SIMPLE_POSE) {
            mutableBitmap = drawPersonPose(image, keyPoints);
        } else if (USE_MODEL == YOLACT) {
            mutableBitmap = drawYolactMask(image, yolactMasks);
        } else if (USE_MODEL == ENET) {
            mutableBitmap = drawENetMask(image, enetMasks);
        } else if (USE_MODEL == FACE_LANDMARK) {
            mutableBitmap = drawFaceLandmark(image, faceKeyPoints);
        } else if (USE_MODEL == DBFACE) {
            mutableBitmap = drawDBFaceLandmark(image, keyPoints);
        } else if (USE_MODEL == MOBILENETV2_FCN) {
            mutableBitmap = drawENetMask(image, enetMasks);  // 与 enet 相同
        } else if (USE_MODEL == MOBILENETV3_SEG) {
            mutableBitmap = drawENetMask(image, enetMasks);  // 与 enet 相同
        }else if (USE_MODEL == LANE_LSTR) {

            /*int width = screen_width;
            int height = screen_width;
            if(resultImageView!=null){
                width = resultImageView.getWidth();
                height = resultImageView.getHeight();
            }

            //Bitmap bitmap = Bitmap.createBitmap( width, height, ARGB_8888, true);
            ArrayList<Box> result_all = new ArrayList<Box> ();
            for(Box b :result){
                result_all.add(b);
            }
            for(Box b :result2){
                result_all.add(b);
            }
            if (!view_setting_lines &&( result_all.size() <= 0)) {
                return mutableBitmap;
            }
            runOnUiThread(() -> {
                mResultView.setResults(result_all);
                mResultView.invalidate();
                mResultView.setVisibility(View.VISIBLE);
            });*/
            int all_count =0;
            if(result!=null){
                all_count += result.length;
            }
            if(result1!=null){
                all_count += result1.length;
            }
            if(result2!=null){
                all_count += result2.length;
            }

            Box[] result_all = new Box[all_count ];
            int result_all_i = 0;
            if(result!=null) {
                for (int i = 0; i < result.length; i++) {
                    result_all[result_all_i] = result[i];
                    result_all_i ++;
                }
            }
            if(result1!=null) {
                for (int i = 0; i < result1.length; i++) {
                    result_all[result_all_i] = result1[i];
                    result_all_i ++;
                }
            }
            int lane_offset = result_all_i;
            if(result2!=null) {
                for (int i = 0; i < result2.length; i++) {
                    result_all[result_all_i] = result2[i];
                    result_all_i ++;
                }
            }
            detectCamera.set(false);
            mutableBitmap = drawBoxRects(image, result_all,lane_offset,frame_count);
            //mutableBitmap = drawBoxRects(image, result1);
            //mutableBitmap = drawBoxRects(mutableBitmap, result2);  // 与 enet 相同


        }
        try {
            /*if(viewFinder.getDisplay().getRotation() == Surface.ROTATION_90){
                Matrix matrix = new Matrix();
                matrix.postRotate(270);
                width = mutableBitmap.getWidth();
                height = mutableBitmap.getHeight();
                mutableBitmap = Bitmap.createBitmap(mutableBitmap, 0, 0, width, height, matrix, false);
            }*/
        }catch (Exception e){}
        return mutableBitmap;
    }

    protected String getModelName() {
        String modelName = "ohhhhh";
        if (USE_MODEL == YOLOV5S) {
            modelName = "YOLOv5s";
        } else if (USE_MODEL == YOLOV4_TINY) {
            modelName = "YOLOv4-tiny";
        } else if (USE_MODEL == MOBILENETV2_YOLOV3_NANO) {
            modelName = "MobileNetV2-YOLOv3-Nano";
        } else if (USE_MODEL == SIMPLE_POSE) {
            modelName = "Simple-Pose";
        } else if (USE_MODEL == YOLACT) {
            modelName = "Yolact";
        } else if (USE_MODEL == ENET) {
            modelName = "ENet";
        } else if (USE_MODEL == FACE_LANDMARK) {
            modelName = "YoloFace500k-landmark106";
        } else if (USE_MODEL == DBFACE) {
            modelName = "DBFace";
        } else if (USE_MODEL == MOBILENETV2_FCN) {
            modelName = "MobileNetV2-FCN";
        } else if (USE_MODEL == MOBILENETV3_SEG) {
            modelName = "MBNV3-Segmentation-small";
        } else if (USE_MODEL == YOLOV5_CUSTOM_LAYER) {
            modelName = "YOLOv5s_Custom_Layer";
        } else if (USE_MODEL == NANODET) {
            modelName = "NanoDet";
        } else if (USE_MODEL == YOLO_FASTEST_XL) {
            modelName = "YOLO-Fastest-xl";
        }else if (USE_MODEL == LANE_LSTR) {
            modelName = "LANE_LSTR";
        }
        return USE_GPU ? "[ GPU ] " + modelName : "[ CPU ] " + modelName;
    }

    @Override
    protected void onDestroy() {
        detectCamera.set(false);
        detectVideo.set(false);
        if (detectService != null) {
            detectService.shutdown();
            detectService = null;
        }
        if (mmr != null) {
            mmr.release();
        }
        CameraX.unbindAll();
        super.onDestroy();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "请允许通过摄像头权限", Toast.LENGTH_SHORT).show();
                //this.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        if (requestCode == REQUEST_PICK_IMAGE) {
            // photo
            runByPhoto(requestCode, resultCode, data);
        } else if (requestCode == REQUEST_PICK_VIDEO) {
            // video
            runByVideo(requestCode, resultCode, data);
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public void runByPhoto(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(this, "Photo error", Toast.LENGTH_SHORT).show();
            return;
        }
        if (detectVideo.get()) {
            Toast.makeText(this, "Video is running", Toast.LENGTH_SHORT).show();
            return;
        }
        detectPhoto.set(true);
        final Bitmap image = getPicture(data.getData());
        if (image == null) {
            Toast.makeText(this, "Photo is null", Toast.LENGTH_SHORT).show();
            return;
        }
        CameraX.unbindAll();
        frame_count ++;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                mutableBitmap = image.copy(ARGB_8888, true);
                width = image.getWidth();
                height = image.getHeight();

                mutableBitmap = detectAndDraw(mutableBitmap,null,frame_count);

                final long dur = System.currentTimeMillis() - start;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String modelName = getModelName();
                        resultImageView.setImageBitmap(mutableBitmap);
                        tvInfo.setText(String.format(Locale.CHINESE, "%s\nSize: %dx%d\nTime: %.2f s\nFPS: %.1f",
                                modelName, height, width, dur / 1000.0, 1000.0f / dur));
                    }
                });
            }
        }, "photo detect");
        thread.start();
    }

    public void runByVideo(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(this, "Video error", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                // String imgNo = cursor.getString(0); // 编号
                String v_path = cursor.getString(1); // 文件路径
                String v_size = cursor.getString(2); // 大小
                String v_name = cursor.getString(3); // 文件名
                detectOnVideo(v_path);
            } else {
                Toast.makeText(this, "Video is null", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Video is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void detectOnVideo(final String path) {
        if (detectVideo.get()) {
            Toast.makeText(this, "Video is running", Toast.LENGTH_SHORT).show();
            return;
        }
        detectVideo.set(true);
        Toast.makeText(MainActivity.this, "FPS is not accurate!", Toast.LENGTH_SHORT).show();
        sbVideo.setVisibility(View.VISIBLE);
        sbVideoSpeed.setVisibility(View.VISIBLE);
        CameraX.unbindAll();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mmr = new FFmpegMediaMetadataRetriever();
                mmr.setDataSource(path);
                String dur = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);  // ms
                String sfps = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);  // fps
//                String sWidth = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);  // w
//                String sHeight = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);  // h
                String rota = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);  // rotation
                int duration = Integer.parseInt(dur);
                float fps = Float.parseFloat(sfps);
                float rotate = 0;
                if (rota != null) {
                    rotate = Float.parseFloat(rota);
                }
                sbVideo.setMax(duration * 1000);
                float frameDis = 1.0f / fps * 1000 * 1000 * videoSpeed;
                videoCurFrameLoc = 0;
                frame_count ++;
                while (detectVideo.get() && (videoCurFrameLoc) < (duration * 1000)) {
                    videoCurFrameLoc = (long) (videoCurFrameLoc + frameDis);
                    sbVideo.setProgress((int) videoCurFrameLoc);
                    final Bitmap b = mmr.getFrameAtTime(videoCurFrameLoc, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                    if (b == null) {
                        continue;
                    }
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotate);
                    width = b.getWidth();
                    height = b.getHeight();
                    final Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, width, height, matrix, false);
                    startTime = System.currentTimeMillis();
                    detectAndDraw(bitmap.copy(ARGB_8888, true),null,frame_count);
                    showResultOnUI();
                    frameDis = 1.0f / fps * 1000 * 1000 * videoSpeed;
                }
                mmr.release();
                if (detectVideo.get()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sbVideo.setVisibility(View.GONE);
                            sbVideoSpeed.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Video end!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                detectVideo.set(false);
            }
        }, "video detect");
        thread.start();
//        startCamera();
    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        if (bitmap == null) {
            return null;
        }
        int rotate = readPictureDegree(picturePath);
        return rotateBitmapByDegree(bitmap, rotate);
    }

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    @Override
    public void onBackPressed() {
        //双击返回退出App
        if (System.currentTimeMillis() - last_press_back_time > 2000) {

            //toast_msg( "Press again quit TekTok", Toast.LENGTH_SHORT).show();
            last_press_back_time = System.currentTimeMillis();
            view_setting_lines = false;
            view_setting();
            param_toggle = false;
            show_param();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * letterbox (slow)
     *
     * @param srcBitmap
     * @param srcWidth
     * @param srcHeight
     * @param dstWidth
     * @param dstHeight
     * @param matrix
     * @return
     */
    public static Bitmap letterbox(Bitmap srcBitmap, int srcWidth, int srcHeight, int dstWidth, int dstHeight, Matrix matrix) {
        long timeStart = System.currentTimeMillis();
        float scale = Math.min((float) dstWidth / srcWidth, (float) dstHeight / srcHeight);
        int nw = (int) (srcWidth * scale);
        int nh = (int) (srcHeight * scale);
        matrix.postScale((float) nw / srcWidth, (float) nh / srcHeight);
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth, srcHeight, matrix, false);
        Bitmap newBitmap = Bitmap.createBitmap(dstWidth, dstHeight, ARGB_8888);//创建和目标相同大小的空Bitmap
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        // 针对绘制bitmap添加抗锯齿
        PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        paint.setFilterBitmap(false);  // 对Bitmap进行滤波处理
        paint.setAntiAlias(true);  // 设置抗锯齿
        canvas.setDrawFilter(pfd);
        canvas.drawBitmap(bitmap, null,
                new Rect((dstHeight - nh) / 2, (dstWidth - nw) / 2,
                        (dstHeight - nh) / 2 + nh, (dstWidth - nw) / 2 + nw),
                paint);
        long timeDur = System.currentTimeMillis() - timeStart;
//        Log.d(TAG, "letterbox time:" + timeDur);
        return newBitmap;
    }


    public void set_advanced_key( ) {
        try {
            Random random = new Random( 2020);
            /*for(int i =0;i< 1000;i++){
                long rand= random.nextInt() & 0xFFFFFF ;
                long rand2= (random.nextInt() & 0xFFFF) << 24 ;
                long test = LSTR.getAdvancedKey(rand );
                Log.d("get_advanced_key",rand + " out " + (test | rand2));
            }*/
            LinearLayout edits= new LinearLayout(MainActivity.this);
            edits.setOrientation(LinearLayout.VERTICAL);
            final EditText edit = new EditText(MainActivity.this);
            edit.setText(openid);
            final EditText edit2 = new EditText(MainActivity.this);
            if(advanced_func_key!=0){
                edit2.setText(String.valueOf(advanced_func_key));
            }else {
                edit2.setText(String.valueOf(""));
            }

            edits.addView(edit);
            edits.addView(edit2);
            //String t = String.valueOf( new Date().getTime())+String.valueOf( (new Random().nextInt(10000)));
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(edits);
            builder.setTitle( "高级功能，赞助获取激活码。 微信： youkpan ，请输入用户名和激活码：" );
            builder.setPositiveButton("激活", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String openidt = edit.getText().toString();
                    String active_key = edit2.getText().toString();

                    if(openidt.equals("") ){
                        toast_msg("请输入用户名以便注册");
                        return;
                    }
                    if(openidt.length()< 6 ){
                        toast_msg("用户名过短");
                        return;
                    }
                    if(openidt.length()>30 ){
                        toast_msg("用户名过长");
                        return;
                    }

                    openid = openidt;

                    if(active_key.equals("")){
                        toast_msg("无效激活码");
                        return;
                    }
                    long testkey ;
                    try {
                        testkey = Long.parseLong(active_key);
                    }catch (Exception e){
                        toast_msg("无效激活码");
                        return;
                    }
                    boolean isok = LSTR.advancedKeyCheck(testkey);
                    if(active_key.equals("") || ! isok){
                        toast_msg("无效激活码");
                        return;
                    }

                    if (isok){

                        advanced_func_key = testkey;
                        toast_msg("激活完成，用户注册成功，激活码已配置");
                        SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                        SharedPreferences.Editor editor = userInfo.edit();
                        editor.putString("openid", openid);
                        editor.putLong("advanced_func_key", advanced_func_key);
                        long key_create_time = new Date().getTime();
                        editor.putLong("key_create_time", key_create_time);
                        editor.commit();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv_detect_input.setVisibility(View.VISIBLE);
                            }
                        });
                        /*
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                OkHttpClient client = new OkHttpClient();

                                try {
                                    String url = "http://hayoou.com/safedriving/check_key.php?openid="+openid+"&key="+testkey+"&deviceid="+deviceid;
                                    Request request = new Request.Builder()
                                            .url(url)
                                            .build();

                                    try (Response response = client.newCall(request).execute()) {
                                        String resp = response.body().string();
                                        if (resp.equals("ok")){
                                            advanced_func_key = testkey;
                                            toast_msg("激活完成，用户注册成功，激活码已配置");
                                            SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = userInfo.edit();
                                            editor.putString("openid", openid);
                                            editor.putLong("advanced_func_key", advanced_func_key);
                                            long key_create_time = new Date().getTime();
                                            editor.putLong("key_create_time", key_create_time);
                                            editor.commit();
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    iv_detect_input.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        }else if(resp.equals("max active count")){
                                            toast_msg("激活失败，激活次数过多");
                                        }else if(resp.equals("error key")){
                                            toast_msg("激活失败，激活码已被使用");
                                        }else if(resp.equals("expire key")){
                                            toast_msg("激活失败，激活码已过期");
                                        }else{
                                            toast_msg("激活失败，未知错误，请检查网络状态");
                                        }

                                    }

                                }catch(Exception e){
                                    toast_msg("激活码检测失败，请检查网络状态");
                                }
                            }
                        }).start();

                         */
                    }

                    dialog.cancel();
                }
            });
            builder.setNeutralButton("取消", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //Utils.openbrowser(Config.USER_PAGE_URL_PREFIX+"&from=ss&openid="+ Config.openid);
                    dialog.cancel();
                    //Toast.makeText(Config.activity,"Reported this comment ");
                }
            });
            builder.show();

        }catch (Exception e){}
    }

    public void view_setting(){

        if(view_setting_lines){
            //TextView FPS_info = findViewById(R.id.FPS_info);
            tvInfo.setVisibility(View.VISIBLE);
            //viewFinder.setVisibility(View.VISIBLE);
            iv_detect_input.setVisibility(View.GONE);
            iv_lane_input.setVisibility(View.VISIBLE);
            mResultView.setVisibility(View.GONE);
            btnPhoto.setVisibility(View.VISIBLE);
            btnInputSize.setVisibility(View.VISIBLE);
            //btnDistance_setting.setVisibility(View.VISIBLE);
            toast_msg("请将绿色水平参考线对准地平线，垂直线对准前方道路中间。");

            btnZhangying.setVisibility(View.VISIBLE);
            btnAbout.setVisibility(View.VISIBLE);

            int permission = ActivityCompat.checkSelfPermission(mcontext, Manifest.permission.CAMERA);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                btnCamera.setVisibility(View.VISIBLE);
            }

            btnAdvanced_key.setVisibility(View.VISIBLE);
            btnSwitch_alarm_mode.setVisibility(View.VISIBLE);
            btnParam_setting.setVisibility(View.VISIBLE);

            btnRoadType.setVisibility(View.VISIBLE);
            TextView textView = findViewById(R.id.txtRoadType);
            textView.setVisibility(View.VISIBLE);

            if(LSTR.advancedKeyCheck(advanced_func_key)){
//                btnAdvanced_key.setVisibility(View.GONE);
            }

            LinearLayout left_func_area = findViewById(R.id.left_func_area);
            left_func_area.setVisibility(View.GONE);

        }else{
            USE_DEBUG_PHOTO_ID=0;
            viewFinder.setVisibility(View.GONE);
            //tvInfo.setVisibility(View.GONE);

            if(advanced_func_key!=0) {
                iv_detect_input.setVisibility(View.VISIBLE);
            }else {
                iv_detect_input.setVisibility(View.GONE);
            }
            iv_lane_input.setVisibility(View.GONE);
            mResultView.setVisibility(View.GONE);
            btnPhoto.setVisibility(View.GONE);
            btnInputSize.setVisibility(View.GONE);
            btnDistance_setting.setVisibility(View.GONE);
            tvNMS.setVisibility(View.GONE);
            tvThreshold.setVisibility(View.GONE);
            nmsSeekBar.setVisibility(View.GONE);
            thresholdSeekBar.setVisibility(View.GONE);
            tvNMNThreshold.setVisibility(View.GONE);

            frontSeekBar.setEnabled(false);
            tvFront.setVisibility(View.GONE);
            frontSeekBar.setVisibility(View.GONE);

            btnZhangying.setVisibility(View.GONE);
            btnAbout.setVisibility(View.GONE);

            tvAlarmWait.setVisibility(View.GONE);
            alarmWaitSeekBar.setVisibility(View.GONE);
            btnCamera.setVisibility(View.GONE);
            btnAdvanced_key.setVisibility(View.GONE);

            tvDetectWidth.setVisibility(View.GONE);
            DetectWidthSeekBar.setVisibility(View.GONE);

            btnSwitch_alarm_mode.setVisibility(View.GONE);
            btnParam_setting.setVisibility(View.GONE);

            LinearLayout left_func_area = findViewById(R.id.left_func_area);
            left_func_area.setVisibility(View.VISIBLE);

            CityDetectHeightSeekBar.setEnabled(false);
            CityDetectHeightSeekBar.setVisibility(View.GONE);
            tvCityDetectHeight.setVisibility(View.GONE);

            laneDetectHeightSeekBar.setVisibility(View.GONE);
            tvLaneDetectHeight.setVisibility(View.GONE);
        }

    }

    public void show_road_type(){
        TextView road_text = findViewById(R.id.txtRoadType);
        int soundid;
        switch (road_type){
            case 1:
                road_text.setText("高速");
                alarm_mode = 1;
                far_enhanced_detect = true;
                toast_msg( "高速模式，检测更远，车道偏离预警启动");
                play_sound(highway_voiceId);
                break;
            case 2:
                road_text.setText("城市");
                alarm_mode = 1;
                far_enhanced_detect = true;
                toast_msg( "城市模式，其他车辆左右进入，加塞，不需要非常严格判断非常远，判断近距离即可");
                play_sound(cityroad_voiceId);
                break;
            case 3:
                road_text.setText("郊外");
                alarm_mode = 2;
                far_enhanced_detect = true;
                //person_detect_focus = 1;
                toast_msg( "郊外模式，人车少的地方，加强对周边区域判断，判断范围更宽，判断距离更远，关闭车道识别");
                play_sound(outskirts_voiceId);
                break;
        }

        SharedPreferences userInfo = mcontext.getSharedPreferences("adas", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();
        editor.putInt("road_type", road_type);
        editor.commit();
        show_alarm_mode(false);
    }

    public void show_param(){

        if(param_toggle){
            nmsSeekBar.setEnabled(true);
            thresholdSeekBar.setEnabled(true);
            tvNMS.setVisibility(View.VISIBLE);
            tvThreshold.setVisibility(View.VISIBLE);
            nmsSeekBar.setVisibility(View.VISIBLE);
            thresholdSeekBar.setVisibility(View.VISIBLE);
            tvNMNThreshold.setVisibility(View.VISIBLE);
            tvAlarmWait.setVisibility(View.VISIBLE);
            alarmWaitSeekBar.setVisibility(View.VISIBLE);
            DetectWidthSeekBar.setEnabled(true);
            tvDetectWidth.setVisibility(View.VISIBLE);
            DetectWidthSeekBar.setVisibility(View.VISIBLE);
            frontSeekBar.setEnabled(true);
            tvFront.setVisibility(View.VISIBLE);
            frontSeekBar.setVisibility(View.VISIBLE);
            CityDetectHeightSeekBar.setEnabled(true);
            CityDetectHeightSeekBar.setVisibility(View.VISIBLE);
            tvCityDetectHeight.setVisibility(View.VISIBLE);
            tvLaneDetectHeight.setVisibility(View.VISIBLE);
            laneDetectHeightSeekBar.setVisibility(View.VISIBLE);
            laneDetectHeightSeekBar.setEnabled(true);
            btnDistance_setting.setVisibility(View.VISIBLE);
            btnHitPredict.setVisibility(View.VISIBLE);
            btnScreenAutoOff.setVisibility(View.VISIBLE);
            btnUltra_fast.setVisibility(View.VISIBLE);
        }else {
            btnUltra_fast.setVisibility(View.GONE);
            btnScreenAutoOff.setVisibility(View.GONE);
            btnHitPredict.setVisibility(View.GONE);
            nmsSeekBar.setEnabled(false);
            thresholdSeekBar.setEnabled(false);
            tvNMS.setVisibility(View.GONE);
            tvThreshold.setVisibility(View.GONE);
            nmsSeekBar.setVisibility(View.GONE);
            thresholdSeekBar.setVisibility(View.GONE);
            tvNMNThreshold.setVisibility(View.GONE);
            tvAlarmWait.setVisibility(View.GONE);
            alarmWaitSeekBar.setVisibility(View.GONE);
            DetectWidthSeekBar.setEnabled(false);
            tvDetectWidth.setVisibility(View.GONE);
            DetectWidthSeekBar.setVisibility(View.GONE);
            frontSeekBar.setEnabled(false);
            tvFront.setVisibility(View.GONE);
            frontSeekBar.setVisibility(View.GONE);
            btnDistance_setting.setVisibility(View.GONE);
            btnRoadType.setVisibility(View.VISIBLE);
            TextView textView = findViewById(R.id.txtRoadType);
            textView.setVisibility(View.VISIBLE);

            CityDetectHeightSeekBar.setEnabled(false);
            CityDetectHeightSeekBar.setVisibility(View.GONE);
            tvCityDetectHeight.setVisibility(View.GONE);

            tvLaneDetectHeight.setVisibility(View.GONE);
            laneDetectHeightSeekBar.setVisibility(View.GONE);
        }

    }

    public void init_param(){
        SharedPreferences sharedPreferences = getSharedPreferences("adas", MODE_PRIVATE);
        //SharedPreferences sharedPreferences = PreferenceManager.getSharedPreferences("adas",mcontext /* Activity context */);
        threshold = sharedPreferences.getFloat("threshold", (float)threshold);
        nms_threshold = sharedPreferences.getFloat("nms_threshold", (float)nms_threshold);
        LaneDetectHeight = sharedPreferences.getFloat("LaneDetectHeight", (float)LaneDetectHeight);
        front_detect = sharedPreferences.getFloat("front_detect", (float)front_detect);
        //direction_center_x_offset = sharedPreferences.getFloat("direction_center_x_offset", (float)direction_center_x_offset);
        alarm_wait_time = (int)sharedPreferences.getFloat("alarm_wait_time", (float)alarm_wait_time);
        advanced_func_key = sharedPreferences.getLong("advanced_func_key", advanced_func_key);
        long key_create_time1 = new Date().getTime();
        key_create_time = sharedPreferences.getLong("key_create_time", key_create_time1);
        if(key_create_time1 == key_create_time ){
            SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
            SharedPreferences.Editor editor = userInfo.edit();
            editor.putLong("key_create_time", key_create_time1);
            editor.apply();
        }
        input_size_idx = sharedPreferences.getInt("input_size_idx", input_size_idx);
        DetectWidth = sharedPreferences.getFloat("DetectWidth", DetectWidth);
        alarm_mode = sharedPreferences.getInt("alarm_mode", alarm_mode);
        road_type = sharedPreferences.getInt("road_type", road_type);
        auto_adjust_detect_area = sharedPreferences.getInt("auto_adjust_detect_area", auto_adjust_detect_area);
        person_detect_focus = sharedPreferences.getInt("person_detect_focus", person_detect_focus);
        openid = sharedPreferences.getString("openid", openid);
        deviceid = sharedPreferences.getString("deviceid", deviceid);
        USE_GPU = sharedPreferences.getBoolean("USE_GPU", USE_GPU);
        carmera_height = sharedPreferences.getFloat("carmera_height", carmera_height);
        distance_fix = sharedPreferences.getFloat("distance_fix", distance_fix);
        vertical_distance_rate = sharedPreferences.getFloat("vertical_distance_rate", vertical_distance_rate);
        hit_predict = sharedPreferences.getInt("hit_predict_i", hit_predict);

        ultra_fast_mode= sharedPreferences.getBoolean("ultra_fast_mode", ultra_fast_mode);
        if(deviceid.equals("")){
            long deviceid_gen = ((new Date().getTime())) << 16 |  (SystemClock.elapsedRealtime()&0xFFFF);
            deviceid = String.valueOf(deviceid_gen);
            SharedPreferences userInfo = getSharedPreferences("adas", MODE_PRIVATE);
            SharedPreferences.Editor editor = userInfo.edit();
            editor.putString("deviceid", deviceid);
            editor.apply();
        }
    }

    public void show_hit_predict(boolean msgout){

        if(hit_predict==1){
            if(msgout)
            toast_msg("高级碰撞预测 ：开,提前 3.1 秒预警，根据前方物体速度和前方物体距离，请设置好测距参数");
            btnHitPredict.setText("碰撞预测 ：3.1秒");
            hit_predict_time = 3.1f;
        }else if(hit_predict==2){
            if(msgout)
            toast_msg("高级碰撞预测 ：开,提前 2.5 秒预警，根据前方物体速度和前方物体距离，请设置好测距参数");
            btnHitPredict.setText("碰撞预测 ：2.5秒");
            hit_predict_time = 2.5f;
        }else if(hit_predict==3){
            if(msgout)
            toast_msg("高级碰撞预测 ：开,提前 2 秒预警，根据前方物体速度和前方物体距离，请设置好测距参数");
            btnHitPredict.setText("碰撞预测 ：2秒");
            hit_predict_time = 2f;
        }else if(hit_predict==4){
            if(msgout)
            toast_msg("高级碰撞预测 ：开,提前 1.5 秒预警，根据前方物体速度和前方物体距离，请设置好测距参数");
            btnHitPredict.setText("碰撞预测 ：1.5秒");
            hit_predict_time = 1.5f;
        }else {
            if(msgout)
            toast_msg("高级碰撞预测 ：关");
            btnHitPredict.setText("碰撞预测 ：关");
        }
    }

    public static float get_float(String s){
        if(s.equals("")){
            return 0;
        }
        try {
            return Float.valueOf(s);
        }catch (Exception e){
            return  0;
        }
    }
}
