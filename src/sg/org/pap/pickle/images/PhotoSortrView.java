package sg.org.pap.pickle.images;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.images.MultiTouchController.MultiTouchObjectCanvas;
import sg.org.pap.pickle.images.MultiTouchController.PointInfo;
import sg.org.pap.pickle.images.MultiTouchController.PositionAndScale;

public class PhotoSortrView extends View implements MultiTouchObjectCanvas<Img> {
    private static final int[] IMAGES = new int[0];
    private static final int UI_MODE_ANISOTROPIC_SCALE = 2;
    private static final int UI_MODE_ROTATE = 1;
    private PointInfo currTouchPoint;
    private Bitmap mBackgroundBitmap;
    private Context mContext;
    private ArrayList<Img> mImages;
    private Paint mLinePaintTouchPointCircle;
    private boolean mShowDebugInfo;
    private int mUIMode;
    private MultiTouchController<Img> multiTouchController;

    class Img {
        private static final float SCREEN_MARGIN = 100.0f;
        private float angle;
        private float centerX;
        private float centerY;
        private int displayHeight;
        private int displayWidth;
        private Drawable drawable;
        private boolean firstLoad = true;
        private int height;
        private float maxX;
        private float maxY;
        private float minX;
        private float minY;
        private int resId;
        private float scaleX;
        private float scaleY;
        private int width;

        public Img(int resId, Resources res) {
            this.resId = resId;
            getMetrics(res);
        }

        private void getMetrics(Resources res) {
            DisplayMetrics metrics = res.getDisplayMetrics();
            this.displayWidth = res.getConfiguration().orientation == PhotoSortrView.UI_MODE_ANISOTROPIC_SCALE ? Math.max(metrics.widthPixels, metrics.heightPixels) : Math.min(metrics.widthPixels, metrics.heightPixels);
            this.displayHeight = res.getConfiguration().orientation == PhotoSortrView.UI_MODE_ANISOTROPIC_SCALE ? Math.min(metrics.widthPixels, metrics.heightPixels) : Math.max(metrics.widthPixels, metrics.heightPixels);
        }

        public void load(Resources res) {
            float cx;
            float cy;
            float sy;
            float sx;
            getMetrics(res);
            this.drawable = res.getDrawable(this.resId);
            this.width = this.drawable.getIntrinsicWidth();
            this.height = this.drawable.getIntrinsicHeight();
            if (this.firstLoad) {
                cx = SCREEN_MARGIN + ((float) (Math.random() * ((double) (((float) this.displayWidth) - 200.0f))));
                cy = SCREEN_MARGIN + ((float) (Math.random() * ((double) (((float) this.displayHeight) - 200.0f))));
                float sc = (float) (((((double) (((float) Math.max(this.displayWidth, this.displayHeight)) / ((float) Math.max(this.width, this.height)))) * Math.random()) * 0.3d) + 0.2d);
                sy = sc;
                sx = sc;
                this.firstLoad = false;
            } else {
                cx = this.centerX;
                cy = this.centerY;
                sx = this.scaleX;
                sy = this.scaleY;
                if (this.maxX < SCREEN_MARGIN) {
                    cx = SCREEN_MARGIN;
                } else if (this.minX > ((float) this.displayWidth) - SCREEN_MARGIN) {
                    cx = ((float) this.displayWidth) - SCREEN_MARGIN;
                }
                if (this.maxY > SCREEN_MARGIN) {
                    cy = SCREEN_MARGIN;
                } else if (this.minY > ((float) this.displayHeight) - SCREEN_MARGIN) {
                    cy = ((float) this.displayHeight) - SCREEN_MARGIN;
                }
            }
            setPos(cx, cy, sx, sy, 0.0f);
        }

        public void unload() {
            this.drawable = null;
        }

