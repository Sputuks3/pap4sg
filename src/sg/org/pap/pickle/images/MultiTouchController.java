package sg.org.pap.pickle.images;

import android.util.Log;
import android.view.MotionEvent;
import com.marshalchen.ultimaterecyclerview.itemTouchHelper.SimpleItemTouchHelperCallback;
import java.lang.reflect.Method;

public class MultiTouchController<T> {
    private static int ACTION_POINTER_INDEX_SHIFT = 0;
    private static int ACTION_POINTER_UP = 0;
    public static final boolean DEBUG = false;
    private static final long EVENT_SETTLE_TIME_INTERVAL = 20;
    private static final float MAX_MULTITOUCH_DIM_JUMP_SIZE = 40.0f;
    private static final float MAX_MULTITOUCH_POS_JUMP_SIZE = 30.0f;
    public static final int MAX_TOUCH_POINTS = 20;
    private static final float MIN_MULTITOUCH_SEPARATION = 30.0f;
    private static final int MODE_DRAG = 1;
    private static final int MODE_NOTHING = 0;
    private static final int MODE_PINCH = 2;
    private static Method m_getHistoricalPressure;
    private static Method m_getHistoricalX;
    private static Method m_getHistoricalY;
    private static Method m_getPointerCount;
    private static Method m_getPointerId;
    private static Method m_getPressure;
    private static Method m_getX;
    private static Method m_getY;
    public static final boolean multiTouchSupported;
    private static final int[] pointerIds = new int[MAX_TOUCH_POINTS];
    private static final float[] pressureVals = new float[MAX_TOUCH_POINTS];
    private static final float[] xVals = new float[MAX_TOUCH_POINTS];
    private static final float[] yVals = new float[MAX_TOUCH_POINTS];
    private boolean handleSingleTouchEvents;
    private PointInfo mCurrPt;
    private float mCurrPtAng;
    private float mCurrPtDiam;
    private float mCurrPtHeight;
    private float mCurrPtWidth;
    private float mCurrPtX;
    private float mCurrPtY;
    private PositionAndScale mCurrXform;
    private int mMode;
    private PointInfo mPrevPt;
    private long mSettleEndTime;
    private long mSettleStartTime;
    MultiTouchObjectCanvas<T> objectCanvas;
    private T selectedObject;
    private float startAngleMinusPinchAngle;
    private float startPosX;
    private float startPosY;
    private float startScaleOverPinchDiam;
    private float startScaleXOverPinchWidth;
    private float startScaleYOverPinchHeight;

    public interface MultiTouchObjectCanvas<T> {
        T getDraggableObjectAtPoint(PointInfo pointInfo);

        void getPositionAndScale(T t, PositionAndScale positionAndScale);

        void selectObject(T t, PointInfo pointInfo);

        boolean setPositionAndScale(T t, PositionAndScale positionAndScale, PointInfo pointInfo);
    }

    public static class PointInfo {
        private int action;
        private float angle;
        private boolean angleIsCalculated;
        private float diameter;
        private boolean diameterIsCalculated;
        private float diameterSq;
        private boolean diameterSqIsCalculated;
        private float dx;
        private float dy;
        private long eventTime;
        private boolean isDown;
        private boolean isMultiTouch;
        private int numPoints;
        private int[] pointerIds = new int[MultiTouchController.MAX_TOUCH_POINTS];
        private float pressureMid;
        private float[] pressures = new float[MultiTouchController.MAX_TOUCH_POINTS];
        private float xMid;
        private float[] xs = new float[MultiTouchController.MAX_TOUCH_POINTS];
        private float yMid;
        private float[] ys = new float[MultiTouchController.MAX_TOUCH_POINTS];

