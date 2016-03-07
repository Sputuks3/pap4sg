package sg.org.pap.pickle.ui.pickle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class RelativeWrapper extends RelativeLayout {
    private int mOffset;

    public RelativeWrapper(Context context) {
        super(context);
    }

    public RelativeWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public RelativeWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void dispatchDraw(Canvas canvas) {
        canvas.clipRect(new Rect(getLeft(), getTop(), getRight(), getBottom() + this.mOffset));
        super.dispatchDraw(canvas);
    }

    public void setClipY(int offset) {
        this.mOffset = offset;
        invalidate();
    }
}