        public boolean setPos(PositionAndScale newImgPosAndScale) {
            return setPos(newImgPosAndScale.getXOff(), newImgPosAndScale.getYOff(), (PhotoSortrView.this.mUIMode & PhotoSortrView.UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale.getScaleX() : newImgPosAndScale.getScale(), (PhotoSortrView.this.mUIMode & PhotoSortrView.UI_MODE_ANISOTROPIC_SCALE) != 0 ? newImgPosAndScale.getScaleY() : newImgPosAndScale.getScale(), newImgPosAndScale.getAngle());
        }

        private boolean setPos(float centerX, float centerY, float scaleX, float scaleY, float angle) {
            float ws = ((float) (this.width / PhotoSortrView.UI_MODE_ANISOTROPIC_SCALE)) * scaleX;
            float hs = ((float) (this.height / PhotoSortrView.UI_MODE_ANISOTROPIC_SCALE)) * scaleY;
            float newMinX = centerX - ws;
            float newMinY = centerY - hs;
            float newMaxX = centerX + ws;
            float newMaxY = centerY + hs;
            if (newMinX > ((float) this.displayWidth) - SCREEN_MARGIN || newMaxX < SCREEN_MARGIN || newMinY > ((float) this.displayHeight) - SCREEN_MARGIN || newMaxY < SCREEN_MARGIN) {
                return false;
            }
            this.centerX = centerX;
            this.centerY = centerY;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.angle = angle;
            this.minX = newMinX;
            this.minY = newMinY;
            this.maxX = newMaxX;
            this.maxY = newMaxY;
            return true;
        }

        public boolean containsPoint(float scrnX, float scrnY) {
            return scrnX >= this.minX && scrnX <= this.maxX && scrnY >= this.minY && scrnY <= this.maxY;
        }

        public void draw(Canvas canvas) {
            canvas.save();
            float dx = (this.maxX + this.minX) / 2.0f;
            float dy = (this.maxY + this.minY) / 2.0f;
            this.drawable.setBounds((int) this.minX, (int) this.minY, (int) this.maxX, (int) this.maxY);
            canvas.translate(dx, dy);
            canvas.rotate((this.angle * 180.0f) / 3.1415927f);
            canvas.translate(-dx, -dy);
            this.drawable.draw(canvas);
            canvas.restore();
        }

        public Drawable getDrawable() {
            return this.drawable;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        public float getCenterX() {
            return this.centerX;
        }

        public float getCenterY() {
            return this.centerY;
        }

        public float getScaleX() {
            return this.scaleX;
        }

        public float getScaleY() {
            return this.scaleY;
        }

        public float getAngle() {
            return this.angle;
        }

        public float getMinX() {
            return this.minX;
        }

        public float getMaxX() {
            return this.maxX;
        }

        public float getMinY() {
            return this.minY;
        }

        public float getMaxY() {
            return this.maxY;
        }
    }

    public PhotoSortrView(Context context) {
        this(context, null);
    }

    public PhotoSortrView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoSortrView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mImages = new ArrayList();
        this.multiTouchController = new MultiTouchController(this);
        this.currTouchPoint = new PointInfo();
        this.mShowDebugInfo = true;
        this.mUIMode = UI_MODE_ROTATE;
        this.mLinePaintTouchPointCircle = new Paint();
        this.mContext = context;
        init(context);
    }

    public void addImage(int drawable) {
        Resources res = this.mContext.getResources();
        this.mImages.add(new Img(drawable, res));
        ((Img) this.mImages.get(this.mImages.size() - 1)).load(res);
        invalidate();
    }

    public void changePhoto(Bitmap bm) {
        if (VERSION.SDK_INT >= 16) {
            setBackground(new BitmapDrawable(this.mContext.getResources(), bm));
        } else {
            setBackgroundDrawable(new BitmapDrawable(bm));
        }
    }

    private void init(Context context) {
        Resources res = context.getResources();
        for (int i = 0; i < IMAGES.length; i += UI_MODE_ROTATE) {
            this.mImages.add(new Img(IMAGES[i], res));
        }
        this.mLinePaintTouchPointCircle.setColor(0);
        this.mLinePaintTouchPointCircle.setStrokeWidth(0.0f);
        this.mLinePaintTouchPointCircle.setStyle(Style.STROKE);
        this.mLinePaintTouchPointCircle.setAntiAlias(true);
        setBackgroundResource(R.drawable.onboarding_am_bg);
    }

