package sg.org.pap.pickle.ui.pickle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.marshalchen.ultimaterecyclerview.itemTouchHelper.SimpleItemTouchHelperCallback;

public class CoverFlowCarousel extends Carousel {
    private float mAdjustPositionMultiplier = 0.8f;
    private float mAdjustPositionThreshold = 0.1f;
    private float mMaxRotationAngle = 70.0f;
    private float mMaxScaleFactor = 1.2f;
    private final Paint mPaint = new Paint();
    private float mPerspectiveMultiplier = SimpleItemTouchHelperCallback.ALPHA_FULL;
    private float mRadius = 2.0f;
    private final Canvas mReflectionCanvas = new Canvas();
    private float mReflectionHeight = 0.5f;
    private final Matrix mReflectionMatrix = new Matrix();
    private int mReflectionOpacity = 112;
    private float mRotationThreshold = 0.3f;
    private float mScalingThreshold = 0.3f;
    private int mTuningWidgetSize = 1280;
    private final PorterDuffXfermode mXfermode = new PorterDuffXfermode(Mode.DST_IN);

    private class CoverFrame extends FrameLayout {
        private Bitmap mReflectionCache;
        private boolean mReflectionCacheInvalid = false;

        public CoverFrame(Context context, View cover) {
            super(context);
            setCover(cover);
        }

        public void setCover(View cover) {
            removeAllViews();
            if (cover.getLayoutParams() != null) {
                setLayoutParams(cover.getLayoutParams());
            }
            LayoutParams lp = new LayoutParams(-1, -1);
            lp.leftMargin = 1;
            lp.topMargin = 1;
            lp.rightMargin = 1;
            lp.bottomMargin = 1;
            if (cover.getParent() != null && (cover.getParent() instanceof ViewGroup)) {
                ((ViewGroup) cover.getParent()).removeView(cover);
            }
            addView(cover, lp);
        }

        public Bitmap getDrawingCache(boolean autoScale) {
            Bitmap b = super.getDrawingCache(autoScale);
            if (this.mReflectionCacheInvalid && this.mReflectionCache == null) {
                try {
                    this.mReflectionCache = CoverFlowCarousel.this.createReflectionBitmap(b);
                    this.mReflectionCacheInvalid = false;
                } catch (NullPointerException e) {
                    Log.e("View", "Null pointer in createReflectionBitmap. Bitmap b=" + b, e);
                }
            }
            return b;
        }

        public void recycle() {
            if (this.mReflectionCache != null) {
                this.mReflectionCache.recycle();
                this.mReflectionCache = null;
            }
            this.mReflectionCacheInvalid = true;
        }
    }

    public CoverFlowCarousel(Context context) {
        super(context);
    }

    public CoverFlowCarousel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverFlowCarousel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setTransformation(View v) {
        int c = getChildCenter(v);
        v.setRotationY(getRotationAngle(c) - getAngleOnCircle(c));
        v.setTranslationX(getChildAdjustPosition(v));
        float scale = getScaleFactor(c) - getChildCircularPathZOffset(c);
        v.setScaleX(scale);
        v.setScaleY(scale);
    }

