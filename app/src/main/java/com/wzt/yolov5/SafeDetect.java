package com.wzt.yolov5;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.Log;

import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static com.wzt.yolov5.MainActivity.CityDetectHeight;
import static com.wzt.yolov5.MainActivity.DetectWidth;
import static com.wzt.yolov5.MainActivity.LaneDetectHeight;
import static com.wzt.yolov5.MainActivity.alarm_mode;
import static com.wzt.yolov5.MainActivity.auto_adjust_detect_area;
import static com.wzt.yolov5.MainActivity.detect_msg;
import static com.wzt.yolov5.MainActivity.device_temperature;
import static com.wzt.yolov5.MainActivity.frame_count;
import static com.wzt.yolov5.MainActivity.front_car_start_voiceId;
import static com.wzt.yolov5.MainActivity.mactivity;
import static com.wzt.yolov5.MainActivity.person_detect_focus;
import static com.wzt.yolov5.MainActivity.recent_fps;
import static com.wzt.yolov5.MainActivity.reset_notice_start_time;
import static com.wzt.yolov5.MainActivity.rest_2hour_voiceId;
import static com.wzt.yolov5.MainActivity.road_type;
import static com.wzt.yolov5.MainActivity.sys_start_time;
import static com.wzt.yolov5.MainActivity.toohot_alarm_time;
import static com.wzt.yolov5.MainActivity.toohot_voiceId;
import static com.wzt.yolov5.MainActivity.ultra_fast_mode;

public class SafeDetect {

    public static float left_line_a = 5.57f;//960f * 0.52f / (0.07f * 1280f) ;
    public static float right_line_a = -left_line_a;//960f * 0.52f / (0.07f * 1280f) ;
    public static float direction_center_x_offset = 0;
    public static float last_alarm_time ;
    public static float last_offset_change_time ;
    private boolean alarming = false;
    private int alarm_count = 0;
    final int DETECT_ALARM_LEFT = 0;
    final int DETECT_ALARM_RIGHT = 1;
    final int DETECT_ALARM_FRONT = 2;
    final int LANE_ALARM_LEFT = 1;
    final int LANE_ALARM_RIGHT = 2;
    private long [] last_detect_alert_time = new long[]{0,0,0,0};
    private long [] last_detect_alert_count = new long[]{0,0,0,0};
    private long [] last_lane_alert_time = new long[]{0,0,0,0};
    private long [] last_lane_alert_count = new long[]{0,0,0,0};
    boolean isWaitingTrafficLight = false;
    long WaitingTrafficLightTime = 0;
    long TrafficClearTime = 0;
    int front_car_start_state =0 ;
    long last_alarm_front_car_start_time = 0;
    boolean TrafficClear = true;
    long TrafficLightDetectTime = 0;
    Box last_alarm_obj = null;
    Box last_detect_alarm_obj = null;
    long last_detect_alarm_time = 0;
    public static int hit_predict = 0;
    public static float hit_predict_time = 3.1f;
    public static float near_object_speed = 0f;
    public static float near_object_distance = 0f;
    public static boolean screen_auto_off = false;

    protected void draw_safe_zone(Canvas canvas,safe_region_param sp) {
        final Paint linePaint = new Paint();

        float canvas_width = canvas.getWidth();
        float canvas_height = canvas.getHeight();

        linePaint.setAlpha(200);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3 * canvas.getWidth() / 800.0f);
        linePaint.setColor(Color.argb(180,20,190,10));
/*
        float line1_X0 = canvas_width/2f - 0.035f*canvas_width + direction_center_x_offset;
        float line1_Y0 = canvas_height*0.52f;

        float line1_X1 = canvas_width/2f + 0.035f*canvas_width + direction_center_x_offset;
        float line2_Y1 = canvas_height*0.6f;
*/
        //safe_region_param sp = get_safe_zone_detect((int)canvas_width,(int)canvas_height);

        canvas.drawLine(sp.safe_region_x[1], sp.safe_region_y[1],
                sp.safe_region_x[2],sp.safe_region_y[2], linePaint);
        //left
        canvas.drawLine(sp.safe_region_x[0],sp.safe_region_y[0],
                sp.safe_region_x[1], sp.safe_region_y[1], linePaint);
        //right
        canvas.drawLine(sp.safe_region_x[2],sp.safe_region_y[2],
                sp.safe_region_x[3],sp.safe_region_y[3], linePaint);

        //道路冲突
        linePaint.setColor(Color.argb(200, 231, 91, 18));
        canvas.drawLine(sp.safe_region_x[12], sp.safe_region_y[12],
                sp.safe_region_x[13],sp.safe_region_y[13], linePaint);

        if(road_type == 2 ) {
            linePaint.setColor(Color.argb(160, 240, 160, 160));
            //城市模式
            canvas.drawLine(sp.safe_region_x[4], sp.safe_region_y[4],
                    sp.safe_region_x[5], sp.safe_region_y[5], linePaint);
            //left
            canvas.drawLine(sp.safe_region_x[0], sp.safe_region_y[0],
                    sp.safe_region_x[4], sp.safe_region_y[4], linePaint);
            //rightif(box.score<=0){
            //                    continue;
            //                }
            canvas.drawLine(sp.safe_region_x[5], sp.safe_region_y[5],
                    sp.safe_region_x[3], sp.safe_region_y[3], linePaint);

            linePaint.setColor(Color.argb(160, 200, 200, 200));

            //城市模式
            canvas.drawLine(sp.safe_region_x[8], sp.safe_region_y[8],
                    sp.safe_region_x[9], sp.safe_region_y[9], linePaint);

        }

