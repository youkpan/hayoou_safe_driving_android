package com.wzt.yolov5;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import static com.wzt.yolov5.MainActivity.distance_fix;
import static com.wzt.yolov5.MainActivity.vertical_distance_rate;
import static com.wzt.yolov5.SafeDetect.hit_predict;

public class Distance {
    public static int[] lane_height_pix =  {121, 131, 141, 150, 160, 170, 180, 189, 199, 209, 219, 228, 238, 248, 258, 267, 277, 287};
    public static int lane_near_width_pix = 0;
    static int imgwidth = 0;
    static int imgheight = 0;
    static float virtual_height = 0;
    static float  startY = 0;
    public Distance(Canvas canvas, Box[] results, int lane_offset){
        int lane_near_left_pix =0;
        int lane_near_right_pix =0;
        imgwidth = canvas.getWidth();
        imgheight = canvas.getHeight();
        virtual_height = (float)imgwidth * 288 / 800;
        startY = (imgheight - virtual_height) /2;
        /*
        for(int i =lane_offset + 18 ;i<lane_offset + 18*2;i ++){
            Box box1 = results[i];
            if (box1.label == 1001) {
                //the last pix , in the bottom
                lane_near_left_pix = (int)box1.y0;
            }else if(box1.label == 1002) {
                //the last pix , in the bottom
                lane_near_right_pix = (int)box1.y0;
            }
        }
        lane_near_width_pix = lane_near_right_pix - lane_near_left_pix;
        */
    }

    public static float getDistance(Box box, Box[] results, SafeDetect.safe_region_param sp, Canvas canvas,int lane_offset){
        //float carmera_height = 1.2f;
        if(box==null){
            return 0;
        }
        float front_distance = 2.0f * distance_fix;
        float lane_width_pix = 0f;
        float lane_width_pix_0 = 0f;
        float object_bottom_to_pix = (box.y1 - startY)*800/imgwidth;
        float object_bottom_to_horizon_pix = object_bottom_to_pix - 121;

        float origin_to_horizon_pix = 0f;

        if(object_bottom_to_pix >=121 && object_bottom_to_pix <=287 ){
            int lane_line_zone_index = 0;
            float lane_left = 0;
            float lane_right =0f;
            float lane_far_width_pix = 0;
            Paint boxPaint = null;
            if(hit_predict >0) {
                boxPaint = new Paint();
                boxPaint.setAlpha(200);
                boxPaint.setStyle(Paint.Style.STROKE);
                boxPaint.setStrokeWidth(4 * imgwidth / 800.0f);
                boxPaint.setColor(Color.argb(255, 20, 255, 255));
            }
            if(vertical_distance_rate >= 0.9999f) {
                for (int i = lane_offset + 1; (i < lane_offset + 54 && i < results.length - 1); i++) {

                    Box box1 = results[i];
                    Box box2 = results[i + 1];

                    if (box1.label != box2.label || box1.label <= 1000 || box1.label > 1002 || box1.score <= 0 || box2.score <= 0) {
                        continue;
                    }

                    if (object_bottom_to_pix >= box1.y0 && object_bottom_to_pix < box2.y0) {
                        if (box1.label == 1001) {
                            lane_left = box1.x0 + (box1.y0 - object_bottom_to_pix) * (box2.x0 - box1.x0) / (box2.y0 - box1.y0);
                            if (hit_predict > 0) {
                                canvas.drawCircle(lane_left / 800f * imgwidth, box1.y0 * imgwidth / 800 + startY, 5f * imgwidth / 800.0f, boxPaint);
                            }
                        } else if (results[i - 1].label != 1001 && lane_left > 150) {//不要第一个点
                            lane_right = box1.x0 + (box1.y0 - object_bottom_to_pix) * (box2.x0 - box1.x0) / (box2.y0 - box1.y0);
                            lane_far_width_pix = lane_right - lane_left;
                            if (hit_predict > 0) {
                                canvas.drawCircle(lane_right / 800f * imgwidth, box1.y0 * imgwidth / 800 + startY, 5f * imgwidth / 800.0f, boxPaint);
                                boxPaint.setStrokeWidth(2 * imgwidth / 800.0f);
                                canvas.drawLine(lane_left / 800f * imgwidth, box1.y0 * imgwidth / 800 + startY, (lane_left + lane_far_width_pix) / 800f * imgwidth, box1.y0 * imgwidth / 800 + startY, boxPaint);
                            }
                            break;
                        }
                    }
                }
            }

            float distance = 0f;
            if(object_bottom_to_horizon_pix>0){
                distance = front_distance * 125f / (object_bottom_to_horizon_pix) ;
            }

            if(lane_far_width_pix > 0){
                float distance1 = front_distance * 0.5f / (lane_far_width_pix/800);
                if(distance >0){
                    distance =  vertical_distance_rate  * distance + (1-vertical_distance_rate) * distance1;
                }else {
                    distance = distance1;
                }
            }
            return distance >300 ? 300f :distance;
        }else {
            return 0f;
        }
    }


}
