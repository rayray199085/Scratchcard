package com.project.stephencao.scratchcard.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import com.project.stephencao.scratchcard.R;

public class ScratchcardView extends View {
    private Paint mOuterPaint;
    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;

    private float mLastX;
    private float mLastY;

    private Paint mImagePaint;

    private Bitmap mImage;
    private String mText;
    private int mTextSize;
    private int mTextColor;
    private Paint mInnerPaint;
    private Rect mTextBound;
    private volatile boolean mIsFinish = false;
    private OnScratchCardEndListener mOnScratchCardEndListener;

    public void setOnScratchCardEndListener(OnScratchCardEndListener onScratchCardEndListener) {
        this.mOnScratchCardEndListener = onScratchCardEndListener;
    }

    public ScratchcardView(Context context) {
        this(context, null);
    }

    public ScratchcardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScratchcardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScratchcardView);
        mText = array.getString(R.styleable.ScratchcardView_text);
        mTextSize = (int) array.getDimension(R.styleable.ScratchcardView_text_size,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 30, getResources().getDisplayMetrics()));
        mTextColor = array.getColor(R.styleable.ScratchcardView_text_color, Color.RED);
        array.recycle();
    }

    private void init() {
        mOuterPaint = new Paint();
        mPath = new Path();
        mImage = BitmapFactory.decodeResource(getResources(), R.drawable.card);
        mInnerPaint = new Paint();
//        mText = "$500,000.00";
        mTextBound = new Rect();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mImagePaint = new Paint();
        setupImagePaint();
        setupOuterPaint();
        setupInnerPaint();
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mImagePaint);
        mImagePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mCanvas.drawBitmap(mImage, 0, 0, mImagePaint); // dest
    }

    private void setupImagePaint() {
        mImagePaint.setAntiAlias(true);
        mImagePaint.setDither(true);
        mImagePaint.setColor(Color.BLACK);
        mImagePaint.setStyle(Paint.Style.FILL);
    }

    private void setupInnerPaint() {
        mInnerPaint.setAntiAlias(true);
        mInnerPaint.setDither(true);
        mInnerPaint.setColor(mTextColor);
        mInnerPaint.setTextSize(mTextSize);
        mInnerPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    private void setupOuterPaint() {
        mOuterPaint.setAntiAlias(true);
        mOuterPaint.setDither(true);
        mOuterPaint.setColor(Color.RED);
        mOuterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOuterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOuterPaint.setStyle(Paint.Style.STROKE);
        mOuterPaint.setStrokeWidth(20);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastX = x;
                mLastY = y;
                mPath.moveTo(x, y);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float dx = Math.abs(x - mLastX);
                float dy = Math.abs(y - mLastY);
                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                new Thread(myRunnable).start();
                break;
            }
        }
        invalidate();
        return true;
    }

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            float wipeArea = 0;
            float totalArea = width * height;
            Bitmap bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
            int[] mPixels = new int[width * height];
            bitmap.getPixels(mPixels, 0, width, 0, 0, width, height);

            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    int index = i + j * width;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                float rate = wipeArea / totalArea * 100;
                if (rate > 60) {
                    mIsFinish = true;
                    postInvalidate();
                }
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        int x = (int) (getMeasuredWidth() / 2.0f - mTextBound.width() / 2);
        int y = (int) (getMeasuredHeight() / 2.0f + mTextBound.height() / 2.0f);
        canvas.drawText(mText, x, y, mInnerPaint);
        if (!mIsFinish) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        } else {
            if (mOnScratchCardEndListener != null) {
                mOnScratchCardEndListener.isEnd();
            }
        }

    }

    private void drawPath() {
        mOuterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOuterPaint); // src
    }

    public interface OnScratchCardEndListener {
        void isEnd();
    }

    public void setTextContent(String text){
        mText = text;
        setupInnerPaint();
    }

}