        if(person_detect_focus ==1 || road_type == 3) {
            linePaint.setColor(Color.argb(160, 150, 190, 10));
            //行人增强 和 郊外模式
            canvas.drawLine(sp.safe_region_x[6], sp.safe_region_y[6],
                    sp.safe_region_x[7], sp.safe_region_y[7], linePaint);
            //left
            canvas.drawLine(sp.safe_region_x[10] , sp.safe_region_y[10],
                    sp.safe_region_x[6], sp.safe_region_y[6], linePaint);
            //right
            canvas.drawLine(sp.safe_region_x[7], sp.safe_region_y[7],
                    sp.safe_region_x[11], sp.safe_region_y[11], linePaint);
        }

    }

    public void alarm_box(Canvas canvas,int alarm_front,int alarm_left,int alarm_right){
        final Paint linePaint = new Paint();
        float canvas_width = canvas.getWidth();
        float canvas_height = canvas.getHeight();

        linePaint.setAlpha(200);
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setStrokeWidth(4 * canvas.getWidth() / 800.0f);
        linePaint.setColor(Color.argb(255,230,10,10));

        if (alarm_front ==1){
            linePaint.setColor(Color.argb(255,230,10,10));
            canvas.drawRect(0 , 0,
                     canvas.getWidth(),0.1f*canvas.getHeight(), linePaint);
        }

        if (alarm_left ==1){
            linePaint.setColor(Color.argb(255,230,10,10));
            canvas.drawRect(0 , 0,
                    0.1f*canvas.getWidth(),canvas.getHeight(), linePaint);
        }

        if (alarm_right ==1){
            linePaint.setColor(Color.argb(255,230,10,10));
            canvas.drawRect(0.9f*canvas.getWidth() , 0,
                    canvas.getWidth(),canvas.getHeight(), linePaint);
        }

    }

    private void fix_direction_offset(int x,int y,float limit_offset_x){
        long timenow = SystemClock.elapsedRealtime();
        if(     (timenow - last_alarm_time < 30000)  &&
                (timenow - last_offset_change_time > 20000) &&
                (auto_adjust_detect_area == 1 || auto_adjust_detect_area == 2)){

            last_offset_change_time = timenow;
            if(Math.abs(direction_center_x_offset + x)<limit_offset_x){
                direction_center_x_offset += x ;
/*
                SharedPreferences userInfo = MainActivity.mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("direction_center_x_offset", direction_center_x_offset);
                editor.commit();
*/
            }
        }
    }

    public void safeDetect(Canvas canvas, Box[] results, MainActivity main,safe_region_param sp,Distance distance,int lane_offset,int frame_count) {
        long time_now = SystemClock.elapsedRealtime();
        draw_safe_zone(canvas,sp);
        float check_detect_time = 0.f;
        float check_lane_time = 1.2f;
        float limit_offset_x = 0.07f * canvas.getWidth();
        if(ultra_fast_mode){
            check_lane_time = 0;
            check_detect_time = 0;
        }else {
            if (road_type == 2) {
                check_lane_time = 1.8f;
            } else if (road_type == 3) {
                check_lane_time = 2.3f;
            }
        }
        TrafficClear = true;
        if(time_now - reset_notice_start_time > 7200000){
            ((MainActivity) mactivity).play_sound(rest_2hour_voiceId);
            reset_notice_start_time = time_now;
        }
        if(device_temperature > 70 && time_now - toohot_alarm_time > 60000){
            ((MainActivity) mactivity).play_sound(toohot_voiceId);
            toohot_alarm_time = time_now;
        }

        int detect_result = in_safe_zone(results, canvas.getWidth(), canvas.getHeight(), false,sp);
        if(hit_predict>0) {
            int near_objct_detect_result = near_objct_in_safe_zone(results, canvas.getWidth(), canvas.getHeight(), false, sp);
            if (near_objct_detect_result>=0 ) {
                boolean calc_enable = !ultra_fast_mode ||
                        (ultra_fast_mode && (road_type!=3 && frame_count %3 ==2 || road_type==3 && frame_count %2 ==1)) ;

                if (calc_enable && last_detect_alarm_time != 0 && last_detect_alarm_obj != null && last_alarm_obj != null ) {
                    float distance1 = distance.getDistance(last_detect_alarm_obj, results, sp, canvas, lane_offset);
                    float distance2 = distance.getDistance(last_alarm_obj, results, sp, canvas, lane_offset);
                    float near_object_speed1 = (distance1 - distance2) / ((float) (time_now - last_detect_alarm_time) / 1000);
                    //if(Math.abs(distance1 - distance2) > 0.001f) {
                        near_object_speed = 0.6f * near_object_speed + 0.4f * near_object_speed1;
                        near_object_distance = distance2;
                        float judge_time = hit_predict_time;
                        /*if (road_type != 1 && ! ultra_fast_mode ){
                            judge_time = hit_predict_time - 0.5f ;
                        }*/
                        if ((distance2 / near_object_speed) < judge_time && distance2 > 1 &&
                                near_object_speed > 1 && near_object_speed1 > 1) {

                            if (alarm_mode != 0 && time_now - last_alarm_time > 2000) {
                                main.play_alarm3();
                                last_alarm_time = time_now;
                                MainActivity.detect_msg = "小心前方，可能碰撞";
                                alarm_box(canvas, 1, 0, 0);
                            }
                        }
                    //}
                }

                if (last_detect_alarm_obj == null || calc_enable) {
                    last_detect_alarm_time = time_now;
                    last_detect_alarm_obj = last_alarm_obj;
                }

            }
        }


        if (detect_result > 0) {

            switch (detect_result) {
                case 1:

                    if (last_detect_alert_time[DETECT_ALARM_LEFT] == 0) {
                        last_detect_alert_time[DETECT_ALARM_LEFT] = time_now;
                    }

                    last_detect_alert_count[DETECT_ALARM_LEFT] += 1;

                    break;
                case 2:

                    if (last_detect_alert_time[DETECT_ALARM_RIGHT] == 0) {
                        last_detect_alert_time[DETECT_ALARM_RIGHT] = time_now;
                    }
                    last_detect_alert_count[DETECT_ALARM_RIGHT] += 1;

                    break;
                case 3:
                    if (last_detect_alert_time[DETECT_ALARM_FRONT] == 0) {
                        last_detect_alert_time[DETECT_ALARM_FRONT] = time_now;
                    }
                    last_detect_alert_count[DETECT_ALARM_FRONT] += 1;

                    break;
                case 11:

                    if (last_lane_alert_time[LANE_ALARM_LEFT] == 0) {
                        last_lane_alert_time[LANE_ALARM_LEFT] = time_now;
                    }
                    last_lane_alert_count[LANE_ALARM_LEFT]++;

                    break;
                case 12:
                    if (last_lane_alert_time[LANE_ALARM_RIGHT] == 0) {
                        last_lane_alert_time[LANE_ALARM_RIGHT] = time_now;
                    }
                    last_lane_alert_count[LANE_ALARM_RIGHT]++;

                    break;
                default:
                    MainActivity.detect_msg = "未知";
                    break;
            }
            /*
            if(SystemClock.elapsedRealtime() - last_alarm_time >MainActivity.alarm_wait_time * 1000){

                if(!alarming && alarm_count <2){
                    last_alarm_time = SystemClock.elapsedRealtime();
                    alarming = true;
                    alarm_count ++;
                    main.play_alarm();
                }
            }*/

        } else {

            //每次超时 1.2秒 如果未能检测到告警，就清空
            for (int i = 0; i < 3; i++) {
                if ((last_detect_alert_time[i] != 0) &&
                        (time_now - last_detect_alert_time[i] > check_detect_time * 1000) &&
                        (last_detect_alert_count[i] < recent_fps * check_detect_time * 0.3)
                ) {
                    last_detect_alert_count[i] = 0;
                    last_detect_alert_time[i] = 0;
                    //main.stop_alarm();
                }
            }

            for (int i = 1; i < 3; i++) {
                if ((last_lane_alert_time[i] != 0) &&
                        (time_now - last_lane_alert_time[i] > check_lane_time * 1000) &&
                        (last_lane_alert_count[i] < recent_fps * check_lane_time * 0.5)
                ) {
                    last_lane_alert_count[i] = 0;
                    last_lane_alert_time[i] = 0;
                    main.stop_alarm3();
                }
            }
/*
            if(SystemClock.elapsedRealtime() - last_alarm_time >1200 && (alarming||alarm_count>0)) {
                alarm_count = 0;
                alarming = false;
                main.pause_alarm();
            }

 */
            if (SystemClock.elapsedRealtime() - last_alarm_time > 20000) {
                MainActivity.detect_msg = "";
            }
        }

        //每次超时 n 秒 如果检测到告警，就清空
        for (int i = 0; i < 3; i++) {
            if ((last_detect_alert_time[i] != 0) &&
                    (time_now - last_detect_alert_time[i] >= check_detect_time * 1000) &&
                    (last_detect_alert_count[i] > recent_fps * check_detect_time * 0.3)
            ) {
                last_detect_alert_count[i] = 0;
                last_detect_alert_time[i] = 0;
                if ((alarm_mode == 1 || alarm_mode == 2) &&
                        (time_now - last_alarm_time > MainActivity.alarm_wait_time * 1000)) {
                    last_alarm_time = time_now;

                    if (road_type == 1) {
                        main.play_alarm(1.5f);
                    } else if (road_type == 2) {
                        main.play_alarm(1.f);
                    } else if (road_type == 3) {
                        main.play_alarm(1.5f);
                    }
                }

                switch (i) {
                    case DETECT_ALARM_LEFT:
                        MainActivity.detect_msg = "左边物体进入行驶区域";
                        alarm_box(canvas, 0, 1, 0);
                        break;
                    case DETECT_ALARM_RIGHT:
                        MainActivity.detect_msg = "右边边物体进入行驶区域";
                        alarm_box(canvas, 0, 0, 1);
                        break;
                    case DETECT_ALARM_FRONT:
                        MainActivity.detect_msg = "前方物体进入行驶区域";
                        alarm_box(canvas, 1, 0, 0);
                        break;
                }

            }
        }

        for (int i = 1; i < 3; i++) {
            if ((last_lane_alert_time[i] != 0) &&
                    (time_now - last_lane_alert_time[i] > check_lane_time * 1000) &&
                    (last_lane_alert_count[i] > recent_fps * check_lane_time * 0.6)
            ) {
                last_lane_alert_count[i] = 0;
                last_lane_alert_time[i] = 0;
                //城市道路，遇到前方有车，车道识别不清
                if(!(road_type==2 && ! TrafficClear)) {
                    switch (i) {
                        case LANE_ALARM_LEFT:
                            if ((alarm_mode == 1 || alarm_mode == 3) &&
                                    (time_now - last_alarm_time > MainActivity.alarm_wait_time * 1000)) {
                                last_alarm_time = time_now;

                                if (road_type == 1) {
                                    main.play_alarm3();
                                } else if (road_type == 2) {
                                    main.play_alarm1(1.f);
                                } else if (road_type == 3) {
                                    main.play_alarm1(1f);
                                }
                            }

                            MainActivity.detect_msg = "车道偏离：\n前方靠近左侧车道";
                            fix_direction_offset(1, 0, limit_offset_x);
                            alarm_box(canvas, 0, 1, 0);
                            break;
                        case LANE_ALARM_RIGHT:
                            if ((alarm_mode == 1 || alarm_mode == 3) &&
                                    (time_now - last_alarm_time > MainActivity.alarm_wait_time * 1000)) {
                                last_alarm_time = time_now;
                                if (road_type == 1) {
                                    main.play_alarm(1.5f);
                                } else if (road_type == 2) {
                                    main.play_alarm1(1.f);
                                } else if (road_type == 3) {
                                    main.play_alarm1(1f);
                                }
                            }

                            MainActivity.detect_msg = "车道偏离：\n前方靠近右侧车道";
                            fix_direction_offset(-1, 0, limit_offset_x);
                            alarm_box(canvas, 0, 0, 1);
                            break;
                    }
                }
            }
        }
        if(road_type ==2) {
            if (front_car_start_state == 1) {

                if (TrafficClear && time_now - WaitingTrafficLightTime > 6000 &&
                        TrafficClearTime == 0) {
                    TrafficClearTime = time_now;
                    front_car_start_state = 2;
                }
                if (TrafficClear && time_now - WaitingTrafficLightTime < 5000) {
                    TrafficClearTime = 0;
                    front_car_start_state = 0;
                    WaitingTrafficLightTime = 0;
                    isWaitingTrafficLight = false;
                }
            }

            if (front_car_start_state == 2) {
                if (TrafficClear && TrafficClearTime != 0 && time_now - TrafficClearTime > 50 &&
                        time_now - TrafficClearTime < 3000 &&
                        time_now - WaitingTrafficLightTime < 60000 &&
                        time_now - last_alarm_front_car_start_time > 60000
                ) {
                    WaitingTrafficLightTime = 0;
                    isWaitingTrafficLight = false;
                    last_alarm_front_car_start_time = time_now;
                    TrafficClearTime = 0;
                    front_car_start_state = 0;
                    ((MainActivity) mactivity).play_sound(front_car_start_voiceId);
                }

                if (TrafficClearTime != 0 && time_now - TrafficClearTime > 3000) {
                    WaitingTrafficLightTime = 0;
                    isWaitingTrafficLight = false;
                    TrafficClearTime = 0;
                    front_car_start_state = 0;
                    //last_alarm_front_car_start_time = 0;
                }
            }


            if (WaitingTrafficLightTime != 0 && time_now - WaitingTrafficLightTime > 60000) {
                WaitingTrafficLightTime = 0;
                isWaitingTrafficLight = false;
            }
            if (TrafficLightDetectTime != 0 && time_now - TrafficLightDetectTime > 30000) {
                TrafficLightDetectTime = 0;
            }
        }
    }

    public static class safe_region_param{
        float [] safe_region_x;
        float [] safe_region_y;
        float [] slope;
        float [] bias;
    }

    public static safe_region_param get_safe_zone_detect(int imgwidth, int imgheight){
        //long start_time = SystemClock.elapsedRealtime();
        safe_region_param sp = new safe_region_param();
        //高速（左下，左上，右上，右下） ，4-5 城市（左上，右上），6--7 行人增强（左上，右上）, 8-9前车起步（左，右）
        //10 - 11 前车起步检测 ，12-13 道路冲突检测（左上，右上）
        sp.safe_region_x = new float[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        sp.safe_region_y = new float[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        sp.slope = new float[] {0,0,0,0,0,0};
        sp.bias = new float[] {0,0,0,0,0,0};

        float y_offset =  (float)(MainActivity.front_detect-0.5)* (- 0.35f) * imgheight;

        float road_x_width_half = 0.005f*imgwidth * DetectWidth;

        sp.safe_region_x[0] = (float)imgwidth/2f - 0.3f*imgwidth * DetectWidth;
        sp.safe_region_y[0] = (float)imgheight*0.9f + y_offset;

        sp.safe_region_x[1] = (float)imgwidth/2f - road_x_width_half + direction_center_x_offset*imgwidth/128;
        sp.safe_region_y[1] = (float)0.52f*imgheight + y_offset;

        sp.safe_region_x[2] = (float)imgwidth/2f + road_x_width_half + direction_center_x_offset*imgwidth/128;
        sp.safe_region_y[2] = sp.safe_region_y[1];//(float)0.52f*imgheight + y_offset;

        sp.safe_region_x[3] = (float)imgwidth/2f + 0.3f*imgwidth * DetectWidth;
        sp.safe_region_y[3] = sp.safe_region_y[0];//(float)imgheight*0.9f + y_offset;

        //直线参数
        sp.slope[0] = (sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        sp.bias[0] = sp.safe_region_y[0] - sp.slope[0] * sp.safe_region_x[0];
        sp.slope[1] = (sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        sp.bias[1] = sp.safe_region_y[2] - sp.slope[1] * sp.safe_region_x[2];

        //城市模式 水平线 X1
        sp.safe_region_y[4] = sp.safe_region_y[1] + (float)imgheight*(  ( 1- CityDetectHeight  )*0.4f)  ;
        sp.safe_region_x[4] = (sp.safe_region_y[4] - sp.bias[0])/sp.slope[0] ;// (float)imgwidth/2f - 0.1f*imgwidth * DetectWidth + direction_center_x_offset;
        //城市模式 水平线 X2
        //sp.safe_region_x[5] = (float)imgwidth/2f + 0.1f*imgwidth * DetectWidth + direction_center_x_offset;
        sp.safe_region_y[5] = sp.safe_region_y[4];//sp.safe_region_y[1] + (float)imgheight*(  (1 - CityDetectHeight  )*0.4f)  ;
        sp.safe_region_x[5] = (sp.safe_region_y[5] - sp.bias[1])/sp.slope[1] ;

        //城市模式 直线参数
        sp.slope[2] = (sp.safe_region_y[4] - sp.safe_region_y[0]) / (sp.safe_region_x[4] - sp.safe_region_x[0]);
        sp.bias[2] = sp.safe_region_y[0] - sp.slope[2] * sp.safe_region_x[0];
        sp.slope[3] = (sp.safe_region_y[3] - sp.safe_region_y[5]) / (sp.safe_region_x[3] - sp.safe_region_x[5]);
        sp.bias[3] = sp.safe_region_y[5] - sp.slope[3] * sp.safe_region_x[5];

        //行人检测增强
        sp.safe_region_x[6] = (float)imgwidth/2f - 0.15f*imgwidth * DetectWidth + direction_center_x_offset*imgwidth/128;
        sp.safe_region_y[6] = (float)imgheight*0.4f + y_offset;
        //行人检测增强 水平线 X2
        sp.safe_region_x[7] = (float)imgwidth/2f + 0.15f*imgwidth * DetectWidth + direction_center_x_offset*imgwidth/128;
        sp.safe_region_y[7] = sp.safe_region_y[6];//(float)imgheight*0.4f + y_offset;

        float near_road_width = 2 * ((float)imgwidth /2 - sp.safe_region_x[0]);
        float startx = (float)imgwidth /2 - near_road_width ;
        if(startx <0 ){
            startx = 0;
        }
        float startx1 = (float)imgwidth /2 + near_road_width ;
        if(startx1 >(float)imgwidth ){
            startx1 = (float)imgwidth;
        }

        //行人检测增强
        sp.safe_region_x[10] = startx + direction_center_x_offset*imgwidth/128;
        sp.safe_region_y[10] = sp.safe_region_y[0];
        //行人检测增强 水平线 X2
        sp.safe_region_x[11] = startx1 + direction_center_x_offset*imgwidth/128;
        sp.safe_region_y[11] = sp.safe_region_y[0];

        //行人检测增强 直线参数
        sp.slope[4] =  (sp.safe_region_y[6] - sp.safe_region_y[10]) / (sp.safe_region_x[6] - sp.safe_region_x[10]);
        sp.bias[4] = sp.safe_region_y[10] - sp.slope[4] * (sp.safe_region_x[10]);
        sp.slope[5] =  (sp.safe_region_y[11] - sp.safe_region_y[7]) / (sp.safe_region_x[11] - sp.safe_region_x[7]);
        sp.bias[5] = sp.safe_region_y[11] - sp.slope[5] *  (sp.safe_region_x[11]);//sp.safe_region_x[7];

        //城市模式 等待红绿灯 ，前车进入检测 水平线
        sp.safe_region_y[8] = sp.safe_region_y[1] + (float)imgheight*((1-CityDetectHeight)*0.2f)  ;
        sp.safe_region_x[8] = (sp.safe_region_y[8] - sp.bias[0])/sp.slope[0] ;// (float)imgwidth/2f - 0.1f*imgwidth * DetectWidth + direction_center_x_offset;
        //城市模式 等待红绿灯 ，前车进入检测 水平线2
        //sp.safe_region_x[5] = (float)imgwidth/2f + 0.1f*imgwidth * DetectWidth + direction_center_x_offset;
        sp.safe_region_y[9] = sp.safe_region_y[8] ;//sp.safe_region_y[1] + (float)imgheight*((1-CityDetectHeight)*0.2f) ;
        sp.safe_region_x[9] = (sp.safe_region_y[8] - sp.bias[1])/sp.slope[1] ;

        //道路冲突检测
        sp.safe_region_y[12] = sp.safe_region_y[1] + (float)imgheight*(LaneDetectHeight*0.2f)  ;
        sp.safe_region_x[12] = (sp.safe_region_y[12] - sp.bias[0])/sp.slope[0] ;// (float)imgwidth/2f - 0.1f*imgwidth * DetectWidth + direction_center_x_offset;
        sp.safe_region_y[13] = sp.safe_region_y[12];//sp.safe_region_y[1] + (float)imgheight*(LaneDetectHeight*0.2f)  ;
        sp.safe_region_x[13] = (sp.safe_region_y[13] - sp.bias[1])/sp.slope[1] ;// (float)imgwidth/2f - 0.1f*imgwidth * DetectWidth + direction_center_x_offset;

        //Log.d("getparam",(float)(SystemClock.elapsedRealtime() - start_time)/1000 + " S");
        return sp;
    }


    public int in_safe_zone(Box[] bboxes,int imgwidth,int imgheight,boolean check_lane,safe_region_param sp) {

        //safe_region_param sp = get_safe_zone_detect(imgwidth, imgheight);

        float slope1 = sp.slope[0];//(sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        float bias1 = sp.bias[0];//sp.safe_region_y[0] - slope1 * sp.safe_region_x[0];
        float slope2 = sp.slope[1];//(sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        float bias2 = sp.bias[1];//sp.safe_region_y[2] - slope2 * sp.safe_region_x[2];
        //long time_now = SystemClock.elapsedRealtime();
        //float half_x = (float)imgwidth/2;
        float virtual_height = (float)imgwidth * 288 / 800;
        float startY = (imgheight - virtual_height) /2;
        float lane_scaleX = (float)imgwidth /800;
        float lane_scaleY = lane_scaleX;
        //Y 从上到下 ，变大

        for (int i = 0; i < bboxes.length; i++) {
            if (bboxes[i].label < 1000) {
                int label = bboxes[i].label;
                if(label>28 && !( label==56 || label==57 ) ){
                    continue;
                }
                //目标检测
                if (bboxes[i].getScore() > MainActivity.threshold || check_lane) {
                    float[] bboxes_xy = new float[]{bboxes[i].x0,bboxes[i].y0,bboxes[i].x1,bboxes[i].y1};
                    float center_x = (bboxes[i].x0 + bboxes[i].x1 )/2;
                    int out =0;

                    switch (road_type){
                        case 1:
                            out = high_way_detect(sp,imgwidth,imgheight,bboxes_xy,label);
                            break;
                        case 2:
                            out = city_road_detect(sp,imgwidth,imgheight,bboxes_xy,label);
                            break;
                        case 3:
                            out = out_skirt_detect(sp,imgwidth,imgheight,bboxes_xy,label);
                            break;
                    }

                    if(out >0){
                        return out;
                    }

                    //x0 y0,x0 y1,x1 y0..
                    //行人增强检测
                    if(person_detect_focus == 1 && label == 0){
                        for (int j = 0; j < 2; j++) {
                            for (int k = 0; k < 2; k++) {

                                float x = bboxes_xy[2*j];
                                //右下
                                float y = bboxes_xy[2*k+1];
                                //车内区域
                                if (y > sp.safe_region_y[0]){
                                    continue;
                                }

                                if (y > sp.safe_region_y[6]){
                                    float ys = sp.slope[4] * x + sp.bias[4];
                                    //在左边线右侧
                                    if (y > ys) {
                                        float ys_right = sp.slope[5] * x + sp.bias[5];
                                        //在右侧线左侧
                                        if (y > ys_right) {
                                            //last_alarm_obj = bboxes[i];
                                            if (center_x < sp.safe_region_x[6] ) {
                                                return 1;
                                            } else if (center_x > sp.safe_region_x[7] )  {
                                                return 2;
                                            }else {
                                                return 3;
                                            }
                                        }
                                    }
                                }
                            }


                        }
                    }
                }
            }else {
                //车道检测
                if (bboxes[i].score > 0 || check_lane) {
                    int label = bboxes[i].label;
                    if (label == 1000 || label == 1003){
                        continue;
                    }
                    //x0 y0,x0 y1,x1 y0..
                    float x = bboxes[i].x0;
                    //右下
                    float y = bboxes[i].y0;

                    x *= lane_scaleX;
                    y *= lane_scaleY;
                    y += startY;

                    if (y > sp.safe_region_y[12]) {
                        //ys = -0.89 * x + 826.7
                        float ys = slope1 * x + bias1;
                         if (y > ys) {
                            float ys_right = slope2 * x + bias2;
                            if (y > ys_right) {

                                    if (label == 1001) {
                                        //Log.d("SafeDetect", "lane in_safe_zone 1  " + x + "  " + y);
                                        return 11;
                                    } else {
                                        //Log.d("SafeDetect", "lane in_safe_zone 2  " + x + "  " + y);
                                        return 12;
                                    }

                            }
                        }
                    }
                }
            }
        }
        return  0;
    }

    public int near_objct_in_safe_zone(Box[] bboxes,int imgwidth,int imgheight,boolean check_lane,safe_region_param sp) {

        float virtual_height = (float)imgwidth * 288 / 800;
        float startY = (imgheight - virtual_height) /2;
        float lane_scaleX = (float)imgwidth /800;
        float lane_scaleY = lane_scaleX;
        //Y 从上到下 ，变大
        float min_y = imgheight;
        int min_idx = -1;
        for (int i = 0; i < bboxes.length; i++) {
            if (bboxes[i].label < 1000) {
                int label = bboxes[i].label;
                if (label > 28 && !(label == 56 || label == 57)) {
                    continue;
                }
                //目标检测
                if (bboxes[i].getScore() > MainActivity.threshold || check_lane) {
                    float[] bboxes_xy = new float[]{bboxes[i].x0, bboxes[i].y0, bboxes[i].x1, bboxes[i].y1};
                    //float center_x = (bboxes[i].x0 + bboxes[i].x1) / 2;
                    int out = 0;

                    out = high_way_detect(sp, imgwidth, imgheight, bboxes_xy, label);

                    if (out > 0 && bboxes_xy[3] < min_y) {
                        min_y = bboxes_xy[3];
                        min_idx = i;
                    }
                }
            }
        }

        if(min_idx!=-1){
            last_alarm_obj = bboxes[min_idx];
        }
        return min_idx;
    }

    public int high_way_detect(safe_region_param sp ,int imgwidth,int imgheight,float [] bboxes_xy,int label) {
        float slope1 = sp.slope[0];//(sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        float bias1 = sp.bias[0];//sp.safe_region_y[0] - slope1 * sp.safe_region_x[0];
        float slope2 = sp.slope[1];//(sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        float bias2 = sp.bias[1];//sp.safe_region_y[2] - slope2 * sp.safe_region_x[2];

        float half_x = (float) imgwidth / 2;

        float center_x = (bboxes_xy[0] + bboxes_xy[2]) / 2;

        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {

                float x = bboxes_xy[2 * j];
                //右下
                float y = bboxes_xy[2 * k + 1];
                //车内区域
                if (y > sp.safe_region_y[0]) {
                    continue;
                }

                //进入安全检测区域
                if (y > sp.safe_region_y[1]) {
                    //ys = -0.89 * x + 826.7
                    //左边线
                    float ys = slope1 * x + bias1;
                    // 左下点，在安全区外
                    //框框包括安全区
                    //右下也在安全区外
                    float x1 = bboxes_xy[2];
                    float y1 = bboxes_xy[3];
                    float ys_right1 = slope2 * x1 + bias2;
                    boolean in_front_but_point_not_in_area =
                            (j == 0 && k == 1 && y <= ys && x < half_x) &&
                            (y1 > sp.safe_region_y[1] && x1 > half_x) &&
                            (y1 < ys_right1) ;

                    boolean in_right_line_left = false;

                    //在左边线右侧
                    if (y > ys) {
                        float ys_right = slope2 * x + bias2;
                        //在右侧线左侧
                        in_right_line_left = (y > ys_right);
                    }

                    if (in_right_line_left ||  in_front_but_point_not_in_area){
                        if(center_x < sp.safe_region_x[1]){
                            return 1;
                        }else if(center_x < sp.safe_region_x[2]){
                            return 3;
                        }else {
                            return 2;
                        }
                    }
                }
            }
        }
        return  0;
    }

    public int city_road_detect(safe_region_param sp ,int imgwidth,int imgheight,float [] bboxes_xy,int label) {
        float slope1 = sp.slope[2];//(sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        float bias1 = sp.bias[2];//sp.safe_region_y[0] - slope1 * sp.safe_region_x[0];
        float slope2 = sp.slope[3];//(sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        float bias2 = sp.bias[3];//sp.safe_region_y[2] - slope2 * sp.safe_region_x[2];

        float half_x = (float) imgwidth / 2;

        float center_x = (bboxes_xy[0] + bboxes_xy[2]) / 2;
        long time_now = SystemClock.elapsedRealtime()  ;

        if(label == 9){
            if(TrafficLightDetectTime==0 && Math.abs(center_x - (float)imgwidth /2) <(float)imgwidth /8 ){
                TrafficLightDetectTime = time_now;
            }
        }

        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {

                float x = bboxes_xy[2 * j];
                //右下
                float y = bboxes_xy[2 * k + 1];
                //车内区域
                if (y > sp.safe_region_y[0]) {
                    continue;
                }
                //在等待位置检测区域
                if(TrafficClear && road_type == 2) {
                    if ( //time_now - TrafficLightDetectTime < 20000 &&
                            center_x > sp.safe_region_x[8] && center_x < sp.safe_region_x[9] &&
                                    y > sp.safe_region_y[8] && y < sp.safe_region_y[0] &&
                                    label != 4 && label != 6 && label <= 7
                        // && (bboxes_xy[3] - bboxes_xy[1] ) > 0.5 * (sp.safe_region_x[9] - sp.safe_region_x[8])
                    ) {
                        isWaitingTrafficLight = true;
                        if (WaitingTrafficLightTime == 0 && front_car_start_state == 0) {
                            WaitingTrafficLightTime = time_now;
                            front_car_start_state = 1;
                        }
                        TrafficClear = false;
                    }
                }

                //进入安全检测区域
                if (y > sp.safe_region_y[4]) {
                    //ys = -0.89 * x + 826.7
                    //左边线
                    float ys = slope1 * x + bias1;
                    // 左下点，在安全区外
                    //框框包括安全区
                    //右下也在安全区外
                    float x1 = bboxes_xy[2];
                    float y1 = bboxes_xy[3];
                    float ys_right1 = slope2 * x1 + bias2;
                    boolean in_front_but_point_not_in_area =
                            (j == 0 && k == 1 && y <= ys && x < half_x) &&
                                    (y1 > sp.safe_region_y[4] && x1 > half_x) &&
                                    (y1 < ys_right1) ;

                    boolean in_right_line_left = false;

                    //在左边线右侧
                    if (y > ys) {
                        float ys_right = slope2 * x + bias2;
                        //在右侧线左侧
                        in_right_line_left = (y > ys_right);
                    }
                    //城市模式 其他车辆左右进入，加塞，不需要非常严格判断非常远，判断近距离即可
                    if (in_right_line_left ||  in_front_but_point_not_in_area){
                        if(center_x < sp.safe_region_x[4]){
                            return 1;
                        }else if(center_x < sp.safe_region_x[5]){
                            return 3;
                        }else {
                            return 2;
                        }
                    }


                }
            }
        }
        return  0;
    }



    public int out_skirt_detect(safe_region_param sp ,int imgwidth,int imgheight,float [] bboxes_xy,int label) {
        float slope1 = sp.slope[4];//(sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        float bias1 = sp.bias[4];//sp.safe_region_y[0] - slope1 * sp.safe_region_x[0];
        float slope2 = sp.slope[5];//(sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        float bias2 = sp.bias[5];//sp.safe_region_y[2] - slope2 * sp.safe_region_x[2];

        float half_x = (float) imgwidth / 2;

        float center_x = (bboxes_xy[0] + bboxes_xy[2]) / 2;

        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {

                float x = bboxes_xy[2 * j];
                //右下
                float y = bboxes_xy[2 * k + 1];
                //车内区域
                if (y > sp.safe_region_y[0]) {
                    continue;
                }
                /*
                if ( y > sp.safe_region_y[1]){
                    //右下点
                    if (center_x < half_x){
                        //Log.d("SafeDetect", "in_safe_zone 1  " + x + "  " + y);
                        return 1;
                    }else {
                        //Log.d("SafeDetect", "in_safe_zone 2  " + x + "  " + y);
                        return 2;
                    }
                }*/
                //进入安全检测区域
                if (y > sp.safe_region_y[1]) {

                    //ys = -0.89 * x + 826.7
                    //左边线
                    float ys = slope1 * x + bias1;
                    // 左下点，在安全区外
                    //框框包括安全区
                    //右下也在安全区外
                    float x1 = bboxes_xy[2];
                    float y1 = bboxes_xy[3];
                    float ys_right1 = slope2 * x1 + bias2;
                    boolean in_front_but_point_not_in_area =
                            (j == 0 && k == 1 && y <= ys && x < half_x) &&
                                    (y1 > sp.safe_region_y[1] && x1 > half_x) &&
                                    (y1 < ys_right1) ;

                    boolean in_right_line_left = false;

                    //在左边线右侧
                    if (y > ys) {
                        float ys_right = slope2 * x + bias2;
                        //在右侧线左侧
                        in_right_line_left = (y > ys_right);
                    }
                    //郊外模式 判断范围和增强行人检测一样宽
                    if (in_right_line_left ||  in_front_but_point_not_in_area){
                        if(center_x < sp.safe_region_x[6]){
                            return 1;
                        }else if(center_x < sp.safe_region_x[7]){
                            return 3;
                        }else {
                            return 2;
                        }
                    }
                }
            }
        }
        return  0;
    }
/*
    public int out_skirt_detect1(safe_region_param sp ,int imgwidth,int imgheight,float [] bboxes_xy){
        float center_x = (bboxes_xy[0] + bboxes_xy[2] )/2;
        //进入安全检测区域
        if (y > sp.safe_region_y[1]) {
            //郊外模式，在检测区域内
            if (road_type == 3 && y > sp.safe_region_y[1]){
                //右下点
                if (center_x < half_x){
                    Log.d("SafeDetect", "in_safe_zone 1  " + x + "  " + y);
                    return 1;
                }else {
                    Log.d("SafeDetect", "in_safe_zone 2  " + x + "  " + y);
                    return 2;
                }
            }
            //ys = -0.89 * x + 826.7
            float ys = slope1 * x + bias1;
            //在左边线右侧
            if (y > ys) {
                float ys_right = slope2 * x + bias2;
                //在右侧线左侧
                if (y > ys_right) {


                    if (j==1 && k==1  ) {
                        //高速，直接报警
                        if(road_type == 1 || road_type == 3){
                            Log.d("SafeDetect", "in_safe_zone 1  " + x + "  " + y);
                            return 1;
                        }else if(road_type == 2 ){
                            //城市模式 其他车辆左右进入，加塞，不需要非常严格判断非常远，判断近距离即可
                            if (y > sp.safe_region_y[4]){
                                Log.d("SafeDetect", "in_safe_zone 1  " + x + "  " + y);
                                return 1;
                            }
                        }

                    }   else{
                        //高速，直接报警
                        if(road_type == 1 || road_type == 3){
                            Log.d("SafeDetect", "in_safe_zone 2  " + x + "  " + y);
                            return 2;
                        }else if(road_type == 2 ){
                            //城市模式 其他车辆左右进入，加塞，不需要非常严格判断非常远，判断近距离即可
                            if (y > sp.safe_region_y[4]){
                                Log.d("SafeDetect", "in_safe_zone 2  " + x + "  " + y);
                                return 2;
                            }
                        }
                    }
                }
            } else if (j == 0 && k == 1 && y <= ys && x < half_x) {
                // 左下点，在安全区外
                //框框包括安全区
                //右下也在安全区外
                float x1 = bboxes_xy[2];
                float y1 = bboxes_xy[3];

                if (y1 > sp.safe_region_y[1] && x1> half_x) {
                    float ys_right1 = slope2 * x1 + bias2;

                    //高速,乡镇，进入安全区域，直接报警
                    if(road_type == 1 || road_type == 3) {
                        if (y1 < ys_right1) {
                            Log.d("SafeDetect", "in_safe_zone full  " + x + "  " + y);
                            return 3;
                        }
                    }else if(road_type == 2 ){
                        //城市模式 其他车辆左右进入，加塞，不需要非常严格判断非常远，判断近距离即可
                        if (y1 < ys_right1) {
                            Log.d("SafeDetect", "in_safe_zone full  " + x + "  " + y);
                            return 3;
                        }
                    }
                }
            }
        }
    }*/

}
