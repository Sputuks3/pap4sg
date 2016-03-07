package sg.org.pap.pickle.ui.pickle;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.Scroller;
import java.util.ArrayList;
import sg.org.pap.pickle.R;
import twitter4j.TwitterResponse;

public class CoverFlowView<T extends CoverFlowAdapter> extends View {
    private static final float CARD_SCALE = 0.15f;
    private static final int DURATION = 200;
    private static final float FRICTION = 10.0f;
    private static final int LONG_CLICK_DELAY = ViewConfiguration.getLongPressTimeout();
    private static final float MAX_SPEED = 6.0f;
    private static float MOVE_POS_MULTIPLE = 3.0f;
    private static final float MOVE_SPEED_MULTIPLE = 1.0f;
    static final int NO_POSITION = -1;
    private static final int TOUCH_MINIMUM_MOVE = 5;
    private final int ALPHA_DATUM = 76;
    protected final int CHILD_SPACING = -200;
    protected final int INVALID_POSITION = NO_POSITION;
    private int STANDARD_ALPHA;
    protected int VISIBLE_VIEWS = 3;
    private T mAdapter;
    private Runnable mAnimationRunnable;
    private int mChildHeight;
    private Matrix mChildTransfromer;
    private int mChildTranslateY;
    protected int mCoverFlowCenter;
    private CoverFlowListener<T> mCoverFlowListener;
    private Rect mCoverFlowPadding;
    boolean mDataChanged;
    private Paint mDrawChildPaint;
    private PaintFlagsDrawFilter mDrawFilter;
    private boolean mDrawing;
    private float mDuration;
    protected CoverFlowGravity mGravity;
    private SparseArray<int[]> mImageRecorder;
    private int mItemCount;
    private int mLastOffset;
    protected CoverFlowLayoutMode mLayoutMode;
    private TopImageLongClickListener mLongClickListener;
    private boolean mLongClickPosted;
    private LongClickRunnable mLongClickRunnable;
    private boolean mLongClickTriggled;
    private float mOffset;
    private RecycleBin mRecycler;
    private Matrix mReflectionTransfromer;
    private int mReflectionTranslateY;
    private ArrayList<Integer> mRemoveReflectionPendingArray;
    private Scroller mScroller;
    private float mStartOffset;
    private float mStartSpeed;
    private long mStartTime;
    private int mTopImageIndex;
    private boolean mTouchMoved;
    private RectF mTouchRect;
    private float mTouchStartPos;
    private float mTouchStartX;
    private float mTouchStartY;
    private VelocityTracker mVelocity;
    private int mVisibleChildCount;
    private int mWidth;
    private int reflectGap;
    private float reflectHeightFraction;
    private boolean topImageClickEnable = true;

    public enum CoverFlowGravity {
        TOP,
        BOTTOM,
        CENTER_VERTICAL
    }

    public enum CoverFlowLayoutMode {
        MATCH_PARENT,
        WRAP_CONTENT
    }

    public interface CoverFlowListener<V extends CoverFlowAdapter> {
        void imageOnTop(CoverFlowView<V> coverFlowView, int i, float f, float f2, float f3, float f4);

        void invalidationCompleted();

        void topImageClicked(CoverFlowView<V> coverFlowView, int i);
    }

    private class LongClickRunnable implements Runnable {
        private int position;

        private LongClickRunnable() {
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public void run() {
            if (CoverFlowView.this.mLongClickListener != null) {
                CoverFlowView.this.mLongClickListener.onLongClick(this.position);
                CoverFlowView.this.mLongClickTriggled = true;
            }
        }
    }

