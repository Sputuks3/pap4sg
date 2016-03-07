package sg.org.pap.pickle.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import it.moondroid.coverflow.BuildConfig;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.ui.base.BaseActivity;

public class MessageActivity extends BaseActivity {
    String mContentType;
    @Bind({2131624124})
    ProgressBar mLoading;
    String mTitle;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    @Bind({2131624122})
    TextView mTvContent;
    @Bind({2131624125})
    WebView mWeb;

    private class MessageTask extends AsyncTask<String, Void, Message> {
        private MessageTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            MessageActivity.this.mLoading.setVisibility(0);
        }

        protected Message doInBackground(String... strings) {
            return RestClient.getInstance().getMessages(strings[0]);
        }

        protected void onPostExecute(Message message) {
            super.onPostExecute(message);
            MessageActivity.this.mTvContent.setText(message.getContent());
            MessageActivity.this.mLoading.setVisibility(8);
            MessageActivity.this.mWeb.getSettings().setJavaScriptEnabled(true);
            MessageActivity.this.mWeb.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Intent intent = new Intent(MessageActivity.this, WebplayerActivity.class);
                    intent.putExtra(WebviewActivity.CONTENT, url);
                    MessageActivity.this.startActivity(intent);
                    return true;
                }
            });
            MessageActivity.this.mWeb.loadDataWithBaseURL(BuildConfig.FLAVOR, message.getContent(), "text/html", "UTF-8", BuildConfig.FLAVOR);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        if (getIntent().getAction().equals("org.sg.pap.terms")) {
            this.mTitle = "Terms of Use";
            this.mContentType = Message.TYPE_TERMS_OF_USE;
            sendScreenAnalytics("/termsOfUse");
        } else {
            this.mTitle = "PDPA";
            this.mContentType = Message.TYPE_PDPA;
            sendScreenAnalytics("/pdpa");
        }
        new MessageTask().execute(new String[]{this.mContentType});
        restoreActionBar();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        onBackPressed();
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        this.mTitleView.setText(this.mTitle);
    }

    protected void onPause() {
        super.onPause();
        if (VERSION.SDK_INT >= 11) {
            this.mWeb.onPause();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mWeb.destroy();
    }
}
