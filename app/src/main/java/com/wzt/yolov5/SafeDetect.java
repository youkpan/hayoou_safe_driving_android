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
import static com.wzt.yolov5.MainActivity.DetectWidth;
import static com.wzt.yolov5.MainActivity.alarm_mode;
import static com.wzt.yolov5.MainActivity.auto_adjust_detect_area;
import static com.wzt.yolov5.MainActivity.person_detect_focus;
import static com.wzt.yolov5.MainActivity.recent_fps;
import static com.wzt.yolov5.MainActivity.road_type;

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

    protected void draw_safe_zone(Canvas canvas) {
        final Paint linePaint = new Paint();

        float canvas_width = canvas.getWidth();
        float canvas_height = canvas.getHeight();

        linePaint.setAlpha(200);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3 * canvas.getWidth() / 800.0f);
        linePaint.setColor(Color.argb(180,20,190,10));

        float line1_X0 = canvas_width/2f - 0.035f*canvas_width + direction_center_x_offset;
        float line1_Y0 = canvas_height*0.52f;

        float line1_X1 = canvas_width/2f + 0.035f*canvas_width + direction_center_x_offset;
        float line2_Y1 = canvas_height*0.6f;

        safe_region_param sp = get_safe_zone_detect((int)canvas_width,(int)canvas_height);

        canvas.drawLine(sp.safe_region_x[1], sp.safe_region_y[1],
                sp.safe_region_x[2],sp.safe_region_y[2], linePaint);
        //left
        canvas.drawLine(sp.safe_region_x[0],sp.safe_region_y[0],
                sp.safe_region_x[1], sp.safe_region_y[1], linePaint);
        //right
        canvas.drawLine(sp.safe_region_x[2],sp.safe_region_y[2],
                sp.safe_region_x[3],sp.safe_region_y[3], linePaint);

        if(road_type == 2 ) {
            linePaint.setColor(Color.argb(160, 220, 190, 190));
            //城市模式
            canvas.drawLine(sp.safe_region_x[4], sp.safe_region_y[4],
                    sp.safe_region_x[5], sp.safe_region_y[5], linePaint);
            //left
            canvas.drawLine(sp.safe_region_x[0], sp.safe_region_y[0],
                    sp.safe_region_x[4], sp.safe_region_y[4], linePaint);
            //right
            canvas.drawLine(sp.safe_region_x[5], sp.safe_region_y[5],
                    sp.safe_region_x[3], sp.safe_region_y[3], linePaint);
        }

        if(person_detect_focus ==1 || road_type == 3) {
            linePaint.setColor(Color.argb(160, 150, 190, 10));
            //城市模式
            canvas.drawLine(sp.safe_region_x[6], sp.safe_region_y[6],
                    sp.safe_region_x[7], sp.safe_region_y[7], linePaint);
            //left
            canvas.drawLine(sp.safe_region_x[0], sp.safe_region_y[0],
                    sp.safe_region_x[6], sp.safe_region_y[6], linePaint);
            //right
            canvas.drawLine(sp.safe_region_x[7], sp.safe_region_y[7],
                    sp.safe_region_x[3], sp.safe_region_y[3], linePaint);
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
        if(     (timenow - last_alarm_time < 40000)  &&
                (timenow - last_offset_change_time > 20000) &&
                (auto_adjust_detect_area == 1 || auto_adjust_detect_area == 2)){

            last_offset_change_time = timenow;
            if(Math.abs(direction_center_x_offset + x)<limit_offset_x){
                direction_center_x_offset += x ;

                SharedPreferences userInfo = MainActivity.mcontext.getSharedPreferences("adas", MODE_PRIVATE);
                SharedPreferences.Editor editor = userInfo.edit();
                editor.putFloat("direction_center_x_offset", direction_center_x_offset);
                editor.commit();

            }
        }
    }

    public void safeDetect(Canvas canvas, Box[] results, MainActivity main){
        draw_safe_zone( canvas );
        float check_detect_time = 0.3f;
        float check_lane_time = 1.5f;
        float limit_offset_x = 0.07f*canvas.getWidth();
        long time_now = SystemClock.elapsedRealtime();
        int detect_result = in_safe_zone(results,canvas.getWidth(),canvas.getHeight(),false);
        if(detect_result>0){
            switch (detect_result){
                case 1:

                    if(last_detect_alert_time[DETECT_ALARM_LEFT]==0){
                        last_detect_alert_time[DETECT_ALARM_LEFT] = time_now;
                    }

                    last_detect_alert_count[DETECT_ALARM_LEFT] += 1;

                    break;
                case 2:

                    if(last_detect_alert_time[DETECT_ALARM_RIGHT]==0){
                        last_detect_alert_time[DETECT_ALARM_RIGHT] = time_now;
                    }
                    last_detect_alert_count[DETECT_ALARM_RIGHT] += 1;

                    break;
                case 3:
                    if(last_detect_alert_time[DETECT_ALARM_FRONT]==0){
                        last_detect_alert_time[DETECT_ALARM_FRONT] = time_now;
                    }
                    last_detect_alert_count[DETECT_ALARM_FRONT] += 1;

                    break;
                case 11:

                    if(last_lane_alert_time[LANE_ALARM_LEFT]==0){
                        last_lane_alert_time[LANE_ALARM_LEFT] = time_now;
                    }
                    last_lane_alert_count[LANE_ALARM_LEFT] ++;

                    break;
                case 12:
                    if(last_lane_alert_time[LANE_ALARM_RIGHT]==0){
                        last_lane_alert_time[LANE_ALARM_RIGHT] = time_now;
                    }
                    last_lane_alert_count[LANE_ALARM_RIGHT] ++;

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

        }else {

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
                if((last_lane_alert_time[i]!=0 ) &&
                    ( time_now - last_lane_alert_time[i] > check_lane_time * 1000 ) &&
                    (last_lane_alert_count[i] < recent_fps * check_lane_time *0.5)
                ){
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
            if(SystemClock.elapsedRealtime() - last_alarm_time >20000){
                MainActivity.detect_msg = "";
            }

        }

        //每次超时 n 秒 如果检测到告警，就清空
        for (int i = 0; i < 3; i++) {
            if ((last_detect_alert_time[i] != 0) &&
                (time_now - last_detect_alert_time[i] > check_detect_time * 1000) &&
                (last_detect_alert_count[i] > recent_fps * check_detect_time * 0.3)
            ) {
                last_detect_alert_count[i] = 0;
                last_detect_alert_time[i] = 0;
                if( (alarm_mode==1 || alarm_mode==2) &&
                        (time_now - last_alarm_time >MainActivity.alarm_wait_time * 1000)){
                    last_alarm_time = time_now;

                    if(road_type == 1 ) {
                        main.play_alarm(1.5f);
                    }else if(road_type == 2 ) {
                        main.play_alarm(1.f);
                    }else if(road_type == 3 ) {
                        main.play_alarm(1.5f);
                    }
                }

                switch (i){
                    case DETECT_ALARM_LEFT:
                        MainActivity.detect_msg = "左边物体进入行驶区域";
                        alarm_box(canvas,0,1,0);
                        break;
                    case DETECT_ALARM_RIGHT:
                        MainActivity.detect_msg = "右边边物体进入行驶区域";
                        alarm_box(canvas,0,0,1);
                        break;
                    case DETECT_ALARM_FRONT:
                        MainActivity.detect_msg = "前方物体进入行驶区域";
                        alarm_box(canvas,1,0,0);
                        break;
                }

            }
        }

        for (int i = 1; i < 3; i++) {
            if((last_lane_alert_time[i]!=0 ) &&
                ( time_now - last_lane_alert_time[i] > check_lane_time * 1000 ) &&
                (last_lane_alert_count[i] > recent_fps * check_lane_time *0.6)
            ){
                last_lane_alert_count[i] = 0;
                last_lane_alert_time[i] = 0;

                switch (i){
                    case LANE_ALARM_LEFT:
                        if((alarm_mode==1 || alarm_mode==3) &&
                                (time_now - last_alarm_time >MainActivity.alarm_wait_time * 1000)){
                            last_alarm_time = time_now;

                            if(road_type == 1 ) {
                                main.play_alarm3();
                            }else if(road_type == 2 ) {
                                main.play_alarm1(1.f);
                            }else if(road_type == 3 ) {
                                main.play_alarm1(1f);
                            }
                        }

                        MainActivity.detect_msg = "车道偏离：\n前方靠近左侧车道";
                        fix_direction_offset(1,0,limit_offset_x);
                        alarm_box(canvas,0,1,0);
                        break;
                    case LANE_ALARM_RIGHT:
                        if((alarm_mode==1 || alarm_mode==3) &&
                                (time_now - last_alarm_time >MainActivity.alarm_wait_time * 1000)){
                            last_alarm_time = time_now;
                            if(road_type == 1 ) {
                                main.play_alarm(1.5f);
                            }else if(road_type == 2 ) {
                                main.play_alarm1(1.f);
                            }else if(road_type == 3 ) {
                                main.play_alarm1(1f);
                            }
                        }

                        MainActivity.detect_msg = "车道偏离：\n前方靠近右侧车道";
                        fix_direction_offset(-1,0,limit_offset_x);
                        alarm_box(canvas,0,0,1);
                        break;
                }


            }
        }

    }

    class safe_region_param{
        float [] safe_region_x;
        float [] safe_region_y;
        float [] slope;
        float [] bias;
    }

    public safe_region_param get_safe_zone_detect(int imgwidth,int imgheight){
        safe_region_param sp = new safe_region_param();
        //高速（左下，左上，右上，右下） ，城市（左上，右上），行人增强（左上，右上）
        sp.safe_region_x = new float[] {0,0,0,0,0,0,0,0};
        sp.safe_region_y = new float[] {0,0,0,0,0,0,0,0};
        sp.slope = new float[] {0,0,0,0,0,0};
        sp.bias = new float[] {0,0,0,0,0,0};

        float y_offset =  (float)(MainActivity.front_detect-0.5)* (- 0.35f) * imgheight;

        float road_x_width_half = 0.035f*imgwidth * DetectWidth;

        sp.safe_region_x[0] = (float)imgwidth/2f - 0.2f*imgwidth * DetectWidth;
        sp.safe_region_y[0] = (float)imgheight*0.8f + y_offset;

        sp.safe_region_x[1] = (float)imgwidth/2f - road_x_width_half + direction_center_x_offset;
        sp.safe_region_y[1] = (float)0.52f*imgheight + y_offset;

        sp.safe_region_x[2] = (float)imgwidth/2f + road_x_width_half + direction_center_x_offset;
        sp.safe_region_y[2] = (float)0.52f*imgheight + y_offset;

        sp.safe_region_x[3] = (float)imgwidth/2f + 0.2f*imgwidth * DetectWidth;
        sp.safe_region_y[3] = (float)imgheight*0.8f + y_offset;

        //直线参数
        sp.slope[0] = (sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        sp.bias[0] = sp.safe_region_y[0] - sp.slope[0] * sp.safe_region_x[0];
        sp.slope[1] = (sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        sp.bias[1] = sp.safe_region_y[2] - sp.slope[1] * sp.safe_region_x[2];

        //城市模式 水平线 X1
        sp.safe_region_x[4] = (float)imgwidth/2f - 0.065f*imgwidth * DetectWidth + direction_center_x_offset;
        sp.safe_region_y[4] = (float)imgheight*0.6f + y_offset;
        //城市模式 水平线 X2
        sp.safe_region_x[5] = (float)imgwidth/2f + 0.065f*imgwidth * DetectWidth + direction_center_x_offset;
        sp.safe_region_y[5] = (float)imgheight*0.6f + y_offset;

        //城市模式 直线参数
        sp.slope[2] = (sp.safe_region_y[4] - sp.safe_region_y[0]) / (sp.safe_region_x[4] - sp.safe_region_x[0]);
        sp.bias[2] = sp.safe_region_y[0] - sp.slope[2] * sp.safe_region_x[0];
        sp.slope[3] = (sp.safe_region_y[3] - sp.safe_region_y[5]) / (sp.safe_region_x[3] - sp.safe_region_x[5]);
        sp.bias[3] = sp.safe_region_y[5] - sp.slope[3] * sp.safe_region_x[5];

        //行人检测增强
        sp.safe_region_x[6] = (float)imgwidth/2f - road_x_width_half *4 + direction_center_x_offset;
        sp.safe_region_y[6] = (float)imgheight*0.52f + y_offset;
        //行人检测增强 水平线 X2
        sp.safe_region_x[7] = (float)imgwidth/2f + road_x_width_half *4 + direction_center_x_offset;
        sp.safe_region_y[7] = (float)imgheight*0.52f + y_offset;

        //行人检测增强 直线参数
        sp.slope[4] = (sp.safe_region_y[6] - sp.safe_region_y[0]) / (sp.safe_region_x[6] - sp.safe_region_x[0]);
        sp.bias[4] = sp.safe_region_y[0] - sp.slope[4] * sp.safe_region_x[0];
        sp.slope[5] = (sp.safe_region_y[3] - sp.safe_region_y[7]) / (sp.safe_region_x[3] - sp.safe_region_x[7]);
        sp.bias[5] = sp.safe_region_y[7] - sp.slope[5] * sp.safe_region_x[7];

        return sp;
    }


    public int in_safe_zone(Box[] bboxes,int imgwidth,int imgheight,boolean check_lane) {

        safe_region_param sp = get_safe_zone_detect(imgwidth, imgheight);

        float slope1 = sp.slope[0];//(sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        float bias1 = sp.bias[0];//sp.safe_region_y[0] - slope1 * sp.safe_region_x[0];
        float slope2 = sp.slope[1];//(sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        float bias2 = sp.bias[1];//sp.safe_region_y[2] - slope2 * sp.safe_region_x[2];

        float half_x = (float)imgwidth/2;
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
                            out = high_way_detect(sp,imgwidth,imgheight,bboxes_xy);
                            if(out >0){
                                return out;
                            }
                            break;
                        case 2:
                            out = city_road_detect(sp,imgwidth,imgheight,bboxes_xy);
                            if(out >0){
                                return out;
                            }
                            break;
                        case 3:
                            out = out_skirt_detect(sp,imgwidth,imgheight,bboxes_xy);
                            if(out >0){
                                return out;
                            }
                            break;

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
                if (bboxes[i].getScore() > 0 || check_lane) {
                    int label = bboxes[i].label;
                    if (label == 1000 || label == 1003){
                        continue;
                    }
                    //x0 y0,x0 y1,x1 y0..
                    float x = bboxes[i].x0;
                    //右下
                    float y = bboxes[i].y0;
                    float virtual_height = (float)imgwidth * 288 / 800;
                    float startY = (imgheight - virtual_height) /2;
                    float lane_scaleX = (float)imgwidth /800;
                    float lane_scaleY = lane_scaleX;
                    x *= lane_scaleX;
                    y *= lane_scaleY;
                    y += startY;

                    if (y > sp.safe_region_y[1]) {
                        //ys = -0.89 * x + 826.7
                        float ys = slope1 * x + bias1;
                         if (y > ys) {
                            float ys_right = slope2 * x + bias2;
                            if (y > ys_right) {

                                    if (label == 1001) {
                                        Log.d("SafeDetect", "lane in_safe_zone 1  " + x + "  " + y);
                                        return 11;
                                    } else {
                                        Log.d("SafeDetect", "lane in_safe_zone 2  " + x + "  " + y);
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

    public int high_way_detect(safe_region_param sp ,int imgwidth,int imgheight,float [] bboxes_xy) {
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

    public int city_road_detect(safe_region_param sp ,int imgwidth,int imgheight,float [] bboxes_xy) {
        float slope1 = sp.slope[2];//(sp.safe_region_y[1] - sp.safe_region_y[0]) / (sp.safe_region_x[1] - sp.safe_region_x[0]);
        float bias1 = sp.bias[2];//sp.safe_region_y[0] - slope1 * sp.safe_region_x[0];
        float slope2 = sp.slope[3];//(sp.safe_region_y[3] - sp.safe_region_y[2]) / (sp.safe_region_x[3] - sp.safe_region_x[2]);
        float bias2 = sp.bias[3];//sp.safe_region_y[2] - slope2 * sp.safe_region_x[2];

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



    public int out_skirt_detect(safe_region_param sp ,int imgwidth,int imgheight,float [] bboxes_xy) {
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