    class RecycleBin {
        @SuppressLint({"NewApi"})
        final LruCache<Integer, Bitmap> bitmapCache = new LruCache<Integer, Bitmap>(getCacheSize(CoverFlowView.this.getContext())) {
            protected int sizeOf(Integer key, Bitmap bitmap) {
                if (VERSION.SDK_INT < 12) {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
                return bitmap.getByteCount();
            }

            protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
                if (evicted && oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };

        RecycleBin() {
        }

        public Bitmap getCachedBitmap(int position) {
            return (Bitmap) this.bitmapCache.get(Integer.valueOf(position));
        }

        public void addBitmap2Cache(int position, Bitmap b) {
            this.bitmapCache.put(Integer.valueOf(position), b);
            Runtime.getRuntime().gc();
        }

        public Bitmap removeCachedBitmap(int position) {
            if (position < 0 || position >= this.bitmapCache.size()) {
                return null;
            }
            return (Bitmap) this.bitmapCache.remove(Integer.valueOf(position));
        }

        public void clear() {
            this.bitmapCache.evictAll();
        }

        private int getCacheSize(Context context) {
            int cacheSize = (1048576 * ((ActivityManager) context.getSystemService("activity")).getMemoryClass()) / 21;
            Log.e("View", "cacheSize == " + cacheSize);
            return cacheSize;
        }
    }

    public interface TopImageLongClickListener {
        void onLongClick(int i);
    }

    public CoverFlowView(Context context) {
        super(context);
        init();
    }

    public CoverFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributes(context, attrs);
        init();
    }

    public CoverFlowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttributes(context, attrs);
        init();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageCoverFlowView);
        int totalVisibleChildren = a.getInt(LONG_CLICK_DELAY, 2);
        if (totalVisibleChildren % 2 == 0) {
            throw new IllegalArgumentException("visible image must be an odd number");
        }
        this.VISIBLE_VIEWS = totalVisibleChildren >> 1;
        this.reflectHeightFraction = a.getFraction(1, 100, LONG_CLICK_DELAY, 0.0f);
        if (this.reflectHeightFraction > 100.0f) {
            this.reflectHeightFraction = 100.0f;
        }
        this.reflectHeightFraction /= 100.0f;
        this.reflectGap = a.getDimensionPixelSize(2, LONG_CLICK_DELAY);
        this.mGravity = CoverFlowGravity.values()[a.getInt(4, CoverFlowGravity.CENTER_VERTICAL.ordinal())];
        this.mLayoutMode = CoverFlowLayoutMode.values()[a.getInt(TOUCH_MINIMUM_MOVE, CoverFlowLayoutMode.WRAP_CONTENT.ordinal())];
        a.recycle();
    }

    private void init() {
        setWillNotDraw(false);
        setClickable(true);
        this.mChildTransfromer = new Matrix();
        this.mReflectionTransfromer = new Matrix();
        this.mTouchRect = new RectF();
        this.mImageRecorder = new SparseArray();
        this.mDrawChildPaint = new Paint();
        this.mDrawChildPaint.setAntiAlias(true);
        this.mDrawChildPaint.setFlags(1);
        this.mCoverFlowPadding = new Rect();
        this.mDrawFilter = new PaintFlagsDrawFilter(LONG_CLICK_DELAY, 3);
        this.mScroller = new Scroller(getContext(), new AccelerateDecelerateInterpolator());
        this.mRemoveReflectionPendingArray = new ArrayList();
    }

