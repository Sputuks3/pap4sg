package sg.org.pap.pickle.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.facebook.internal.AnalyticsEvents;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;
import it.moondroid.coverflow.BuildConfig;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.News;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class YoutubeNewsDetailActivity extends YouTubeBaseActivity implements OnInitializedListener {
    public static final String ARG_CONTENT = "content";
    public String VIDEO_ID;
    private boolean isDestroyed = false;
    @Bind({2131624224})
    ImageButton mBtnBack;
    @Bind({2131624126})
    Button mFeatured;
    private News mNews;
    @Bind({2131624074})
    YouTubePlayerView mPlayer;
    @Bind({2131624121})
    TextView mTimestamp;
    @Bind({2131624119})
    TextView mTitle;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    private Tracker mTracker;
    @Bind({2131624123})
    WebView mWeb;
    private PlaybackEventListener playbackEventListener = new PlaybackEventListener() {
        public void onBuffering(boolean arg0) {
        }

        public void onPaused() {
        }

        public void onPlaying() {
        }

        public void onSeekTo(int arg0) {
        }

        public void onStopped() {
        }
    };
    private PlayerStateChangeListener playerStateChangeListener = new PlayerStateChangeListener() {
        public void onAdStarted() {
        }

        public void onError(ErrorReason arg0) {
        }

        public void onLoaded(String arg0) {
        }

        public void onLoading() {
        }

        public void onVideoEnded() {
        }

        public void onVideoStarted() {
        }
    };

    private class NewsTask extends AsyncTask<String, Void, News> {
        private NewsTask() {
        }

        protected News doInBackground(String... strings) {
            try {
                String id = strings[0];
                if (TextUtils.isEmpty(id)) {
                    return RestClient.getInstance().getNewsDetail("9");
                }
                return RestClient.getInstance().getNewsDetail(id);
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(News newsResponse) {
            super.onPostExecute(newsResponse);
            if (newsResponse != null) {
                YoutubeNewsDetailActivity.this.mNews = newsResponse;
                YoutubeNewsDetailActivity.this.bindNews(YoutubeNewsDetailActivity.this.mNews);
            }
        }
    }

    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.animator.anim_fade_from_right, R.animator.anim_fade_to_left);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_news_detail);
        ButterKnife.bind(this);
        if (getIntent().getSerializableExtra(ARG_CONTENT) != null) {
            this.mNews = (News) getIntent().getSerializableExtra(ARG_CONTENT);
            bindNews(this.mNews);
            new NewsTask().execute(new String[]{this.mNews.getId()});
        }
        this.mTracker = PickleApp.getInstance().getDefaultTracker();
        restoreActionBar();
        this.mBtnBack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                YoutubeNewsDetailActivity.this.onBackPressed();
            }
        });
        if (TextUtils.isEmpty(this.mNews.getVideo())) {
            this.VIDEO_ID = BuildConfig.FLAVOR;
            return;
        }
        this.VIDEO_ID = this.mNews.getVideo().substring(this.mNews.getVideo().lastIndexOf("=") + 1);
        this.mPlayer.initialize("AIzaSyAi1ihpj1wR89E4y2M1VW0cX_ly8-tjso4", this);
    }

    protected void onResume() {
        super.onResume();
        this.mTracker.setScreenName("/news/detail");
        this.mTracker.send(new ScreenViewBuilder().build());
    }

    protected void onDestroy() {
        super.onDestroy();
        setIsDestroyed(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction("android.intent.action.SEND");
            sendIntent.putExtra("android.intent.extra.TEXT", this.mNews.getUrl());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindNews(News news) {
        this.mTitle.setText(news.getTitle());
        this.mTimestamp.setText(PickleApp.getFormalDate(news.getDate()));
        this.mWeb.getSettings().setJavaScriptEnabled(true);
        this.mWeb.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(YoutubeNewsDetailActivity.this, WebplayerActivity.class);
                intent.putExtra(WebviewActivity.CONTENT, url);
                YoutubeNewsDetailActivity.this.startActivity(intent);
                return true;
            }
        });
        this.mWeb.loadDataWithBaseURL(BuildConfig.FLAVOR, news.getContent(), "text/html", "UTF-8", BuildConfig.FLAVOR);
        if (!(!news.getTypeName().equals(AnalyticsEvents.PARAMETER_SHARE_DIALOG_CONTENT_VIDEO) || TextUtils.isEmpty(news.getVideo()) || TextUtils.isEmpty(this.mNews.getVideo()))) {
            this.VIDEO_ID = this.mNews.getVideo().substring(this.mNews.getVideo().lastIndexOf("=") + 1);
        }
        if (news.isFeatured()) {
            this.mFeatured.setVisibility(0);
            this.mFeatured.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    YoutubeNewsDetailActivity.this.startActivity(new Intent(YoutubeNewsDetailActivity.this, CandidateFlowActivity.class));
                }
            });
        }
    }

    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    public void setIsDestroyed(boolean isDestroyed) {
        this.isDestroyed = isDestroyed;
    }

    public void onInitializationSuccess(Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.setPlayerStateChangeListener(this.playerStateChangeListener);
        youTubePlayer.setPlaybackEventListener(this.playbackEventListener);
        if (!b) {
            youTubePlayer.cueVideo(this.VIDEO_ID);
        }
    }

    public void onInitializationFailure(Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, "Failured to Initialize!", 1).show();
    }

    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(R.animator.anim_fade_from_left, R.animator.anim_fade_to_right);
        }
    }

    public void restoreActionBar() {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_candidate_detail, menu);
        return true;
    }

    public static String extractYTId(String ytUrl) {
        Matcher matcher = Pattern.compile("^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$", 2).matcher(ytUrl);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}
