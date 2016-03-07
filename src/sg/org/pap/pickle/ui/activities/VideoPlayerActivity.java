package sg.org.pap.pickle.ui.activities;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;
import butterknife.Bind;
import butterknife.ButterKnife;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.base.Connection;

public class VideoPlayerActivity extends BaseActivity {
    public static final String CONTENT = "ARGS_CONTENT";
    @Bind({2131624142})
    ImageButton mClose;
    @Bind({2131624124})
    ProgressBar mLoading;
    @Bind({2131624141})
    VideoView mVideoView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(1);
        setContentView(R.layout.activity_video);
        getWindow().setLayout(-1, -2);
        setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
        String mVideoUrl = getIntent().getStringExtra(CONTENT);
        if (TextUtils.isEmpty(mVideoUrl) || !Connection.connected(this)) {
            Connection.showSnackBar(this, this.mVideoView);
        } else {
            this.mVideoView.setZOrderOnTop(true);
            this.mVideoView.setVideoURI(Uri.parse(mVideoUrl));
            MediaController mController = new MediaController(this);
            mController.setAnchorView(this.mVideoView);
            this.mVideoView.setMediaController(mController);
            this.mVideoView.start();
            this.mVideoView.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    VideoPlayerActivity.this.mLoading.setVisibility(8);
                }
            });
            this.mVideoView.setOnCompletionListener(new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    VideoPlayerActivity.this.finish();
                }
            });
            this.mVideoView.setOnErrorListener(new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(VideoPlayerActivity.this, "Something went wrong", 0).show();
                    return true;
                }
            });
        }
        this.mClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                VideoPlayerActivity.this.onBackPressed();
            }
        });
    }

    protected void onPause() {
        super.onPause();
        if (VERSION.SDK_INT >= 11) {
            this.mVideoView.pause();
        }
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

    protected void onDestroy() {
        super.onDestroy();
        this.mVideoView.stopPlayback();
    }
}