    private void init(Context context, Bitmap bitmap) {
        Resources res = context.getResources();
        for (int i = 0; i < IMAGES.length; i += UI_MODE_ROTATE) {
            this.mImages.add(new Img(IMAGES[i], res));
        }
        this.mLinePaintTouchPointCircle.setColor(0);
        this.mLinePaintTouchPointCircle.setStrokeWidth(0.0f);
        this.mLinePaintTouchPointCircle.setStyle(Style.STROKE);
        this.mLinePaintTouchPointCircle.setAntiAlias(true);
    }

    public void loadImages(Context context) {
        Resources res = context.getResources();
        int n = this.mImages.size();
        for (int i = 0; i < n; i += UI_MODE_ROTATE) {
            ((Img) this.mImages.get(i)).load(res);
        }
    }

    public void unloadImages() {
        int n = this.mImages.size();
        for (int i = 0; i < n; i += UI_MODE_ROTATE) {
            ((Img) this.mImages.get(i)).unload();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = this.mImages.size();
        for (int i = 0; i < n; i += UI_MODE_ROTATE) {
            ((Img) this.mImages.get(i)).draw(canvas);
        }
        if (this.mShowDebugInfo) {
            drawMultitouchDebugMarks(canvas);
        }
    }

    public void trackballClicked() {
        this.mUIMode = (this.mUIMode + UI_MODE_ROTATE) % 3;
        invalidate();
    }

    private void drawMultitouchDebugMarks(Canvas canvas) {
        if (this.currTouchPoint.isDown()) {
            float[] xs = this.currTouchPoint.getXs();
            float[] ys = this.currTouchPoint.getYs();
            float[] pressures = this.currTouchPoint.getPressures();
            int numPoints = Math.min(this.currTouchPoint.getNumTouchPoints(), UI_MODE_ANISOTROPIC_SCALE);
            for (int i = 0; i < numPoints; i += UI_MODE_ROTATE) {
                canvas.drawCircle(xs[i], ys[i], 50.0f + (pressures[i] * 80.0f), this.mLinePaintTouchPointCircle);
            }
            if (numPoints == UI_MODE_ANISOTROPIC_SCALE) {
                canvas.drawLine(xs[0], ys[0], xs[UI_MODE_ROTATE], ys[UI_MODE_ROTATE], this.mLinePaintTouchPointCircle);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.multiTouchController.onTouchEvent(event);
    }

    public Img getDraggableObjectAtPoint(PointInfo pt) {
        float x = pt.getX();
        float y = pt.getY();
        for (int i = this.mImages.size() - 1; i >= 0; i--) {
            Img im = (Img) this.mImages.get(i);
            if (im.containsPoint(x, y)) {
                return im;
            }
        }
        return null;
    }

    public void selectObject(Img img, PointInfo touchPoint) {
        this.currTouchPoint.set(touchPoint);
        if (img != null) {
            this.mImages.remove(img);
            this.mImages.add(img);
        }
        invalidate();
    }

    public void getPositionAndScale(Img img, PositionAndScale objPosAndScaleOut) {
        boolean z;
        boolean z2 = false;
        float centerX = img.getCenterX();
        float centerY = img.getCenterY();
        if ((this.mUIMode & UI_MODE_ANISOTROPIC_SCALE) == 0) {
            z = true;
        } else {
            z = false;
        }
        float scaleX = (img.getScaleX() + img.getScaleY()) / 2.0f;
        boolean z3 = (this.mUIMode & UI_MODE_ANISOTROPIC_SCALE) != 0;
        float scaleX2 = img.getScaleX();
        float scaleY = img.getScaleY();
        if ((this.mUIMode & UI_MODE_ROTATE) != 0) {
            z2 = true;
        }
        objPosAndScaleOut.set(centerX, centerY, z, scaleX, z3, scaleX2, scaleY, z2, img.getAngle());
    }

    public boolean setPositionAndScale(Img img, PositionAndScale newImgPosAndScale, PointInfo touchPoint) {
        this.currTouchPoint.set(touchPoint);
        boolean ok = img.setPos(newImgPosAndScale);
        if (ok) {
            invalidate();
        }
        return ok;
    }
}
