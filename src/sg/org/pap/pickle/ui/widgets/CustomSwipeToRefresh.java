package sg.org.pap.pickle.ui.widgets;

import android.content.Context;
import android.os.Build.VERSION;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import java.lang.reflect.Field;

public class CustomSwipeToRefresh extends SwipeRefreshLayout implements OnGlobalLayoutListener {
    private static int DEFAULT_REFRESH_TRIGGER_DISTANCE = 20;
    private static float MAX_SWIPE_DISTANCE_FACTOR = 0.1f;
    private int refreshTriggerDistance = DEFAULT_REFRESH_TRIGGER_DISTANCE;
    ViewTreeObserver vto = getViewTreeObserver();

    public CustomSwipeToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.vto.addOnGlobalLayoutListener(this);
    }

    public void onGlobalLayout() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Float mDistanceToTriggerSync = Float.valueOf(1.0E-7f);
        try {
            Field field = SwipeRefreshLayout.class.getDeclaredField("mDistanceToTriggerSync");
            field.setAccessible(true);
            field.setFloat(this, mDistanceToTriggerSync.floatValue());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
        ViewTreeObserver obs = getViewTreeObserver();
        if (VERSION.SDK_INT >= 16) {
            obs.removeOnGlobalLayoutListener(this);
        } else {
            obs.removeGlobalOnLayoutListener(this);
        }
    }

    private int getRefreshTriggerDistance() {
        return this.refreshTriggerDistance;
    }

    private void setRefreshTriggerDistance(int refreshTriggerDistance) {
        this.refreshTriggerDistance = refreshTriggerDistance;
    }
}
