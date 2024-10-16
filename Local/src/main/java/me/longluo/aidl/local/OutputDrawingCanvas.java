package me.longluo.aidl.local;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * View to render the result of receiving AIDL
 * commands via the Event Bus to draw lines.
 */
public class OutputDrawingCanvas extends View {
    private Paint mPaint;
    private Path mPath;
    private Bitmap mBitmapA;
    private Bitmap mBitmapB;
    private Canvas mCanvas;

    private float mOffsetX;
    private float mOffsetY;
    private float mRotation;

    private Handler mHandler;
    private Runnable mRunnable;

    Matrix mBitmapMatrixA;
    Matrix mBitmapMatrixB;

    private List<Integer> mColours;
    private int mPaintColour;

    private List<Integer> mStrokeWidths;
    private int mStrokeWidth;

    private Random mRandom;

    public OutputDrawingCanvas(Context context) {
        super(context);
        init();
    }

    public OutputDrawingCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OutputDrawingCanvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void resume() {
        startRotationAnimation();
    }

    public void pause() {
        mHandler.removeCallbacks(mRunnable);
    }

    public void clear() {
        mBitmapA.eraseColor(Color.TRANSPARENT);
        mBitmapB.eraseColor(Color.TRANSPARENT);
        invalidate();
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        mPath.moveTo(x1, y1);
        mPath.lineTo(x2, y2);

        mCanvas.setBitmap(mBitmapA);
        mCanvas.drawPath(mPath, mPaint);

        mCanvas.setBitmap(mBitmapB);
        mCanvas.drawPath(mPath, mPaint);

        mPath.reset();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mOffsetX = w / 2f;
        mOffsetY = h / 2f;

        int size = Math.max(w, h);

        mBitmapA = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        mBitmapB = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmapA);
    }

    private void updateAnimation() {
        mRotation += 1f;
        if (mRotation >= 360f) {
            mRotation = 0f;
        }

        mPaintColour = mColours.get(mRandom.nextInt(mColours.size()));
        mStrokeWidth = mStrokeWidths.get(mRandom.nextInt(mStrokeWidths.size()));
    }

    private void init() {
        mRandom = new Random();

        mBitmapMatrixA = new Matrix();
        mBitmapMatrixB = new Matrix();

        mRotation = 0f;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);
        mPath = new Path();

        mColours = new ArrayList<>();
        mColours.add(Color.BLACK);
        mColours.add(Color.DKGRAY);
        mColours.add(Color.GRAY);
        mColours.add(Color.LTGRAY);
        mColours.add(Color.WHITE);
        mColours.add(Color.RED);
        mColours.add(Color.GREEN);
        mColours.add(Color.BLUE);
        mColours.add(Color.YELLOW);
        mColours.add(Color.CYAN);
        mColours.add(Color.MAGENTA);

        mStrokeWidths = new ArrayList<>();
        mStrokeWidths.add(1);
        mStrokeWidths.add(2);
        mStrokeWidths.add(3);
        mStrokeWidths.add(4);
        mStrokeWidths.add(5);
        mStrokeWidths.add(6);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                updateAnimation();
                invalidate();
                startRotationAnimation();
            }
        };

        mHandler = new Handler();
    }

    private void startRotationAnimation() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 16);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mPaintColour);
        mPaint.setStrokeWidth(mStrokeWidth);

        mBitmapMatrixA.reset();
        mBitmapMatrixA.postRotate(mRotation, mOffsetX * 1.2f, mOffsetY * 1.2f);
        canvas.drawBitmap(mBitmapA, mBitmapMatrixA, mPaint);

        mBitmapMatrixB.reset();
        mBitmapMatrixB.postRotate(-mRotation, mOffsetX * 0.8f, mOffsetY * 0.8f);
        canvas.drawBitmap(mBitmapB, mBitmapMatrixB, mPaint);

        canvas.drawPath(mPath, mPaint);
    }
}
