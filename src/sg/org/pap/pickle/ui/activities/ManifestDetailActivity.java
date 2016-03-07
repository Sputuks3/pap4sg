package sg.org.pap.pickle.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.facebook.internal.AnalyticsEvents;
import it.moondroid.coverflow.BuildConfig;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.api.data.Media;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.Pillar;
import sg.org.pap.pickle.ui.base.BaseActivity;

public class ManifestDetailActivity extends BaseActivity {
    public static final String ARGS_CONTENT = "content";
    public static final String CONTENT_MEDIA = "media";
    @Bind({2131624122})
    TextView mContent;
    @Bind({2131624114})
    LinearLayout mForewordLayout;
    @Bind({2131624115})
    TextView mForewordTitle;
    @Bind({2131624075})
    ImageView mImage;
    private Media mMedia;
    private Pillar mPillar;
    @Bind({2131624076})
    ImageView mPlay;
    @Bind({2131624116})
    Button mReplay;
    @Bind({2131624121})
    TextView mSubtitle;
    @Bind({2131624119})
    TextView mTitle;
    @Bind({2131624118})
    LinearLayout mTitleLayout;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    @Bind({2131624123})
    WebView mWeb;

    private class PillarTask extends AsyncTask<String, Void, Pillar> {
        private PillarTask() {
        }

        protected Pillar doInBackground(String... strings) {
            try {
                return RestClient.getInstance().getPillar(strings[0]);
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(Pillar pillar) {
            super.onPostExecute(pillar);
            if (pillar != null) {
                ManifestDetailActivity.this.mPillar = pillar;
                if (ManifestDetailActivity.this.mPillar.getContentType().equals(Message.TYPE_TERMS_OF_USE)) {
                    ManifestDetailActivity.this.mTitleView.setText("Foreword");
                    ManifestDetailActivity.this.mTitleLayout.setVisibility(8);
                    ManifestDetailActivity.this.mForewordLayout.setVisibility(0);
                    ManifestDetailActivity.this.mForewordTitle.setText(ManifestDetailActivity.this.mPillar.getTitle());
                    ManifestDetailActivity.this.mReplay.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(ManifestDetailActivity.this, VideoPlayerActivity.class);
                            intent.putExtra(WebviewActivity.CONTENT, ManifestDetailActivity.this.mMedia.getVideo());
                            ManifestDetailActivity.this.startActivity(intent);
                        }
                    });
                    ManifestDetailActivity.this.sendScreenAnalytics("/aboutUs/foreword");
                } else {
                    ManifestDetailActivity.this.mTitleView.setText(ManifestDetailActivity.this.getString(R.string.manifesto));
                    ManifestDetailActivity.this.mForewordLayout.setVisibility(8);
                    ManifestDetailActivity.this.mTitleLayout.setVisibility(0);
                    ManifestDetailActivity.this.mTitle.setText(ManifestDetailActivity.this.mPillar.getTitle());
                    ManifestDetailActivity.this.mSubtitle.setText(ManifestDetailActivity.this.mPillar.getCaption());
                    ManifestDetailActivity.this.sendScreenAnalytics("/aboutUs/detail");
                }
                ManifestDetailActivity.this.mContent.setText(Html.fromHtml(ManifestDetailActivity.this.mPillar.getContent().replace("<a href=\"http:", "<a href=\"myscheme:")));
                ManifestDetailActivity.this.mContent.setMovementMethod(LinkMovementMethod.getInstance());
                ManifestDetailActivity.this.mWeb.getSettings().setJavaScriptEnabled(true);
                ManifestDetailActivity.this.mWeb.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent intent = new Intent(ManifestDetailActivity.this, WebplayerActivity.class);
                        intent.putExtra(WebviewActivity.CONTENT, url);
                        ManifestDetailActivity.this.startActivity(intent);
                        return true;
                    }
                });
                ManifestDetailActivity.this.mWeb.loadDataWithBaseURL(BuildConfig.FLAVOR, ManifestDetailActivity.this.mPillar.getContent(), "text/html", "UTF-8", BuildConfig.FLAVOR);
                Glide.with(ManifestDetailActivity.this).load(ManifestDetailActivity.this.mPillar.getPhoto()).crossFade().into(ManifestDetailActivity.this.mImage);
                if (ManifestDetailActivity.this.mPillar.getTypeText().equals(AnalyticsEvents.PARAMETER_SHARE_DIALOG_CONTENT_VIDEO)) {
                    ManifestDetailActivity.this.mPlay.setVisibility(0);
                    ManifestDetailActivity.this.mPlay.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Intent intent = new Intent(ManifestDetailActivity.this, WebplayerActivity.class);
                            intent.putExtra(WebviewActivity.CONTENT, ManifestDetailActivity.this.mPillar.getVideo());
                            ManifestDetailActivity.this.startActivity(intent);
                        }
                    });
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manifest_detail);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        restoreActionBar();
        this.mPlay.setVisibility(8);
        this.mPillar = (Pillar) getIntent().getSerializableExtra(ARGS_CONTENT);
        this.mMedia = (Media) getIntent().getSerializableExtra(CONTENT_MEDIA);
        if (this.mPillar != null) {
            this.mTitle.setText(this.mPillar.getTitle());
            this.mSubtitle.setText(this.mPillar.getCaption());
            Glide.with(this).load(this.mPillar.getPhoto1()).crossFade().into(this.mImage);
            new PillarTask().execute(new String[]{this.mPillar.getId()});
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_manifest_detail, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == 16908332) {
            onBackPressed();
            return true;
        }
        if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction("android.intent.action.SEND");
            sendIntent.putExtra("android.intent.extra.TEXT", this.mPillar.getUrl());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        this.mTitleView.setText(getString(R.string.manifesto));
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