        private void set(int numPoints, float[] x, float[] y, float[] pressure, int[] pointerIds, int action, boolean isDown, long eventTime) {
            this.eventTime = eventTime;
            this.action = action;
            this.numPoints = numPoints;
            for (int i = MultiTouchController.MODE_NOTHING; i < numPoints; i += MultiTouchController.MODE_DRAG) {
                this.xs[i] = x[i];
                this.ys[i] = y[i];
                this.pressures[i] = pressure[i];
                this.pointerIds[i] = pointerIds[i];
            }
            this.isDown = isDown;
            this.isMultiTouch = numPoints >= MultiTouchController.MODE_PINCH ? true : MultiTouchController.DEBUG;
            if (this.isMultiTouch) {
                this.xMid = (x[MultiTouchController.MODE_NOTHING] + x[MultiTouchController.MODE_DRAG]) * 0.5f;
                this.yMid = (y[MultiTouchController.MODE_NOTHING] + y[MultiTouchController.MODE_DRAG]) * 0.5f;
                this.pressureMid = (pressure[MultiTouchController.MODE_NOTHING] + pressure[MultiTouchController.MODE_DRAG]) * 0.5f;
                this.dx = Math.abs(x[MultiTouchController.MODE_DRAG] - x[MultiTouchController.MODE_NOTHING]);
                this.dy = Math.abs(y[MultiTouchController.MODE_DRAG] - y[MultiTouchController.MODE_NOTHING]);
            } else {
                this.xMid = x[MultiTouchController.MODE_NOTHING];
                this.yMid = y[MultiTouchController.MODE_NOTHING];
                this.pressureMid = pressure[MultiTouchController.MODE_NOTHING];
                this.dy = 0.0f;
                this.dx = 0.0f;
            }
            this.angleIsCalculated = MultiTouchController.DEBUG;
            this.diameterIsCalculated = MultiTouchController.DEBUG;
            this.diameterSqIsCalculated = MultiTouchController.DEBUG;
        }

        public void set(PointInfo other) {
            this.numPoints = other.numPoints;
            for (int i = MultiTouchController.MODE_NOTHING; i < this.numPoints; i += MultiTouchController.MODE_DRAG) {
                this.xs[i] = other.xs[i];
                this.ys[i] = other.ys[i];
                this.pressures[i] = other.pressures[i];
                this.pointerIds[i] = other.pointerIds[i];
            }
            this.xMid = other.xMid;
            this.yMid = other.yMid;
            this.pressureMid = other.pressureMid;
            this.dx = other.dx;
            this.dy = other.dy;
            this.diameter = other.diameter;
            this.diameterSq = other.diameterSq;
            this.angle = other.angle;
            this.isDown = other.isDown;
            this.action = other.action;
            this.isMultiTouch = other.isMultiTouch;
            this.diameterIsCalculated = other.diameterIsCalculated;
            this.diameterSqIsCalculated = other.diameterSqIsCalculated;
            this.angleIsCalculated = other.angleIsCalculated;
            this.eventTime = other.eventTime;
        }

        public boolean isMultiTouch() {
            return this.isMultiTouch;
        }

        public float getMultiTouchWidth() {
            return this.isMultiTouch ? this.dx : 0.0f;
        }

        public float getMultiTouchHeight() {
            return this.isMultiTouch ? this.dy : 0.0f;
        }

        private int julery_isqrt(int val) {
            int g = MultiTouchController.MODE_NOTHING;
            int b = 32768;
            int bshft = 15;
            while (true) {
                int bshft2 = bshft - 1;
                int temp = ((g << MultiTouchController.MODE_DRAG) + b) << bshft;
                if (val >= temp) {
                    g += b;
                    val -= temp;
                }
                b >>= MultiTouchController.MODE_DRAG;
                if (b <= 0) {
                    return g;
                }
                bshft = bshft2;
            }
        }

        public float getMultiTouchDiameterSq() {
            if (!this.diameterSqIsCalculated) {
                this.diameterSq = this.isMultiTouch ? (this.dx * this.dx) + (this.dy * this.dy) : 0.0f;
                this.diameterSqIsCalculated = true;
            }
            return this.diameterSq;
        }

