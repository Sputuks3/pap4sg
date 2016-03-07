package sg.org.pap.pickle.ui.activities;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import it.moondroid.coverflow.BuildConfig;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.base.Connection;

public class WebviewActivity extends BaseActivity {
    public static final String CONTENT = "ARGS_CONTENT";
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    @Bind({2131624141})
    WebView mVideoView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        restoreActionBar();
        String mVideoUrl = BuildConfig.FLAVOR;
        if (savedInstanceState == null) {
            mVideoUrl = getIntent().getDataString().replace("myscheme://", "http://");
        }
        if (TextUtils.isEmpty(mVideoUrl) || !Connection.connected(this)) {
            Connection.showSnackBar(this, this.mVideoView);
            return;
        }
        this.mVideoView.getSettings().setJavaScriptEnabled(true);
        this.mVideoView.setWebViewClient(new WebViewClient());
        if (mVideoUrl.endsWith(".pdf")) {
            this.mVideoView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + mVideoUrl);
        } else {
            this.mVideoView.loadUrl(mVideoUrl);
        }
    }

    protected void onPause() {
        super.onPause();
        if (VERSION.SDK_INT >= 11) {
            this.mVideoView.onPause();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mVideoView.destroy();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.mTitleView.setText(BuildConfig.FLAVOR);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_share) {
        }
        return super.onOptionsItemSelected(item);
    }
}
