package com.example.vpunay.qrsample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

public class ScannerOverlay extends ViewGroup {
    private static float left;
    private static float top;
    private static float right;
    private static float bottom;
    private static int rectWidth, rectHeight;
    private Paint eraser, borderPaint;
    private PorterDuffXfermode porterDuffXfermode;
    private RectF rectF;

    public ScannerOverlay(final Context context) {
        super(context);
    }

    public ScannerOverlay(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerOverlay(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScannerOverlay,
                0, 0);
        //Initialize overlay UI essentials
        rectWidth = a.getInteger(R.styleable.ScannerOverlay_square_width,
                getResources().getInteger(R.integer.smart_login_default_square_width));
        rectHeight = a.getInteger(R.styleable.ScannerOverlay_square_height,
                getResources().getInteger(R.integer.smart_login_default_square_height));

        eraser = new Paint();
        porterDuffXfermode = new PorterDuffXfermode((PorterDuff.Mode.CLEAR));
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final int rectWidthPx = dpToPx(rectWidth, displayMetrics);
        final int rectHeightPx = dpToPx(rectHeight, displayMetrics);
        final float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        //If screen size is small, adjust position of Qr scanner square
        final double heightPositionDivider = dpHeight <= getResources().getInteger(R.integer.small_screen_height) ? 4.5
                : getResources().getInteger(R.integer.smart_login_height_divider);
        //Calculate scanner sides
        left = (float)((w - rectWidthPx) / getResources().getInteger(R.integer.smart_login_width_position_divider));
        top = (float)((h - rectHeightPx ) / heightPositionDivider);
        bottom = top + rectHeightPx;
        right = left + rectWidthPx;
        //Create rect
        rectF = new RectF(left, top, right, bottom);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public static boolean isQrCodeInsideOverlay(final RectF rect, final GraphicOverlay graphicOverlay){
       return rect.centerX() >= left && rect.centerX() <= right && rect.centerY() >= top && rect.centerY() <= bottom;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    public int dpToPx(final int dp, final DisplayMetrics displayMetrics) {
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // draw transparent rect
        eraser.setAntiAlias(true);
        eraser.setXfermode(porterDuffXfermode);
        canvas.drawRoundRect(rectF, 0, 0, eraser);

        //border color
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        //border thickness
        final int thickness = getResources().getInteger(R.integer.smart_login_border_thickness);
        //border distance
        final int distance = getResources().getInteger(R.integer.smart_login_border_distance);

        //Draw borders:

        //top left corner
        canvas.drawRect(left - thickness, top - thickness, distance + left, top, borderPaint);
        canvas.drawRect(left - thickness, top, left, distance + top, borderPaint);

        //top right corner
        canvas.drawRect(right - distance, top - thickness, right + thickness, top, borderPaint);
        canvas.drawRect(right, top, right + thickness, distance + top, borderPaint);

        //bottom left corner
        canvas.drawRect(left - thickness, bottom, distance + left, bottom + thickness, borderPaint);
        canvas.drawRect(left - thickness, bottom - distance, left, bottom, borderPaint);

        //bottom right corner
        canvas.drawRect(right - distance, bottom, right + thickness, bottom + thickness, borderPaint);
        canvas.drawRect(right, bottom - distance, right + thickness, bottom, borderPaint);
    }
}