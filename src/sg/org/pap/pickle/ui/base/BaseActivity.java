package sg.org.pap.pickle.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class BaseActivity extends AppCompatActivity {
    private Tracker mTracker;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTracker = PickleApp.getInstance().getDefaultTracker();
    }

    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(R.animator.anim_fade_from_left, R.animator.anim_fade_to_right);
        }
    }

    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.animator.anim_fade_from_right, R.animator.anim_fade_to_left);
    }

    public void sendScreenAnalytics(String name) {
        this.mTracker.setScreenName(name);
        this.mTracker.send(new ScreenViewBuilder().build());
    }
}