        public float getMultiTouchDiameter() {
            float f = 0.0f;
            if (!this.diameterIsCalculated) {
                if (this.isMultiTouch) {
                    float diamSq = getMultiTouchDiameterSq();
                    if (diamSq != 0.0f) {
                        f = ((float) julery_isqrt((int) (256.0f * diamSq))) / 16.0f;
                    }
                    this.diameter = f;
                    if (this.diameter < this.dx) {
                        this.diameter = this.dx;
                    }
                    if (this.diameter < this.dy) {
                        this.diameter = this.dy;
                    }
                } else {
                    this.diameter = 0.0f;
                }
                this.diameterIsCalculated = true;
            }
            return this.diameter;
        }

        public float getMultiTouchAngle() {
            if (!this.angleIsCalculated) {
                if (this.isMultiTouch) {
                    this.angle = (float) Math.atan2((double) (this.ys[MultiTouchController.MODE_DRAG] - this.ys[MultiTouchController.MODE_NOTHING]), (double) (this.xs[MultiTouchController.MODE_DRAG] - this.xs[MultiTouchController.MODE_NOTHING]));
                } else {
                    this.angle = 0.0f;
                }
                this.angleIsCalculated = true;
            }
            return this.angle;
        }

        public int getNumTouchPoints() {
            return this.numPoints;
        }

        public float getX() {
            return this.xMid;
        }

        public float[] getXs() {
            return this.xs;
        }

        public float getY() {
            return this.yMid;
        }

        public float[] getYs() {
            return this.ys;
        }

        public int[] getPointerIds() {
            return this.pointerIds;
        }

        public float getPressure() {
            return this.pressureMid;
        }

        public float[] getPressures() {
            return this.pressures;
        }

        public boolean isDown() {
            return this.isDown;
        }

        public int getAction() {
            return this.action;
        }

        public long getEventTime() {
            return this.eventTime;
        }
    }

    public static class PositionAndScale {
        private float angle;
        private float scale;
        private float scaleX;
        private float scaleY;
        private boolean updateAngle;
        private boolean updateScale;
        private boolean updateScaleXY;
        private float xOff;
        private float yOff;

        public void set(float xOff, float yOff, boolean updateScale, float scale, boolean updateScaleXY, float scaleX, float scaleY, boolean updateAngle, float angle) {
            float f = SimpleItemTouchHelperCallback.ALPHA_FULL;
            this.xOff = xOff;
            this.yOff = yOff;
            this.updateScale = updateScale;
            if (scale == 0.0f) {
                scale = SimpleItemTouchHelperCallback.ALPHA_FULL;
            }
            this.scale = scale;
            this.updateScaleXY = updateScaleXY;
            if (scaleX == 0.0f) {
                scaleX = SimpleItemTouchHelperCallback.ALPHA_FULL;
            }
            this.scaleX = scaleX;
            if (scaleY != 0.0f) {
                f = scaleY;
            }
            this.scaleY = f;
            this.updateAngle = updateAngle;
            this.angle = angle;
        }

        protected void set(float xOff, float yOff, float scale, float scaleX, float scaleY, float angle) {
            float f = SimpleItemTouchHelperCallback.ALPHA_FULL;
            this.xOff = xOff;
            this.yOff = yOff;
            if (scale == 0.0f) {
                scale = SimpleItemTouchHelperCallback.ALPHA_FULL;
            }
            this.scale = scale;
            if (scaleX == 0.0f) {
                scaleX = SimpleItemTouchHelperCallback.ALPHA_FULL;
            }
            this.scaleX = scaleX;
            if (scaleY != 0.0f) {
                f = scaleY;
            }
            this.scaleY = f;
            this.angle = angle;
        }

        public float getXOff() {
            return this.xOff;
        }

        public float getYOff() {
            return this.yOff;
        }

        public float getScale() {
            return !this.updateScale ? SimpleItemTouchHelperCallback.ALPHA_FULL : this.scale;
        }