    public void setAdapter(T adapter) {
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            this.mItemCount = this.mAdapter.getCount();
            if (this.mItemCount < (this.VISIBLE_VIEWS << 1) + 1) {
                throw new IllegalArgumentException("total count in adapter must larger than visible images!");
            }
            this.mRecycler = new RecycleBin();
        }
        resetList();
        requestLayout();
    }

    public T getAdapter() {
        return this.mAdapter;
    }

    public void setCoverFlowListener(CoverFlowListener<T> l) {
        this.mCoverFlowListener = l;
    }

    private void resetList() {
        if (this.mRecycler != null) {
            this.mRecycler.clear();
        }
        this.mChildHeight = LONG_CLICK_DELAY;
        this.mOffset = 0.0f;
        this.mLastOffset = NO_POSITION;
        this.STANDARD_ALPHA = 179 / this.VISIBLE_VIEWS;
        if (this.mGravity == null) {
            this.mGravity = CoverFlowGravity.CENTER_VERTICAL;
        }
        if (this.mLayoutMode == null) {
            this.mLayoutMode = CoverFlowLayoutMode.WRAP_CONTENT;
        }
        this.mImageRecorder.clear();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mAdapter != null) {
            this.mCoverFlowPadding.left = getPaddingLeft();
            this.mCoverFlowPadding.right = getPaddingRight();
            this.mCoverFlowPadding.top = getPaddingTop();
            this.mCoverFlowPadding.bottom = getPaddingBottom();
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int visibleCount = (this.VISIBLE_VIEWS << 1) + 1;
            int avaiblableHeight = (heightSize - this.mCoverFlowPadding.top) - this.mCoverFlowPadding.bottom;
            int maxChildTotalHeight = LONG_CLICK_DELAY;
            for (int i = LONG_CLICK_DELAY; i < visibleCount; i++) {
                int childHeight = this.mAdapter.getImage(i).getHeight();
                int childTotalHeight = (int) ((((float) childHeight) + (((float) childHeight) * this.reflectHeightFraction)) + ((float) this.reflectGap));
                if (maxChildTotalHeight < childTotalHeight) {
                    maxChildTotalHeight = childTotalHeight;
                }
            }
            if (heightMode == 1073741824 || heightMode == Integer.MIN_VALUE) {
                if (avaiblableHeight < maxChildTotalHeight) {
                    this.mChildHeight = avaiblableHeight;
                } else if (this.mLayoutMode == CoverFlowLayoutMode.MATCH_PARENT) {
                    this.mChildHeight = avaiblableHeight;
                } else if (this.mLayoutMode == CoverFlowLayoutMode.WRAP_CONTENT) {
                    this.mChildHeight = maxChildTotalHeight;
                    if (heightMode == Integer.MIN_VALUE) {
                        heightSize = (this.mChildHeight + this.mCoverFlowPadding.top) + this.mCoverFlowPadding.bottom;
                    }
                }
            } else if (this.mLayoutMode == CoverFlowLayoutMode.MATCH_PARENT) {
                this.mChildHeight = avaiblableHeight;
            } else if (this.mLayoutMode == CoverFlowLayoutMode.WRAP_CONTENT) {
                this.mChildHeight = maxChildTotalHeight;
                heightSize = (this.mChildHeight + this.mCoverFlowPadding.top) + this.mCoverFlowPadding.bottom;
            }
            if (this.mGravity == CoverFlowGravity.CENTER_VERTICAL) {
                this.mChildTranslateY = (heightSize >> 1) - (this.mChildHeight >> 1);
            } else if (this.mGravity == CoverFlowGravity.TOP) {
                this.mChildTranslateY = this.mCoverFlowPadding.top;
            } else if (this.mGravity == CoverFlowGravity.BOTTOM) {
                this.mChildTranslateY = (heightSize - this.mCoverFlowPadding.bottom) - this.mChildHeight;
            }
            this.mReflectionTranslateY = (int) (((float) (this.mChildTranslateY + this.mChildHeight)) - (((float) this.mChildHeight) * this.reflectHeightFraction));
            setMeasuredDimension(widthSize, heightSize);
            this.mVisibleChildCount = visibleCount;
            this.mWidth = widthSize;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    protected void onDraw(Canvas canvas) {
        if (this.mAdapter == null) {
            super.onDraw(canvas);
            return;
        }
        int rightChild;
        int i;
        this.mDrawing = true;
        canvas.setDrawFilter(this.mDrawFilter);
        float offset = this.mOffset;
        int mid = (int) Math.floor(((double) offset) + 0.5d);
        if (this.mVisibleChildCount % 2 == 0) {
            rightChild = (this.mVisibleChildCount >> 1) + NO_POSITION;
        } else {
            rightChild = this.mVisibleChildCount >> 1;
        }
        for (i = mid - (this.mVisibleChildCount >> 1); i < mid; i++) {
            drawChild(canvas, i, ((float) i) - offset);
        }
        for (i = mid + rightChild; i >= mid; i += NO_POSITION) {
            drawChild(canvas, i, ((float) i) - offset);
        }
        if (this.mLastOffset != ((int) offset)) {
            imageOnTop(getActuallyPosition((int) offset));
            this.mLastOffset = (int) offset;
        }
        this.mDrawing = false;
        int removeCount = this.mRemoveReflectionPendingArray.size();
        for (i = LONG_CLICK_DELAY; i < removeCount; i++) {
            this.mRecycler.removeCachedBitmap(((Integer) this.mRemoveReflectionPendingArray.get(i)).intValue());
        }
        this.mRemoveReflectionPendingArray.clear();
        super.onDraw(canvas);
        this.mCoverFlowListener.invalidationCompleted();
    }

    protected final void drawChild(Canvas canvas, int position, float offset) {
        int actuallyPosition = getActuallyPosition(position);
        Bitmap child = this.mAdapter.getImage(actuallyPosition);
        Bitmap reflection = obtainReflection(actuallyPosition, child);
        int[] wAndh = (int[]) this.mImageRecorder.get(actuallyPosition);
        if (wAndh == null) {
            this.mImageRecorder.put(actuallyPosition, new int[]{child.getWidth(), child.getHeight()});
        } else {
            wAndh[LONG_CLICK_DELAY] = child.getWidth();
            wAndh[1] = child.getHeight();
        }
        if (child != null && !child.isRecycled() && canvas != null) {
            makeChildTransfromer(child, position, offset);
            canvas.drawBitmap(child, this.mChildTransfromer, this.mDrawChildPaint);
            if (reflection != null) {
                canvas.drawBitmap(reflection, this.mReflectionTransfromer, this.mDrawChildPaint);
            }
        }
    }

    private void makeChildTransfromer(Bitmap child, int position, float offset) {
        float scale;
        float translateX;
        this.mChildTransfromer.reset();
        this.mReflectionTransfromer.reset();
        if (position != 0) {
            scale = MOVE_SPEED_MULTIPLE - (Math.abs(offset) * 0.25f);
        } else {
            scale = MOVE_SPEED_MULTIPLE - (Math.abs(offset) * CARD_SCALE);
        }
        int childTotalHeight = (int) ((((float) child.getHeight()) + (((float) child.getHeight()) * this.reflectHeightFraction)) + ((float) this.reflectGap));
        float originalChildHeightScale = ((float) ((int) ((((float) this.mChildHeight) - (((float) this.mChildHeight) * this.reflectHeightFraction)) - ((float) this.reflectGap)))) / ((float) child.getHeight());
        float childHeightScale = originalChildHeightScale * scale;
        int childWidth = (int) (((float) child.getWidth()) * childHeightScale);
        int centerChildWidth = (int) (((float) child.getWidth()) * originalChildHeightScale);
        int leftSpace = ((this.mWidth >> 1) - this.mCoverFlowPadding.left) - (centerChildWidth >> 1);
        int rightSpace = ((this.mWidth >> 1) - this.mCoverFlowPadding.right) - (centerChildWidth >> 1);
        if (offset <= 0.0f) {
            translateX = ((((float) leftSpace) / ((float) this.VISIBLE_VIEWS)) * (((float) this.VISIBLE_VIEWS) + offset)) + ((float) this.mCoverFlowPadding.left);
        } else {
            translateX = ((((float) this.mWidth) - ((((float) rightSpace) / ((float) this.VISIBLE_VIEWS)) * (((float) this.VISIBLE_VIEWS) - offset))) - ((float) childWidth)) - ((float) this.mCoverFlowPadding.right);
        }
        float alpha = 254.0f - (Math.abs(offset) * ((float) this.STANDARD_ALPHA));
        if (alpha < 0.0f) {
            alpha = 0.0f;
        } else if (alpha > 254.0f) {
            alpha = 254.0f;
        }
        this.mDrawChildPaint.setAlpha((int) alpha);
        this.mChildTransfromer.preTranslate(0.0f, (float) (-(childTotalHeight >> 1)));
        this.mChildTransfromer.postScale(childHeightScale, childHeightScale);
        float adjustedChildTranslateY = 0.0f;
        if (childHeightScale != MOVE_SPEED_MULTIPLE) {
            adjustedChildTranslateY = (float) ((this.mChildHeight - childTotalHeight) >> 1);
        }
        this.mChildTransfromer.postTranslate(translateX, ((float) this.mChildTranslateY) + adjustedChildTranslateY);
        getCustomTransformMatrix(this.mChildTransfromer, this.mDrawChildPaint, child, position, offset);
        this.mChildTransfromer.postTranslate(0.0f, (float) (childTotalHeight >> 1));
        this.mReflectionTransfromer.preTranslate(0.0f, (float) (-(childTotalHeight >> 1)));
        this.mReflectionTransfromer.postScale(childHeightScale, childHeightScale);
        this.mReflectionTransfromer.postTranslate(translateX, (((float) this.mReflectionTranslateY) * scale) + adjustedChildTranslateY);
        getCustomTransformMatrix(this.mReflectionTransfromer, this.mDrawChildPaint, child, position, offset);
        this.mReflectionTransfromer.postTranslate(0.0f, (float) (childTotalHeight >> 1));
    }

    protected void getCustomTransformMatrix(Matrix transfromer, Paint mDrawChildPaint, Bitmap child, int position, float offset) {
    }

    private void imageOnTop(int position) {
        this.mTopImageIndex = position;
        int[] wAndh = (int[]) this.mImageRecorder.get(position);
        int widthInView = (int) (((float) wAndh[LONG_CLICK_DELAY]) * (((float) ((int) ((((float) this.mChildHeight) - (((float) this.mChildHeight) * this.reflectHeightFraction)) - ((float) this.reflectGap)))) / ((float) wAndh[1])));
        Log.e("View", "height ==>" + 652 + " width ==>" + 540);
        this.mTouchRect.left = (float) ((this.mWidth >> 1) - 270);
        this.mTouchRect.top = (float) this.mChildTranslateY;
        this.mTouchRect.right = this.mTouchRect.left + ((float) 540);
        this.mTouchRect.bottom = this.mTouchRect.top + ((float) 652);
        Log.e("View", "rect==>" + this.mTouchRect);
        if (this.mCoverFlowListener != null) {
            this.mCoverFlowListener.imageOnTop(this, position, this.mTouchRect.left, this.mTouchRect.top, this.mTouchRect.right, this.mTouchRect.bottom);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getAction()) {
            case LONG_CLICK_DELAY /*?: ONE_ARG  (wrap: int
  0x0004: INVOKE  (r0_1 int) =  android.view.ViewConfiguration.getLongPressTimeout():int type: STATIC)*/:
                if (this.mScroller.computeScrollOffset()) {
                    this.mScroller.abortAnimation();
                    invalidate();
                }
                stopLongClick();
                triggleLongClick(event.getX(), event.getY());
                touchBegan(event);
                return true;
            case TwitterResponse.READ /*1*/:
                touchEnded(event);
                stopLongClick();
                return true;
            case TwitterResponse.READ_WRITE /*2*/:
                touchMoved(event);
                return true;
            default:
                return false;
        }
    }

    private void triggleLongClick(float x, float y) {
        if (this.mTouchRect.contains(x, y) && this.mLongClickListener != null && this.topImageClickEnable && !this.mLongClickPosted) {
            this.mLongClickRunnable.setPosition(this.mTopImageIndex);
            postDelayed(this.mLongClickRunnable, (long) LONG_CLICK_DELAY);
        }
    }

    private void stopLongClick() {
        if (this.mLongClickRunnable != null) {
            removeCallbacks(this.mLongClickRunnable);
            this.mLongClickPosted = false;
            this.mLongClickTriggled = false;
        }
    }

    private void touchBegan(MotionEvent event) {
        endAnimation();
        float x = event.getX();
        this.mTouchStartX = x;
        this.mTouchStartY = event.getY();
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
        this.mStartOffset = this.mOffset;
        this.mTouchMoved = false;
        this.mTouchStartPos = ((x / ((float) this.mWidth)) * MOVE_POS_MULTIPLE) - 5.0f;
        this.mTouchStartPos /= 2.0f;
        this.mVelocity = VelocityTracker.obtain();
        this.mVelocity.addMovement(event);
    }

    private void touchMoved(MotionEvent event) {
        float pos = (((event.getX() / ((float) this.mWidth)) * MOVE_POS_MULTIPLE) - 5.0f) / 2.0f;
        if (!this.mTouchMoved) {
            float dx = Math.abs(event.getX() - this.mTouchStartX);
            float dy = Math.abs(event.getY() - this.mTouchStartY);
            if (dx >= 5.0f || dy >= 5.0f) {
                this.mTouchMoved = true;
                stopLongClick();
            } else {
                return;
            }
        }
        this.mOffset = (this.mStartOffset + this.mTouchStartPos) - pos;
        invalidate();
        this.mVelocity.addMovement(event);
    }

    private void touchEnded(MotionEvent event) {
        float pos = (((event.getX() / ((float) this.mWidth)) * MOVE_POS_MULTIPLE) - 5.0f) / 2.0f;
        if (this.mTouchMoved || ((double) this.mOffset) - Math.floor((double) this.mOffset) != 0.0d) {
            this.mStartOffset += this.mTouchStartPos - pos;
            this.mOffset = this.mStartOffset;
            this.mVelocity.addMovement(event);
            this.mVelocity.computeCurrentVelocity(1000);
            double speed = (((double) this.mVelocity.getXVelocity()) / ((double) this.mWidth)) * 1.0d;
            if (speed > 6.0d) {
                speed = 6.0d;
            } else if (speed < -6.0d) {
                speed = -6.0d;
            }
            startAnimation(-speed);
        } else {
            Log.e("View", " touch ==>" + event.getX() + " , " + event.getY());
            if (!(this.mTouchRect == null || !this.mTouchRect.contains(event.getX(), event.getY()) || this.mCoverFlowListener == null || !this.topImageClickEnable || this.mLongClickTriggled)) {
                this.mCoverFlowListener.topImageClicked(this, this.mTopImageIndex);
            }
        }
        this.mVelocity.clear();
        this.mVelocity.recycle();
    }

    private void startAnimation(double speed) {
        if (this.mAnimationRunnable == null) {
            double delta = (speed * speed) / 20.0d;
            if (speed < 0.0d) {
                delta = -delta;
            }
            double nearest = Math.floor(0.5d + (((double) this.mStartOffset) + delta));
            this.mStartSpeed = (float) Math.sqrt((Math.abs(nearest - ((double) this.mStartOffset)) * 10.0d) * 2.0d);
            if (nearest < ((double) this.mStartOffset)) {
                this.mStartSpeed = -this.mStartSpeed;
            }
            this.mDuration = Math.abs(this.mStartSpeed / FRICTION);
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mAnimationRunnable = new Runnable() {
                public void run() {
                    CoverFlowView.this.driveAnimation();
                }
            };
            post(this.mAnimationRunnable);
        }
    }

    private void driveAnimation() {
        float elapsed = ((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / 1000.0f;
        if (elapsed >= this.mDuration) {
            endAnimation();
            return;
        }
        updateAnimationAtElapsed(elapsed);
        post(this.mAnimationRunnable);
    }

    private void endAnimation() {
        if (this.mAnimationRunnable != null) {
            this.mOffset = (float) Math.floor(((double) this.mOffset) + 0.5d);
            invalidate();
            removeCallbacks(this.mAnimationRunnable);
            this.mAnimationRunnable = null;
        }
    }

    private void updateAnimationAtElapsed(float elapsed) {
        if (elapsed > this.mDuration) {
            elapsed = this.mDuration;
        }
        float delta = (Math.abs(this.mStartSpeed) * elapsed) - (((FRICTION * elapsed) * elapsed) / 2.0f);
        if (this.mStartSpeed < 0.0f) {
            delta = -delta;
        }
        this.mOffset = this.mStartOffset + delta;
        invalidate();
    }

    public void invalidatePosition(int position) {
        if (this.mAdapter != null && position >= 0 && position < this.mAdapter.getCount()) {
            if (!this.mDrawing) {
                this.mRecycler.removeCachedBitmap(position);
            } else if (!this.mRemoveReflectionPendingArray.contains(Integer.valueOf(position))) {
                this.mRemoveReflectionPendingArray.add(Integer.valueOf(position));
            }
            if (position >= this.mTopImageIndex - this.VISIBLE_VIEWS && position <= this.mTopImageIndex + this.VISIBLE_VIEWS) {
                invalidate();
            }
        }
    }

    private int getActuallyPosition(int position) {
        if (this.mAdapter == null) {
            return NO_POSITION;
        }
        int max = this.mAdapter.getCount();
        position += this.VISIBLE_VIEWS;
        while (true) {
            if (position >= 0 && position < max) {
                return position;
            }
            if (position < 0) {
                position += max;
            } else if (position >= max) {
                position -= max;
            }
        }
    }

    private Bitmap obtainReflection(int position, Bitmap src) {
        if (this.reflectHeightFraction <= 0.0f) {
            return null;
        }
        Bitmap reflection = this.mRecycler.getCachedBitmap(position);
        if (reflection != null && !reflection.isRecycled()) {
            return reflection;
        }
        this.mRecycler.removeCachedBitmap(position);
        reflection = BitmapUtils.createReflectedBitmap(src, this.reflectHeightFraction);
        if (reflection == null) {
            return reflection;
        }
        this.mRecycler.addBitmap2Cache(position, reflection);
        return reflection;
    }

    public void setVisibleImage(int count) {
        if (count % 2 == 0) {
            throw new IllegalArgumentException("visible image must be an odd number");
        }
        this.VISIBLE_VIEWS = count / 2;
        this.STANDARD_ALPHA = 179 / this.VISIBLE_VIEWS;
    }

    public void setCoverFlowGravity(CoverFlowGravity gravity) {
        this.mGravity = gravity;
    }

    public void setCoverFlowLayoutMode(CoverFlowLayoutMode mode) {
        this.mLayoutMode = mode;
    }

    public void setReflectionHeight(int fraction) {
        if (fraction < 0) {
            fraction = LONG_CLICK_DELAY;
        } else if (fraction > 100) {
            fraction = 100;
        }
        this.reflectHeightFraction = (float) fraction;
    }

    public void setReflectionGap(int gap) {
        if (gap < 0) {
            gap = LONG_CLICK_DELAY;
        }
        this.reflectGap = gap;
    }

    public void disableTopImageClick() {
        this.topImageClickEnable = false;
    }

    public void enableTopImageClick() {
        this.topImageClickEnable = true;
    }

    public void setSelection(int position) {
        int max = this.mAdapter.getCount();
        if (position < 0 || position >= max) {
            throw new IllegalArgumentException("Position want to select can not less than 0 or larger than max of adapter provide!");
        } else if (this.mTopImageIndex != position) {
            if (this.mScroller.computeScrollOffset()) {
                this.mScroller.abortAnimation();
            }
            int from = (int) (this.mOffset * 100.0f);
            int disX = ((position - this.VISIBLE_VIEWS) * 100) - from;
            this.mScroller.startScroll(from, LONG_CLICK_DELAY, disX, LONG_CLICK_DELAY, Math.min(Math.abs((position + max) - this.mTopImageIndex), Math.abs(position - this.mTopImageIndex)) * DURATION);
            invalidate();
        }
    }

    public void computeScroll() {
        super.computeScroll();
        if (this.mScroller.computeScrollOffset()) {
            this.mOffset = ((float) this.mScroller.getCurrX()) / 100.0f;
            invalidate();
        }
    }

    public void setTopImageLongClickListener(TopImageLongClickListener listener) {
        this.mLongClickListener = listener;
        if (listener == null) {
            this.mLongClickRunnable = null;
        } else if (this.mLongClickRunnable == null) {
            this.mLongClickRunnable = new LongClickRunnable();
        }
    }
}
