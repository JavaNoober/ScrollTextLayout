package com.noober.scrolltextlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoqi on 2018/7/3
 */
public class ScrollTextView extends View {

    static List<String> textList = new ArrayList<>();

    Paint hintTextPaint;
    Paint textPaint;

    int height;
    int width;
    float margin;//上下文字间隔

    Rect hintRect;

    Rect centerRect;

    float textHeight;
    float hintTextHeight;

    static {
        textList.add("1八卦神算子 今日收益 +99.99%");
        textList.add("2八卦神算子 今日收益 +99.99%");
        textList.add("3八卦神算子 今日收益 +99.99%");
        textList.add("4八卦神算子 今日收益 +99.99%");
    }

    public ScrollTextView(Context context) {
        super(context);
        init(context);
    }

    public ScrollTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScrollTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context){
        margin = dip2px(context, 10);
        hintRect = new Rect();
        centerRect = new Rect();
        hintTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintTextPaint.setTextAlign(Paint.Align.LEFT);
        hintTextPaint.setColor(Color.parseColor("#43515D"));
        hintTextPaint.setTextSize(sp2px(context, 20));
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setTextSize(sp2px(context, 30));

        textPaint.getTextBounds(textList.get(0), 0, textList.get(0).length(), centerRect);
        hintTextPaint.getTextBounds(textList.get(1), 0, textList.get(1).length(), hintRect);
        textHeight = centerRect.height();
        hintTextHeight = hintRect.height();
    }

    public static float sp2px(Context context, float spVal) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, context.getResources().getDisplayMetrics());
    }

    public static float dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5F;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerTextY = height / 2;
        canvas.drawText(textList.get(0), 0,  centerTextY, textPaint);
        float bottomTextY = centerTextY + hintTextHeight + margin;
        canvas.drawText(textList.get(1), 0,  bottomTextY, hintTextPaint);
        Log.e("ScrollTextView", "bottomTextY:" + bottomTextY);
        float topTextY = centerTextY - textHeight - margin;
        canvas.drawText(textList.get(2), 0,  topTextY, hintTextPaint);
        Log.e("ScrollTextView", "topTextY:" + topTextY);
        scrollText(canvas);
    }


    private void scrollText(Canvas canvas){
        canvas.translate(0,0);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.e("ScrollTextView", wSpecMode + "");
//        if (wSpecMode == MeasureSpec.AT_MOST && hSpecMode == MeasureSpec.AT_MOST){
//            setMeasuredDimension(300,300);
//        }else  if (wSpecMode == MeasureSpec.AT_MOST){
//            setMeasuredDimension(300,hSpecSize);
//        }else if (hSpecMode ==  MeasureSpec.AT_MOST) {
//            setMeasuredDimension(wSpecSize,300);
//        }
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        Log.e("ScrollTextViewonMeasure", height + "");
        Log.e("ScrollTextViewonMeasure", width + "");
    }
}