        public float getScaleX() {
            return !this.updateScaleXY ? SimpleItemTouchHelperCallback.ALPHA_FULL : this.scaleX;
        }

        public float getScaleY() {
            return !this.updateScaleXY ? SimpleItemTouchHelperCallback.ALPHA_FULL : this.scaleY;
        }

        public float getAngle() {
            return !this.updateAngle ? 0.0f : this.angle;
        }
    }

    private void extractCurrPtInfo() {
        float f = 0.0f;
        this.mCurrPtX = this.mCurrPt.getX();
        this.mCurrPtY = this.mCurrPt.getY();
        this.mCurrPtDiam = Math.max(21.3f, !this.mCurrXform.updateScale ? 0.0f : this.mCurrPt.getMultiTouchDiameter());
        this.mCurrPtWidth = Math.max(MIN_MULTITOUCH_SEPARATION, !this.mCurrXform.updateScaleXY ? 0.0f : this.mCurrPt.getMultiTouchWidth());
        this.mCurrPtHeight = Math.max(MIN_MULTITOUCH_SEPARATION, !this.mCurrXform.updateScaleXY ? 0.0f : this.mCurrPt.getMultiTouchHeight());
        if (this.mCurrXform.updateAngle) {
            f = this.mCurrPt.getMultiTouchAngle();
        }
        this.mCurrPtAng = f;
    }

    public MultiTouchController(MultiTouchObjectCanvas<T> objectCanvas) {
        this(objectCanvas, true);
    }

    public MultiTouchController(MultiTouchObjectCanvas<T> objectCanvas, boolean handleSingleTouchEvents) {
        this.selectedObject = null;
        this.mCurrXform = new PositionAndScale();
        this.mMode = MODE_NOTHING;
        this.mCurrPt = new PointInfo();
        this.mPrevPt = new PointInfo();
        this.handleSingleTouchEvents = handleSingleTouchEvents;
        this.objectCanvas = objectCanvas;
    }

    protected void setHandleSingleTouchEvents(boolean handleSingleTouchEvents) {
        this.handleSingleTouchEvents = handleSingleTouchEvents;
    }

    protected boolean getHandleSingleTouchEvents() {
        return this.handleSingleTouchEvents;
    }