    protected void dispatchDraw(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(3, 3));
        super.dispatchDraw(canvas);
    }

    public void computeScroll() {
        super.computeScroll();
        for (int i = 0; i < getChildCount(); i++) {
            setTransformation(getChildAt(i));
        }
    }

    protected int getPartOfViewCoveredBySibling() {
        return 0;
    }

    protected View getViewFromAdapter(int position) {
        CoverFrame frame = (CoverFrame) this.mCache.getCachedView();
        View recycled = null;
        if (frame != null) {
            recycled = frame.getChildAt(0);
        }
        View v = this.mAdapter.getView(position, recycled, this);
        if (frame == null) {
            frame = new CoverFrame(getContext(), v);
        } else {
            frame.setCover(v);
        }
        if (VERSION.SDK_INT >= 11) {
            frame.setLayerType(1, null);
        }
        frame.setDrawingCacheEnabled(true);
        return frame;
    }

    private float getRotationAngle(int childCenter) {
        return (-this.mMaxRotationAngle) * getClampedRelativePosition(getRelativePosition(childCenter), this.mRotationThreshold * getWidgetSizeMultiplier());
    }

    private float getAngleOnCircle(int childCenter) {
        float x = getRelativePosition(childCenter) / this.mRadius;
        if (x < -1.0f) {
            x = -1.0f;
        }
        if (x > SimpleItemTouchHelperCallback.ALPHA_FULL) {
            x = SimpleItemTouchHelperCallback.ALPHA_FULL;
        }
        return (float) (((Math.acos((double) x) / 3.141592653589793d) * 180.0d) - 90.0d);
    }

    private float getScaleFactor(int childCenter) {
        return ((this.mMaxScaleFactor - SimpleItemTouchHelperCallback.ALPHA_FULL) * (SimpleItemTouchHelperCallback.ALPHA_FULL - Math.abs(getClampedRelativePosition(getRelativePosition(childCenter), this.mScalingThreshold * getWidgetSizeMultiplier())))) + SimpleItemTouchHelperCallback.ALPHA_FULL;
    }

    private float getClampedRelativePosition(float position, float threshold) {
        if (position < 0.0f) {
            if (position < (-threshold)) {
                return -1.0f;
            }
            return position / threshold;
        } else if (position > threshold) {
            return SimpleItemTouchHelperCallback.ALPHA_FULL;
        } else {
            return position / threshold;
        }
    }

    private float getRelativePosition(int pixexPos) {
        int half = getWidth() / 2;
        return ((float) (pixexPos - (getScrollX() + half))) / ((float) half);
    }

    private float getWidgetSizeMultiplier() {
        return ((float) this.mTuningWidgetSize) / ((float) getWidth());
    }

    private float getChildAdjustPosition(View child) {
        int c = getChildCenter(child);
        return (((((float) this.mChildWidth) * this.mAdjustPositionMultiplier) * this.mSpacing) * getClampedRelativePosition(getRelativePosition(c), this.mAdjustPositionThreshold * getWidgetSizeMultiplier())) * getSpacingMultiplierOnCirlce(c);
    }

    private float getSpacingMultiplierOnCirlce(int childCenter) {
        return (float) Math.sin(Math.acos((double) (getRelativePosition(childCenter) / this.mRadius)));
    }

    private float getOffsetOnCircle(int childCenter) {
        float x = getRelativePosition(childCenter) / this.mRadius;
        if (x < -1.0f) {
            x = -1.0f;
        }
        if (x > SimpleItemTouchHelperCallback.ALPHA_FULL) {
            x = SimpleItemTouchHelperCallback.ALPHA_FULL;
        }
        return (float) (1.0d - Math.sin(Math.acos((double) x)));
    }

    private float getChildCircularPathZOffset(int center) {
        return this.mPerspectiveMultiplier * getOffsetOnCircle(center);
    }

    protected View addAndMeasureChild(View child, int layoutMode) {
        if (child.getLayoutParams() == null) {
            child.setLayoutParams(new ViewGroup.LayoutParams(this.mChildWidth, this.mChildHeight));
        }
        addViewInLayout(child, layoutMode == 1 ? 0 : -1, child.getLayoutParams(), true);
        measureChild(child, MeasureSpec.makeMeasureSpec(this.mChildWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mChildHeight, 1073741824));
        child.setDrawingCacheEnabled(isChildrenDrawnWithCacheEnabled());
        return child;
    }

    private Bitmap createReflectionBitmap(Bitmap original) {
        int w = original.getWidth();
        int rh = (int) (((float) original.getHeight()) * this.mReflectionHeight);
        int gradientColor = Color.argb(this.mReflectionOpacity, 255, 255, 255);
        Bitmap reflection = Bitmap.createBitmap(original, 0, rh, w, rh, this.mReflectionMatrix, false);
        LinearGradient shader = new LinearGradient(0.0f, 0.0f, 0.0f, (float) reflection.getHeight(), gradientColor, 16777215, TileMode.CLAMP);
        this.mPaint.reset();
        this.mPaint.setShader(shader);
        this.mPaint.setXfermode(this.mXfermode);
        this.mReflectionCanvas.setBitmap(reflection);
        this.mReflectionCanvas.drawRect(0.0f, 0.0f, (float) reflection.getWidth(), (float) reflection.getHeight(), this.mPaint);
        return reflection;
    }
}
