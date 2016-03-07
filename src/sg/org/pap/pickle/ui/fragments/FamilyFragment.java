package sg.org.pap.pickle.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ProgressBar;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.api.data.YyfResponse;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.ui.activities.VideoPlayerActivity;
import sg.org.pap.pickle.ui.activities.WebviewActivity;
import sg.org.pap.pickle.ui.base.BaseFragment;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class FamilyFragment extends BaseFragment {
    @Bind({2131624071})
    ProgressBar mLoading;
    @Bind({2131624141})
    WebView mVideoView;

    private class YayfTask extends AsyncTask<Void, Void, YyfResponse> {
        private YayfTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            FamilyFragment.this.mLoading.setVisibility(0);
        }

        protected YyfResponse doInBackground(Void... params) {
            return RestClient.getInstance().getYayf();
        }

        protected void onPostExecute(YyfResponse yyfResponse) {
            super.onPostExecute(yyfResponse);
            if (yyfResponse != null) {
                if (yyfResponse.getYyf().getMedia().getType().equals(Message.TYPE_HQ_CONTACT) && !PickleApp.getInstance().isYayf()) {
                    Intent intent = new Intent(FamilyFragment.this.getActivity(), VideoPlayerActivity.class);
                    intent.putExtra(WebviewActivity.CONTENT, yyfResponse.getYyf().getMedia().getVideo());
                    FamilyFragment.this.startActivity(intent);
                    PickleApp.getInstance().setYayf(true);
                }
                String mWebUrl = yyfResponse.getYyf().getWebviewUrl();
                FamilyFragment.this.mLoading.setVisibility(8);
                if (TextUtils.isEmpty(mWebUrl) || !Connection.connected(FamilyFragment.this.getActivity())) {
                    Connection.showSnackBar(FamilyFragment.this.getActivity(), FamilyFragment.this.mVideoView);
                    return;
                }
                FamilyFragment.this.mVideoView.setPadding(0, 0, 0, 0);
                FamilyFragment.this.mVideoView.setInitialScale(FamilyFragment.this.getScale());
                FamilyFragment.this.mVideoView.getSettings().setJavaScriptEnabled(true);
                FamilyFragment.this.mVideoView.loadUrl(mWebUrl);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendScreenAnalytics("/yayf");
        return inflater.inflate(R.layout.fragment_family, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        new YayfTask().execute(new Void[0]);
    }

    public void onPause() {
        super.onPause();
        if (VERSION.SDK_INT >= 11) {
            this.mVideoView.onPause();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mVideoView.destroy();
    }

    private int getScale() {
        return Double.valueOf(Double.valueOf(new Double((double) ((WindowManager) getActivity().getSystemService("window")).getDefaultDisplay().getWidth()).doubleValue() / new Double((double) (this.mVideoView.getRight() - this.mVideoView.getLeft())).doubleValue()).doubleValue() * 100.0d).intValue();
    }
}
