package sg.org.pap.pickle.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.facebook.internal.AnalyticsEvents;
import it.moondroid.coverflow.BuildConfig;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.News;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class NewsDetailActivity extends BaseActivity {
    public static final String ARG_CONTENT = "content";
    private boolean isDestroyed = false;
    @Bind({2131624122})
    TextView mContent;
    @Bind({2131624126})
    Button mFeatured;
    @Bind({2131624075})
    ImageView mImage;
    private News mNews;
    @Bind({2131624076})
    ImageView mPlayer;
    @Bind({2131624121})
    TextView mTimestamp;
    @Bind({2131624119})
    TextView mTitle;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    @Bind({2131624123})
    WebView mWeb;

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
                NewsDetailActivity.this.mNews = newsResponse;
                NewsDetailActivity.this.bindNews(NewsDetailActivity.this.mNews);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        if (getIntent().getSerializableExtra(ARG_CONTENT) != null) {
            this.mNews = (News) getIntent().getSerializableExtra(ARG_CONTENT);
            bindNews(this.mNews);
            new NewsTask().execute(new String[]{this.mNews.getId()});
        }
        restoreActionBar();
    }

    protected void onResume() {
        super.onResume();
        sendScreenAnalytics("/news/detail");
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

    private void bindNews(final News news) {
        this.mTitle.setText(news.getTitle());
        this.mTimestamp.setText(PickleApp.getFormalDate(news.getDate()));
        this.mWeb.getSettings().setJavaScriptEnabled(true);
        this.mWeb.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(NewsDetailActivity.this, WebplayerActivity.class);
                intent.putExtra(WebviewActivity.CONTENT, url);
                NewsDetailActivity.this.startActivity(intent);
                return true;
            }
        });
        this.mWeb.loadDataWithBaseURL(BuildConfig.FLAVOR, news.getContent(), "text/html", "UTF-8", BuildConfig.FLAVOR);
        if (!news.getTypeName().equals(AnalyticsEvents.PARAMETER_SHARE_DIALOG_CONTENT_VIDEO)) {
            this.mPlayer.setVisibility(8);
            if (!(TextUtils.isEmpty(news.getPhoto()) || isDestroyed())) {
                Glide.with(this).load(news.getPhoto()).crossFade().into(this.mImage);
            }
        } else if (!TextUtils.isEmpty(news.getVideo())) {
            this.mPlayer.setVisibility(0);
            Glide.with(this).load(news.getVideoPhoto()).crossFade().into(this.mImage);
            this.mPlayer.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(NewsDetailActivity.this, WebplayerActivity.class);
                    intent.putExtra(WebviewActivity.CONTENT, news.getVideo());
                    NewsDetailActivity.this.startActivity(intent);
                }
            });
        }
        if (news.isFeatured()) {
            this.mFeatured.setVisibility(0);
            this.mFeatured.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    NewsDetailActivity.this.startActivity(new Intent(NewsDetailActivity.this, CandidateFlowActivity.class));
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

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        this.mTitleView.setText("News");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_candidate_detail, menu);
        return true;
    }
}
