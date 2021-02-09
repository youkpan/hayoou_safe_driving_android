// Copyright (c) 2020 Facebook, Inc. and its affiliates.
// All rights reserved.
//
// This source code is licensed under the BSD-style license found in the
// LICENSE file in the root directory of this source tree.

package com.wzt.yolov5;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

import static com.wzt.yolov5.MainActivity.view_setting_lines;


public class ResultView extends View {

    private final static int TEXT_X = 40;
    private final static int TEXT_Y = 35;
    private final static int TEXT_WIDTH = 260;
    private final static int TEXT_HEIGHT = 50;

    private Paint mPaintRectangle;
    private Paint mPaintText;
    private ArrayList<Box> mResults;

    public ResultView(Context context) {
        super(context);
    }

    public ResultView(Context context, AttributeSet attrs){
        super(context, attrs);
        mPaintRectangle = new Paint();
        mPaintRectangle.setColor(Color.YELLOW);
        mPaintText = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mResults == null) return;
        int count = 0;

        final Paint boxPaint = new Paint();
        float view_width = this.getWidth();
        float view_height = this.getHeight();
        if(view_setting_lines){
            boxPaint.setAlpha(200);
            boxPaint.setStyle(Paint.Style.STROKE);
            boxPaint.setStrokeWidth(4 * this.getWidth() / 800.0f);
            boxPaint.setColor(Color.argb(255,20,255,10));
            canvas.drawRect(new RectF( 0 ,(float)view_height*121/288 ,view_width ,
                    (float)view_height*121/288+1), boxPaint);
            canvas.drawRect(new RectF( (float)view_width/2 ,0 ,(float)view_width/2 ,
                    (float)view_height+1), boxPaint);
        }
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(2 * this.getWidth() / 800.0f);
        boxPaint.setTextSize(18 * this.getWidth() / 800.0f);

        for (Box box : mResults) {

            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            RectF rect = box.getRect();
            if (box.label <1000){
                canvas.drawText(box.getLabel() + String.format(Locale.CHINESE, " %.3f",
                        box.getScore()), box.x0 + 3, box.y0 + 30 * view_width / 1000.0f, boxPaint);
                rect = box.getRect();
                boxPaint.setStyle(Paint.Style.STROKE);
                //canvas.drawRect(new RectF(rect.left*scaleX ,rect.top*scaleY ,rect.right*scaleX ,
                //       rect.bottom*scaleY ), boxPaint);
                canvas.drawRect(rect, boxPaint);
            }else{
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
                float scaleX  = (float) view_width / 800;
                float scaleY  = (float) view_height / 288;
                rect.left *= scaleX;
                rect.top *= scaleY;

                canvas.drawCircle(rect.left,rect.top,5, boxPaint);
            }
        }
    }

    public void setResults(ArrayList<Box> results) {
        mResults = results;
    }
}
