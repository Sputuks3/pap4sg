package sg.org.pap.pickle.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class BaseSupportFragment extends Fragment {
    public static final String ITEM = "item";
    public static final String MODE = "mode";
    private Tracker mTracker;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTracker = PickleApp.getInstance().getDefaultTracker();
    }

    public void startAnimatedActivity(Intent intent) {
        super.startActivity(intent);
        getActivity().overridePendingTransition(R.animator.anim_fade_from_right, R.animator.anim_fade_to_left);
    }

    public void startAnimatedActivity(Intent intent, int enterAnim, int exitAnim) {
        super.startActivity(intent);
        getActivity().overridePendingTransition(enterAnim, exitAnim);
    }

    public void sendScreenAnalytics(String name) {
        this.mTracker.setScreenName(name);
        this.mTracker.send(new ScreenViewBuilder().build());
    }
}
