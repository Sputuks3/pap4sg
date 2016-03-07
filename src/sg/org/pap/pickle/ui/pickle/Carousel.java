package sg.org.pap.pickle.ui.pickle;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.Scroller;
import com.marshalchen.ultimaterecyclerview.itemTouchHelper.SimpleItemTouchHelperCallback;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class Carousel extends ViewGroup {
    protected static final int LAYOUT_MODE_AFTER = 0;
    protected static final int LAYOUT_MODE_TO_BEFORE = 1;
    protected static final int TOUCH_STATE_ALIGN = 3;
    protected static final int TOUCH_STATE_FLING = 2;
    protected static final int TOUCH_STATE_RESTING = 0;
    protected static final int TOUCH_STATE_SCROLLING = 1;
    protected final int NO_VALUE;
    protected Adapter mAdapter;
    protected final ViewCache<View> mCache;
    protected int mChildHeight;
    protected int mChildWidth;
    private final DataSetObserver mDataObserver;
    private int mFirstVisibleChild;
    private float mLastMotionX;
    private int mLastVisibleChild;
    protected int mLeftEdge;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private OnItemSelectedListener mOnItemSelectedListener;
    private int mReverseOrderIndex;
    protected int mRightEdge;
    private final Scroller mScroller;
    private int mSelection;
    private int mSlowDownCoefficient;
    protected float mSpacing;
    protected int mTouchSlop;
    protected int mTouchState;
    private VelocityTracker mVelocityTracker;

    public interface OnItemSelectedListener {
        void onItemSelected(View view, int i);
    }

    protected static class ViewCache<T extends View> {
        private final LinkedList<WeakReference<T>> mCachedItemViews = new LinkedList();

        protected ViewCache() {
        }

        public T getCachedView() {
            if (this.mCachedItemViews.size() == 0) {
                return null;
            }
            View v;
            do {
                v = (View) ((WeakReference) this.mCachedItemViews.removeFirst()).get();
                if (v != null) {
                    return v;
                }
            } while (this.mCachedItemViews.size() != 0);
            return v;
        }

        public void cacheView(T v) {
            this.mCachedItemViews.addLast(new WeakReference(v));
        }
    }

    public Carousel(Context context) {
        this(context, null);
    }

    public Carousel(Context context, AttributeSet attrs) {
        this(context, attrs, TOUCH_STATE_RESTING);
    }

    public Carousel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.NO_VALUE = -2147481871;
        this.mScroller = new Scroller(getContext());
        this.mTouchState = TOUCH_STATE_RESTING;
        this.mDataObserver = new DataSetObserver() {
            public void onChanged() {
                Carousel.this.reset();
            }

            public void onInvalidated() {
                Carousel.this.removeAllViews();
                Carousel.this.invalidate();
            }
        };
        this.mSpacing = 0.5f;
        this.mReverseOrderIndex = -1;
        this.mSlowDownCoefficient = TOUCH_STATE_SCROLLING;
        this.mChildWidth = 240;
        this.mChildHeight = 360;
        this.mCache = new ViewCache();
        this.mRightEdge = -2147481871;
        this.mLeftEdge = -2147481871;
        setChildrenDrawingOrderEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public Adapter getAdapter() {
        return this.mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataObserver);
        }
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(this.mDataObserver);
        reset();
    }

    public View getSelectedView() {
        return getChildAt(this.mReverseOrderIndex);
    }

    public int getSelection() {
        return this.mSelection;
    }

    public void setSelection(int position) {
        if (this.mAdapter == null) {
            throw new IllegalStateException("You are trying to set selection on widget without adapter");
        } else if (position < 0 || position > this.mAdapter.getCount() - 1) {
            throw new IllegalArgumentException("Position index must be in range of adapter values (0 - getCount()-1)");
        } else {
            this.mSelection = position;
            reset();
        }
    }

    public void computeScroll() {
        int centerItemLeft = (getWidth() / TOUCH_STATE_FLING) - (this.mChildWidth / TOUCH_STATE_FLING);
        int centerItemRight = (getWidth() / TOUCH_STATE_FLING) + (this.mChildWidth / TOUCH_STATE_FLING);
        if (this.mRightEdge != -2147481871 && this.mScroller.getFinalX() > this.mRightEdge - centerItemRight) {
            this.mScroller.setFinalX(this.mRightEdge - centerItemRight);
        }
        if (this.mLeftEdge != -2147481871 && this.mScroller.getFinalX() < this.mLeftEdge - centerItemLeft) {
            this.mScroller.setFinalX(this.mLeftEdge - centerItemLeft);
        }
        if (this.mScroller.computeScrollOffset()) {
            if (this.mScroller.getFinalX() == this.mScroller.getCurrX()) {
                this.mScroller.abortAnimation();
                this.mTouchState = TOUCH_STATE_RESTING;
                clearChildrenCache();
            } else {
                scrollTo(this.mScroller.getCurrX(), TOUCH_STATE_RESTING);
                postInvalidate();
            }
        } else if (this.mTouchState == TOUCH_STATE_FLING) {
            this.mTouchState = TOUCH_STATE_RESTING;
            clearChildrenCache();
        }
        refill();
        updateReverseOrderIndex();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (this.mAdapter != null && this.mAdapter.getCount() != 0) {
            View v = null;
            if (getChildCount() == 0) {
                v = getViewFromAdapter(this.mSelection);
                addAndMeasureChild(v, TOUCH_STATE_RESTING);
                int left = (getWidth() / TOUCH_STATE_FLING) - (v.getMeasuredWidth() / TOUCH_STATE_FLING);
                int right = left + v.getMeasuredWidth();
                int top = (getHeight() / TOUCH_STATE_FLING) - (v.getMeasuredHeight() / TOUCH_STATE_FLING);
                v.layout(left, top, right, top + v.getMeasuredHeight());
                this.mFirstVisibleChild = this.mSelection;
                this.mLastVisibleChild = this.mSelection;
                if (this.mLastVisibleChild == this.mAdapter.getCount() - 1) {
                    this.mRightEdge = right;
                }
                if (this.mFirstVisibleChild == 0) {
                    this.mLeftEdge = left;
                }
            }
            refill();
            if (v != null) {
                this.mReverseOrderIndex = indexOfChild(v);
                v.setSelected(true);
                return;
            }
            updateReverseOrderIndex();
        }
    }

    private void updateReverseOrderIndex() {
        int oldReverseIndex = this.mReverseOrderIndex;
        int screenCenter = (getWidth() / TOUCH_STATE_FLING) + getScrollX();
        int c = getChildCount();
        int minDiff = Integer.MAX_VALUE;
        int minDiffIndex = -1;
        for (int i = TOUCH_STATE_RESTING; i < c; i += TOUCH_STATE_SCROLLING) {
            int diff = Math.abs(screenCenter - getChildCenter(i));
            if (diff < minDiff) {
                minDiff = diff;
                minDiffIndex = i;
            }
        }
        if (minDiff != Integer.MAX_VALUE) {
            this.mReverseOrderIndex = minDiffIndex;
        }
        if (oldReverseIndex != this.mReverseOrderIndex) {
            View oldSelected = getChildAt(oldReverseIndex);
            View newSelected = getChildAt(this.mReverseOrderIndex);
            oldSelected.setSelected(false);
            newSelected.setSelected(true);
            this.mSelection = this.mFirstVisibleChild + this.mReverseOrderIndex;
            if (this.mOnItemSelectedListener != null) {
                this.mOnItemSelectedListener.onItemSelected(newSelected, this.mSelection);
            }
        }
    }

    protected int layoutChildToBefore(View v, int right) {
        int t = (getHeight() / TOUCH_STATE_FLING) - (v.getMeasuredHeight() / TOUCH_STATE_FLING);
        int r = right;
        v.layout(right - v.getMeasuredWidth(), t, r, t + v.getMeasuredHeight());
        return r - ((int) (((float) v.getMeasuredWidth()) * this.mSpacing));
    }

    protected int layoutChild(View v, int left) {
        int l = left;
        int t = (getHeight() / TOUCH_STATE_FLING) - (v.getMeasuredHeight() / TOUCH_STATE_FLING);
        v.layout(l, t, l + v.getMeasuredWidth(), t + v.getMeasuredHeight());
        return ((int) (((float) v.getMeasuredWidth()) * this.mSpacing)) + l;
    }

    protected View addAndMeasureChild(View child, int layoutMode) {
        if (child.getLayoutParams() == null) {
            child.setLayoutParams(new LayoutParams(this.mChildWidth, this.mChildHeight));
        }
        addViewInLayout(child, layoutMode == TOUCH_STATE_SCROLLING ? TOUCH_STATE_RESTING : -1, child.getLayoutParams(), true);
        measureChild(child, MeasureSpec.makeMeasureSpec(this.mChildWidth, 1073741824), MeasureSpec.makeMeasureSpec(this.mChildHeight, 1073741824));
        child.setDrawingCacheEnabled(isChildrenDrawnWithCacheEnabled());
        return child;
    }

    private void reset() {
        if (this.mAdapter != null && this.mAdapter.getCount() != 0) {
            if (getChildCount() == 0) {
                requestLayout();
                return;
            }
            View selectedView = getChildAt(this.mReverseOrderIndex);
            int selectedLeft = selectedView.getLeft();
            int selectedTop = selectedView.getTop();
            removeAllViewsInLayout();
            this.mRightEdge = -2147481871;
            this.mLeftEdge = -2147481871;
            View v = this.mAdapter.getView(this.mSelection, null, this);
            addAndMeasureChild(v, TOUCH_STATE_RESTING);
            this.mReverseOrderIndex = TOUCH_STATE_RESTING;
            int right = selectedLeft + v.getMeasuredWidth();
            v.layout(selectedLeft, selectedTop, right, selectedTop + v.getMeasuredHeight());
            this.mFirstVisibleChild = this.mSelection;
            this.mLastVisibleChild = this.mSelection;
            if (this.mLastVisibleChild == this.mAdapter.getCount() - 1) {
                this.mRightEdge = right;
            }
            if (this.mFirstVisibleChild == 0) {
                this.mLeftEdge = selectedLeft;
            }
            refill();
            this.mReverseOrderIndex = indexOfChild(v);
            v.setSelected(true);
        }
    }

    protected void refill() {
        if (this.mAdapter != null && getChildCount() != 0) {
            int leftScreenEdge = getScrollX();
            int rightScreenEdge = leftScreenEdge + getWidth();
            removeNonVisibleViewsLeftToRight(leftScreenEdge);
            removeNonVisibleViewsRightToLeft(rightScreenEdge);
            refillLeftToRight(leftScreenEdge, rightScreenEdge);
            refillRightToLeft(leftScreenEdge);
        }
    }

    protected int getPartOfViewCoveredBySibling() {
        return (int) (((float) this.mChildWidth) * (SimpleItemTouchHelperCallback.ALPHA_FULL - this.mSpacing));
    }

    protected View getViewFromAdapter(int position) {
        return this.mAdapter.getView(position, this.mCache.getCachedView(), this);
    }

    protected void refillRightToLeft(int leftScreenEdge) {
        if (getChildCount() != 0) {
            int newRight = getChildAt(TOUCH_STATE_RESTING).getRight() - ((int) (((float) this.mChildWidth) * this.mSpacing));
            while (newRight - getPartOfViewCoveredBySibling() > leftScreenEdge && this.mFirstVisibleChild > 0) {
                this.mFirstVisibleChild--;
                View child = getViewFromAdapter(this.mFirstVisibleChild);
                child.setSelected(false);
                this.mReverseOrderIndex += TOUCH_STATE_SCROLLING;
                addAndMeasureChild(child, TOUCH_STATE_SCROLLING);
                newRight = layoutChildToBefore(child, newRight);
                if (this.mFirstVisibleChild <= 0) {
                    this.mLeftEdge = child.getLeft();
                }
            }
        }
    }

    protected void refillLeftToRight(int leftScreenEdge, int rightScreenEdge) {
        int newLeft = getChildAt(getChildCount() - 1).getLeft() + ((int) (((float) this.mChildWidth) * this.mSpacing));
        while (getPartOfViewCoveredBySibling() + newLeft < rightScreenEdge && this.mLastVisibleChild < this.mAdapter.getCount() - 1) {
            this.mLastVisibleChild += TOUCH_STATE_SCROLLING;
            View child = getViewFromAdapter(this.mLastVisibleChild);
            child.setSelected(false);
            addAndMeasureChild(child, TOUCH_STATE_RESTING);
            newLeft = layoutChild(child, newLeft);
            if (this.mLastVisibleChild >= this.mAdapter.getCount() - 1) {
                this.mRightEdge = child.getRight();
            }
        }
    }

    protected void removeNonVisibleViewsLeftToRight(int leftScreenEdge) {
        if (getChildCount() != 0) {
            View firstChild = getChildAt(TOUCH_STATE_RESTING);
            while (firstChild != null && ((float) firstChild.getLeft()) + (((float) this.mChildWidth) * this.mSpacing) < ((float) leftScreenEdge) && getChildCount() > TOUCH_STATE_SCROLLING) {
                removeViewsInLayout(TOUCH_STATE_RESTING, TOUCH_STATE_SCROLLING);
                this.mCache.cacheView(firstChild);
                this.mFirstVisibleChild += TOUCH_STATE_SCROLLING;
                this.mReverseOrderIndex--;
                if (this.mReverseOrderIndex == 0) {
                    return;
                }
                if (getChildCount() > TOUCH_STATE_SCROLLING) {
                    firstChild = getChildAt(TOUCH_STATE_RESTING);
                } else {
                    firstChild = null;
                }
            }
        }
    }

    protected void removeNonVisibleViewsRightToLeft(int rightScreenEdge) {
        if (getChildCount() != 0) {
            View lastChild = getChildAt(getChildCount() - 1);
            while (lastChild != null && ((float) lastChild.getRight()) - (((float) this.mChildWidth) * this.mSpacing) > ((float) rightScreenEdge) && getChildCount() > TOUCH_STATE_SCROLLING) {
                removeViewsInLayout(getChildCount() - 1, TOUCH_STATE_SCROLLING);
                this.mCache.cacheView(lastChild);
                this.mLastVisibleChild--;
                if (getChildCount() - 1 == this.mReverseOrderIndex) {
                    return;
                }
                if (getChildCount() > TOUCH_STATE_SCROLLING) {
                    lastChild = getChildAt(getChildCount() - 1);
                } else {
                    lastChild = null;
                }
            }
        }
    }

    protected int getChildCenter(View v) {
        return v.getLeft() + ((v.getRight() - v.getLeft()) / TOUCH_STATE_FLING);
    }

    protected int getChildCenter(int i) {
        return getChildCenter(getChildAt(i));
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        return i < this.mReverseOrderIndex ? i : (childCount - 1) - (i - this.mReverseOrderIndex);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == TOUCH_STATE_FLING && this.mTouchState == TOUCH_STATE_SCROLLING) {
            return true;
        }
        float x = ev.getX();
        float y = ev.getY();
        switch (action) {
            case TOUCH_STATE_RESTING /*0*/:
                this.mLastMotionX = x;
                this.mTouchState = this.mScroller.isFinished() ? TOUCH_STATE_RESTING : TOUCH_STATE_SCROLLING;
                break;
            case TOUCH_STATE_SCROLLING /*1*/:
                this.mTouchState = TOUCH_STATE_RESTING;
                clearChildrenCache();
                break;
            case TOUCH_STATE_FLING /*2*/:
                boolean xMoved;
                if (((int) Math.abs(x - this.mLastMotionX)) > this.mTouchSlop) {
                    xMoved = true;
                } else {
                    xMoved = false;
                }
                if (xMoved) {
                    this.mTouchState = TOUCH_STATE_SCROLLING;
                    enableChildrenCache();
                    cancelLongPress();
                    break;
                }
                break;
        }
        if (this.mTouchState != TOUCH_STATE_SCROLLING) {
            return false;
        }
        return true;
    }

    protected void scrollByDelta(int deltaX) {
        int rightInPixels;
        int leftInPixels;
        deltaX /= this.mSlowDownCoefficient;
        int centerItemLeft = (getWidth() / TOUCH_STATE_FLING) - (this.mChildWidth / TOUCH_STATE_FLING);
        int centerItemRight = (getWidth() / TOUCH_STATE_FLING) + (this.mChildWidth / TOUCH_STATE_FLING);
        if (this.mRightEdge == -2147481871) {
            rightInPixels = Integer.MAX_VALUE;
        } else {
            rightInPixels = this.mRightEdge;
        }
        if (this.mLeftEdge == -2147481871) {
            leftInPixels = Integer.MIN_VALUE + getWidth();
        } else {
            leftInPixels = this.mLeftEdge;
        }
        int x = getScrollX() + deltaX;
        if (x < leftInPixels - centerItemLeft) {
            deltaX -= x - (leftInPixels - centerItemLeft);
        } else if (x > rightInPixels - centerItemRight) {
            deltaX -= x - (rightInPixels - centerItemRight);
        }
        scrollBy(deltaX, TOUCH_STATE_RESTING);
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean xMoved = false;
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(event);
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case TOUCH_STATE_RESTING /*0*/:
                if (!this.mScroller.isFinished()) {
                    this.mScroller.forceFinished(true);
                }
                this.mLastMotionX = x;
                break;
            case TOUCH_STATE_SCROLLING /*1*/:
                if (this.mTouchState != TOUCH_STATE_SCROLLING) {
                    clearChildrenCache();
                    this.mTouchState = TOUCH_STATE_RESTING;
                    break;
                }
                this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                int initialXVelocity = (int) this.mVelocityTracker.getXVelocity();
                int initialYVelocity = (int) this.mVelocityTracker.getYVelocity();
                if (Math.abs(initialXVelocity) + Math.abs(initialYVelocity) > this.mMinimumVelocity) {
                    fling(-initialXVelocity, -initialYVelocity);
                } else {
                    clearChildrenCache();
                    this.mTouchState = TOUCH_STATE_RESTING;
                }
                if (this.mVelocityTracker != null) {
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    break;
                }
                break;
            case TOUCH_STATE_FLING /*2*/:
                if (this.mTouchState != TOUCH_STATE_SCROLLING) {
                    if (((int) Math.abs(x - this.mLastMotionX)) > this.mTouchSlop) {
                        xMoved = true;
                    }
                    if (xMoved) {
                        this.mTouchState = TOUCH_STATE_SCROLLING;
                        enableChildrenCache();
                        cancelLongPress();
                        break;
                    }
                }
                int deltaX = (int) (this.mLastMotionX - x);
                this.mLastMotionX = x;
                scrollByDelta(deltaX);
                break;
                break;
            case TOUCH_STATE_ALIGN /*3*/:
                this.mTouchState = TOUCH_STATE_RESTING;
                break;
        }
        return true;
    }

    public void fling(int velocityX, int velocityY) {
        int rightInPixels;
        int leftInPixels;
        velocityX /= this.mSlowDownCoefficient;
        this.mTouchState = TOUCH_STATE_FLING;
        int x = getScrollX();
        int y = getScrollY();
        int centerItemLeft = (getWidth() / TOUCH_STATE_FLING) - (this.mChildWidth / TOUCH_STATE_FLING);
        int centerItemRight = (getWidth() / TOUCH_STATE_FLING) + (this.mChildWidth / TOUCH_STATE_FLING);
        if (this.mRightEdge == -2147481871) {
            rightInPixels = Integer.MAX_VALUE;
        } else {
            rightInPixels = this.mRightEdge;
        }
        if (this.mLeftEdge == -2147481871) {
            leftInPixels = Integer.MIN_VALUE + getWidth();
        } else {
            leftInPixels = this.mLeftEdge;
        }
        int i = velocityX;
        int i2 = velocityY;
        this.mScroller.fling(x, y, i, i2, leftInPixels - centerItemLeft, (rightInPixels - centerItemRight) + TOUCH_STATE_SCROLLING, TOUCH_STATE_RESTING, TOUCH_STATE_RESTING);
        invalidate();
    }

    private void enableChildrenCache() {
        setChildrenDrawnWithCacheEnabled(true);
        setChildrenDrawingCacheEnabled(true);
    }

    private void clearChildrenCache() {
        setChildrenDrawnWithCacheEnabled(false);
    }

    public void setSpacing(float spacing) {
        this.mSpacing = spacing;
    }

    public void setChildWidth(int width) {
        this.mChildWidth = width;
    }

    public void setChildHeight(int height) {
        this.mChildHeight = height;
    }

    public void setSlowDownCoefficient(int c) {
        if (c < TOUCH_STATE_SCROLLING) {
            throw new IllegalArgumentException("Slowdown coeficient must be greater than 0");
        }
        this.mSlowDownCoefficient = c;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }
}