    static {
        ACTION_POINTER_UP = 6;
        ACTION_POINTER_INDEX_SHIFT = 8;
        boolean succeeded = DEBUG;
        try {
            m_getPointerCount = MotionEvent.class.getMethod("getPointerCount", new Class[MODE_NOTHING]);
            Class[] clsArr = new Class[MODE_DRAG];
            clsArr[MODE_NOTHING] = Integer.TYPE;
            m_getPointerId = MotionEvent.class.getMethod("getPointerId", clsArr);
            clsArr = new Class[MODE_DRAG];
            clsArr[MODE_NOTHING] = Integer.TYPE;
            m_getPressure = MotionEvent.class.getMethod("getPressure", clsArr);
            clsArr = new Class[MODE_PINCH];
            clsArr[MODE_NOTHING] = Integer.TYPE;
            clsArr[MODE_DRAG] = Integer.TYPE;
            m_getHistoricalX = MotionEvent.class.getMethod("getHistoricalX", clsArr);
            clsArr = new Class[MODE_PINCH];
            clsArr[MODE_NOTHING] = Integer.TYPE;
            clsArr[MODE_DRAG] = Integer.TYPE;
            m_getHistoricalY = MotionEvent.class.getMethod("getHistoricalY", clsArr);
            clsArr = new Class[MODE_PINCH];
            clsArr[MODE_NOTHING] = Integer.TYPE;
            clsArr[MODE_DRAG] = Integer.TYPE;
            m_getHistoricalPressure = MotionEvent.class.getMethod("getHistoricalPressure", clsArr);
            clsArr = new Class[MODE_DRAG];
            clsArr[MODE_NOTHING] = Integer.TYPE;
            m_getX = MotionEvent.class.getMethod("getX", clsArr);
            clsArr = new Class[MODE_DRAG];
            clsArr[MODE_NOTHING] = Integer.TYPE;
            m_getY = MotionEvent.class.getMethod("getY", clsArr);
            succeeded = true;
        } catch (Exception e) {
            Log.e("MultiTouchController", "static initializer failed", e);
        }
        multiTouchSupported = succeeded;
        if (multiTouchSupported) {
            try {
                ACTION_POINTER_UP = MotionEvent.class.getField("ACTION_POINTER_UP").getInt(null);
                ACTION_POINTER_INDEX_SHIFT = MotionEvent.class.getField("ACTION_POINTER_INDEX_SHIFT").getInt(null);
            } catch (Exception e2) {
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        try {
            int pointerCount = multiTouchSupported ? ((Integer) m_getPointerCount.invoke(event, new Object[MODE_NOTHING])).intValue() : MODE_DRAG;
            if (this.mMode == 0 && !this.handleSingleTouchEvents && pointerCount == MODE_DRAG) {
                return DEBUG;
            }
            int action = event.getAction();
            int histLen = event.getHistorySize() / pointerCount;
            int histIdx = MODE_NOTHING;
            while (histIdx <= histLen) {
                float[] fArr;
                int i;
                long historicalEventTime;
                boolean processingHist = histIdx < histLen ? true : DEBUG;
                if (!multiTouchSupported || pointerCount == MODE_DRAG) {
                    float historicalPressure;
                    xVals[MODE_NOTHING] = processingHist ? event.getHistoricalX(histIdx) : event.getX();
                    yVals[MODE_NOTHING] = processingHist ? event.getHistoricalY(histIdx) : event.getY();
                    fArr = pressureVals;
                    if (processingHist) {
                        historicalPressure = event.getHistoricalPressure(histIdx);
                    } else {
                        historicalPressure = event.getPressure();
                    }
                    fArr[MODE_NOTHING] = historicalPressure;
                } else {
                    int numPointers = Math.min(pointerCount, MAX_TOUCH_POINTS);
                    for (int ptrIdx = MODE_NOTHING; ptrIdx < numPointers; ptrIdx += MODE_DRAG) {
                        Object[] objArr;
                        Object invoke;
                        Method method = m_getPointerId;
                        Object[] objArr2 = new Object[MODE_DRAG];
                        objArr2[MODE_NOTHING] = Integer.valueOf(ptrIdx);
                        pointerIds[ptrIdx] = ((Integer) method.invoke(event, objArr2)).intValue();
                        fArr = xVals;
                        if (processingHist) {
                            method = m_getHistoricalX;
                            objArr = new Object[MODE_PINCH];
                            objArr[MODE_NOTHING] = Integer.valueOf(ptrIdx);
                            objArr[MODE_DRAG] = Integer.valueOf(histIdx);
                            invoke = method.invoke(event, objArr);
                        } else {
                            method = m_getX;
                            objArr = new Object[MODE_DRAG];
                            objArr[MODE_NOTHING] = Integer.valueOf(ptrIdx);
                            invoke = method.invoke(event, objArr);
                        }
                        fArr[ptrIdx] = ((Float) invoke).floatValue();
                        fArr = yVals;
                        if (processingHist) {
                            method = m_getHistoricalY;
                            objArr = new Object[MODE_PINCH];
                            objArr[MODE_NOTHING] = Integer.valueOf(ptrIdx);
                            objArr[MODE_DRAG] = Integer.valueOf(histIdx);
                            invoke = method.invoke(event, objArr);
                        } else {
                            method = m_getY;
                            objArr = new Object[MODE_DRAG];
                            objArr[MODE_NOTHING] = Integer.valueOf(ptrIdx);
                            invoke = method.invoke(event, objArr);
                        }
                        fArr[ptrIdx] = ((Float) invoke).floatValue();
                        fArr = pressureVals;
                        if (processingHist) {
                            method = m_getHistoricalPressure;
                            objArr = new Object[MODE_PINCH];
                            objArr[MODE_NOTHING] = Integer.valueOf(ptrIdx);
                            objArr[MODE_DRAG] = Integer.valueOf(histIdx);
                            invoke = method.invoke(event, objArr);
                        } else {
                            method = m_getPressure;
                            objArr = new Object[MODE_DRAG];
                            objArr[MODE_NOTHING] = Integer.valueOf(ptrIdx);
                            invoke = method.invoke(event, objArr);
                        }
                        fArr[ptrIdx] = ((Float) invoke).floatValue();
                    }
                }
                fArr = xVals;
                float[] fArr2 = yVals;
                float[] fArr3 = pressureVals;
                int[] iArr = pointerIds;
                if (processingHist) {
                    i = MODE_PINCH;
                } else {
                    i = action;
                }
                boolean z = processingHist ? true : (action == MODE_DRAG || (((MODE_DRAG << ACTION_POINTER_INDEX_SHIFT) - 1) & action) == ACTION_POINTER_UP || action == 3) ? DEBUG : true;
                if (processingHist) {
                    historicalEventTime = event.getHistoricalEventTime(histIdx);
                } else {
                    historicalEventTime = event.getEventTime();
                }
                decodeTouchEvent(pointerCount, fArr, fArr2, fArr3, iArr, i, z, historicalEventTime);
                histIdx += MODE_DRAG;
            }
            return true;
        } catch (Exception e) {
            Log.e("MultiTouchController", "onTouchEvent() failed", e);
            return DEBUG;
        }
    }

    private void decodeTouchEvent(int pointerCount, float[] x, float[] y, float[] pressure, int[] pointerIds, int action, boolean down, long eventTime) {
        PointInfo tmp = this.mPrevPt;
        this.mPrevPt = this.mCurrPt;
        this.mCurrPt = tmp;
        this.mCurrPt.set(pointerCount, x, y, pressure, pointerIds, action, down, eventTime);
        multiTouchController();
    }

    private void anchorAtThisPositionAndScale() {
        if (this.selectedObject != null) {
            this.objectCanvas.getPositionAndScale(this.selectedObject, this.mCurrXform);
            float access$400 = !this.mCurrXform.updateScale ? SimpleItemTouchHelperCallback.ALPHA_FULL : this.mCurrXform.scale == 0.0f ? SimpleItemTouchHelperCallback.ALPHA_FULL : this.mCurrXform.scale;
            float currScaleInv = SimpleItemTouchHelperCallback.ALPHA_FULL / access$400;
            extractCurrPtInfo();
            this.startPosX = (this.mCurrPtX - this.mCurrXform.xOff) * currScaleInv;
            this.startPosY = (this.mCurrPtY - this.mCurrXform.yOff) * currScaleInv;
            this.startScaleOverPinchDiam = this.mCurrXform.scale / this.mCurrPtDiam;
            this.startScaleXOverPinchWidth = this.mCurrXform.scaleX / this.mCurrPtWidth;
            this.startScaleYOverPinchHeight = this.mCurrXform.scaleY / this.mCurrPtHeight;
            this.startAngleMinusPinchAngle = this.mCurrXform.angle - this.mCurrPtAng;
        }
    }

    private void performDragOrPinch() {
        float currScale = SimpleItemTouchHelperCallback.ALPHA_FULL;
        if (this.selectedObject != null) {
            if (this.mCurrXform.updateScale && this.mCurrXform.scale != 0.0f) {
                currScale = this.mCurrXform.scale;
            }
            extractCurrPtInfo();
            this.mCurrXform.set(this.mCurrPtX - (this.startPosX * currScale), this.mCurrPtY - (this.startPosY * currScale), this.startScaleOverPinchDiam * this.mCurrPtDiam, this.startScaleXOverPinchWidth * this.mCurrPtWidth, this.startScaleYOverPinchHeight * this.mCurrPtHeight, this.startAngleMinusPinchAngle + this.mCurrPtAng);
            if (!this.objectCanvas.setPositionAndScale(this.selectedObject, this.mCurrXform, this.mCurrPt)) {
            }
        }
    }

    private void multiTouchController() {
        MultiTouchObjectCanvas multiTouchObjectCanvas;
        switch (this.mMode) {
            case MODE_NOTHING /*0*/:
                if (this.mCurrPt.isDown()) {
                    this.selectedObject = this.objectCanvas.getDraggableObjectAtPoint(this.mCurrPt);
                    if (this.selectedObject != null) {
                        this.mMode = MODE_DRAG;
                        this.objectCanvas.selectObject(this.selectedObject, this.mCurrPt);
                        anchorAtThisPositionAndScale();
                        long eventTime = this.mCurrPt.getEventTime();
                        this.mSettleEndTime = eventTime;
                        this.mSettleStartTime = eventTime;
                        return;
                    }
                    return;
                }
                return;
            case MODE_DRAG /*1*/:
                if (!this.mCurrPt.isDown()) {
                    this.mMode = MODE_NOTHING;
                    multiTouchObjectCanvas = this.objectCanvas;
                    this.selectedObject = null;
                    multiTouchObjectCanvas.selectObject(null, this.mCurrPt);
                    return;
                } else if (this.mCurrPt.isMultiTouch()) {
                    this.mMode = MODE_PINCH;
                    anchorAtThisPositionAndScale();
                    this.mSettleStartTime = this.mCurrPt.getEventTime();
                    this.mSettleEndTime = this.mSettleStartTime + EVENT_SETTLE_TIME_INTERVAL;
                    return;
                } else if (this.mCurrPt.getEventTime() < this.mSettleEndTime) {
                    anchorAtThisPositionAndScale();
                    return;
                } else {
                    performDragOrPinch();
                    return;
                }
            case MODE_PINCH /*2*/:
                if (this.mCurrPt.isMultiTouch() && this.mCurrPt.isDown()) {
                    if (Math.abs(this.mCurrPt.getX() - this.mPrevPt.getX()) > MIN_MULTITOUCH_SEPARATION || Math.abs(this.mCurrPt.getY() - this.mPrevPt.getY()) > MIN_MULTITOUCH_SEPARATION || Math.abs(this.mCurrPt.getMultiTouchWidth() - this.mPrevPt.getMultiTouchWidth()) * 0.5f > MAX_MULTITOUCH_DIM_JUMP_SIZE || Math.abs(this.mCurrPt.getMultiTouchHeight() - this.mPrevPt.getMultiTouchHeight()) * 0.5f > MAX_MULTITOUCH_DIM_JUMP_SIZE) {
                        anchorAtThisPositionAndScale();
                        this.mSettleStartTime = this.mCurrPt.getEventTime();
                        this.mSettleEndTime = this.mSettleStartTime + EVENT_SETTLE_TIME_INTERVAL;
                        return;
                    } else if (this.mCurrPt.eventTime < this.mSettleEndTime) {
                        anchorAtThisPositionAndScale();
                        return;
                    } else {
                        performDragOrPinch();
                        return;
                    }
                } else if (this.mCurrPt.isDown()) {
                    this.mMode = MODE_DRAG;
                    anchorAtThisPositionAndScale();
                    this.mSettleStartTime = this.mCurrPt.getEventTime();
                    this.mSettleEndTime = this.mSettleStartTime + EVENT_SETTLE_TIME_INTERVAL;
                    return;
                } else {
                    this.mMode = MODE_NOTHING;
                    multiTouchObjectCanvas = this.objectCanvas;
                    this.selectedObject = null;
                    multiTouchObjectCanvas.selectObject(null, this.mCurrPt);
                    return;
                }
            default:
                return;
        }
    }

    public int getMode() {
        return this.mMode;
    }
}
