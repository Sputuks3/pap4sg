package sg.org.pap.pickle.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.CallbackManager.Factory;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.internal.ShareConstants;
import it.moondroid.coverflow.BuildConfig;
import java.util.ArrayList;
import java.util.Collection;
import org.json.JSONException;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.Representative;
import sg.org.pap.pickle.models.RepresentativeDetails;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class CandidateDetail extends BaseActivity implements OnClickListener {
    public static final String ARGS_CONTENT = "content";
    @Bind({2131624106})
    RelativeLayout mBranch;
    @Bind({2131624078})
    ImageButton mBtnFb;
    @Bind({2131624079})
    ImageButton mBtnIg;
    @Bind({2131624081})
    ImageButton mBtnPin;
    @Bind({2131624076})
    ImageView mBtnPlay;
    @Bind({2131624080})
    ImageButton mBtnTw;
    private CallbackManager mCallbackManager;
    @Bind({2131624072})
    ScrollView mContentScroll;
    RepresentativeDetails mDetails;
    @Bind({2131624098})
    RelativeLayout mEmail;
    @Bind({2131624086})
    RelativeLayout mFacebook;
    @Bind({2131624090})
    RelativeLayout mInstagram;
    @Bind({2131624071})
    ProgressBar mLoading;
    @Bind({2131624094})
    RelativeLayout mPinterest;
    @Bind({2131624218})
    ImageView mProfile;
    private ProgressDialog mProgressDialog;
    Representative mRep;
    @Bind({2131624077})
    LinearLayout mSocials;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    @Bind({2131624108})
    TextView mTvBranch;
    @Bind({2131624219})
    TextView mTvDesignation;
    @Bind({2131624100})
    TextView mTvEmail;
    @Bind({2131624088})
    TextView mTvFacebook;
    @Bind({2131624092})
    TextView mTvInstagram;
    @Bind({2131624144})
    TextView mTvName;
    @Bind({2131624096})
    TextView mTvPinterest;
    @Bind({2131624217})
    TextView mTvQuote;
    @Bind({2131624181})
    TextView mTvTown;
    @Bind({2131624084})
    TextView mTvTwitter;
    @Bind({2131624104})
    TextView mTvWebsite;
    @Bind({2131624082})
    RelativeLayout mTwitter;
    @Bind({2131624074})
    FrameLayout mVideoFrame;
    @Bind({2131624075})
    ImageView mVideoThumb;
    @Bind({2131624102})
    RelativeLayout mWebsite;

    class FacebookAsync extends AsyncTask<Void, Void, Void> {
        private final String mUrl;

        public FacebookAsync(String url) {
            this.mUrl = url;
            CandidateDetail.this.mProgressDialog = new ProgressDialog(CandidateDetail.this);
        }

        protected void onPreExecute() {
            super.onPreExecute();
            CandidateDetail.this.mProgressDialog.setMessage("Loading Faceboook...");
            CandidateDetail.this.mProgressDialog.show();
        }

        protected Void doInBackground(Void... voids) {
            CandidateDetail.this.callFacebook(this.mUrl);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class RepsTask extends AsyncTask<String, Void, RepresentativeDetails> {
        private RepsTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            CandidateDetail.this.mLoading.setVisibility(0);
        }

        protected RepresentativeDetails doInBackground(String... strings) {
            try {
                return RestClient.getInstance().getRepresentative(strings[0]);
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(RepresentativeDetails representativeDetails) {
            super.onPostExecute(representativeDetails);
            CandidateDetail.this.mDetails = representativeDetails;
            CandidateDetail.this.bindView(CandidateDetail.this.mDetails);
            CandidateDetail.this.mLoading.setVisibility(8);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_detail);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        this.mRep = (Representative) getIntent().getSerializableExtra(ARGS_CONTENT);
        if (this.mRep != null) {
            this.mTvName.setText(this.mRep.getName());
            this.mTvDesignation.setText(this.mRep.getDesignation());
            this.mTvTown.setText(this.mRep.getConstituencyName());
            this.mTvQuote.setText(this.mRep.getDescription());
            if (!TextUtils.isEmpty(this.mRep.getPhoto2())) {
                Glide.with(this).load(this.mRep.getPhoto2()).crossFade().into(this.mProfile);
            } else if (!TextUtils.isEmpty(this.mRep.getPhoto1())) {
                Glide.with(this).load(this.mRep.getPhoto1()).crossFade().into(this.mProfile);
            }
            new RepsTask().execute(new String[]{this.mRep.getRid()});
        }
        restoreActionBar();
    }

    protected void onResume() {
        super.onResume();
        sendScreenAnalytics("/representatives/detail");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_candidate_detail, menu);
        return true;
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
            sendIntent.putExtra("android.intent.extra.TEXT", this.mDetails.getSocial().getWebsite());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        if (this.mRep != null) {
            this.mTitleView.setText(this.mRep.getName());
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void onClick(View view) {
        Intent intent;
        String url = ((TextView) view).getText().toString();
        if (view.getId() == R.id.tv_facebook) {
            if (PickleApp.appInstalled(this, "com.facebook.katana")) {
                new FacebookAsync(url).execute(new Void[0]);
                return;
            }
            intent = new Intent(this, WebplayerActivity.class);
            intent.putExtra(WebviewActivity.CONTENT, url);
            startActivity(intent);
        } else if (view.getId() == R.id.tv_twitter) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(String.format("twitter://user?screen_name=%s", new Object[]{PickleApp.getLastPath(url)}))));
            } catch (Exception e) {
                intent = new Intent(this, WebplayerActivity.class);
                intent.putExtra(WebviewActivity.CONTENT, url);
                startActivity(intent);
            }
        } else {
            intent = new Intent(this, WebplayerActivity.class);
            intent.putExtra(WebviewActivity.CONTENT, url);
            startActivity(intent);
        }
    }

    private void setClick(View view, String url) {
        if (view.getId() == R.id.btn_fb) {
            new FacebookAsync(url).execute(new Void[0]);
        } else if (view.getId() == R.id.btn_tw) {
            try {
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(String.format("twitter://user?screen_name=%s", new Object[]{PickleApp.getLastPath(url)}))));
            } catch (Exception e) {
                intent = new Intent(this, WebplayerActivity.class);
                intent.putExtra(WebviewActivity.CONTENT, url);
                startActivity(intent);
            }
        } else {
            intent = new Intent(this, WebplayerActivity.class);
            intent.putExtra(WebviewActivity.CONTENT, url);
            startActivity(intent);
        }
    }

    public void bindView(final RepresentativeDetails details) {
        if (details != null) {
            if (TextUtils.isEmpty(details.getVideoUrl())) {
                this.mVideoFrame.setVisibility(8);
                this.mVideoThumb.setVisibility(8);
                this.mBtnPlay.setVisibility(8);
                this.mSocials.setVisibility(8);
            } else {
                this.mVideoFrame.setVisibility(0);
                this.mVideoThumb.setVisibility(0);
                this.mBtnPlay.setVisibility(0);
                Glide.with(this).load(details.getVideoThumbnail()).crossFade().into(this.mVideoThumb);
                this.mBtnPlay.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(CandidateDetail.this, VideoPlayerActivity.class);
                        intent.putExtra(WebviewActivity.CONTENT, details.getVideoUrl());
                        CandidateDetail.this.startActivity(intent);
                    }
                });
            }
            this.mBtnFb.setVisibility(8);
            this.mBtnIg.setVisibility(8);
            this.mBtnTw.setVisibility(8);
            this.mBtnPin.setVisibility(8);
            if (TextUtils.isEmpty(details.getSocial().getFacebook())) {
                this.mFacebook.setVisibility(8);
            } else {
                this.mTvFacebook.setText(details.getSocial().getFacebook());
                this.mTvFacebook.setOnClickListener(this);
            }
            if (TextUtils.isEmpty(details.getSocial().getTwitter())) {
                this.mTwitter.setVisibility(8);
            } else {
                this.mTvTwitter.setText(details.getSocial().getTwitter());
                this.mTvTwitter.setOnClickListener(this);
            }
            if (TextUtils.isEmpty(details.getSocial().getInstagram())) {
                this.mInstagram.setVisibility(8);
            } else {
                this.mTvInstagram.setText(details.getSocial().getInstagram());
                this.mTvInstagram.setOnClickListener(this);
            }
            if (TextUtils.isEmpty(details.getSocial().getPinterest())) {
                this.mPinterest.setVisibility(8);
            } else {
                this.mTvPinterest.setText(details.getSocial().getPinterest());
                this.mTvPinterest.setOnClickListener(this);
            }
            if (TextUtils.isEmpty(details.getSocial().getWebsite())) {
                this.mWebsite.setVisibility(8);
            } else {
                this.mTvWebsite.setText(details.getSocial().getWebsite());
                this.mTvWebsite.setOnClickListener(this);
            }
            if (TextUtils.isEmpty(details.getSocial().getEmail())) {
                this.mEmail.setVisibility(8);
            } else {
                final String email = details.getSocial().getEmail();
                this.mTvEmail.setText(details.getSocial().getEmail());
                this.mTvEmail.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent("android.intent.action.SEND", Uri.fromParts("mailto:", email.trim(), null));
                        intent.setType("message/rfc822");
                        intent.putExtra("android.intent.extra.EMAIL", new String[]{email});
                        intent.putExtra("android.intent.extra.SUBJECT", "Mail from #PAP4SG App");
                        intent.putExtra("android.intent.extra.TEXT", BuildConfig.FLAVOR);
                        CandidateDetail.this.startActivity(Intent.createChooser(intent, "Send Email"));
                    }
                });
            }
            if (TextUtils.isEmpty(details.getSocial().getMisc())) {
                this.mBranch.setVisibility(8);
            } else {
                this.mTvBranch.setText(details.getSocial().getMisc());
            }
            if (TextUtils.isEmpty(this.mRep.getPhoto2()) && !TextUtils.isEmpty(this.mDetails.getPhoto2())) {
                Glide.with(this).load(this.mDetails.getPhoto2()).crossFade().into(this.mProfile);
            }
            if (TextUtils.isEmpty(this.mDetails.getDescription())) {
                this.mTvQuote.setText("\n\n\n");
            } else {
                String desc = this.mDetails.getDescription();
                this.mTvQuote.setText(this.mDetails.getDescription());
                for (int j = this.mTvQuote.getLineCount(); j < 3; j++) {
                    desc = desc + "\n";
                }
                this.mTvQuote.setText(desc);
            }
            this.mContentScroll.setVisibility(0);
        }
    }

    public void callFacebook(final String url) {
        FacebookSdk.sdkInitialize(this);
        this.mCallbackManager = Factory.create();
        LoginManager login = LoginManager.getInstance();
        Collection permissions = new ArrayList();
        permissions.add("public_profile");
        login.logInWithReadPermissions((Activity) this, permissions);
        login.registerCallback(this.mCallbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newGraphPathRequest(loginResult.getAccessToken(), "/", new Callback() {
                    public void onCompleted(GraphResponse graphResponse) {
                        Log.d("GraphResponse", graphResponse.toString());
                        if (CandidateDetail.this.mProgressDialog != null && CandidateDetail.this.mProgressDialog.isShowing()) {
                            CandidateDetail.this.mProgressDialog.dismiss();
                        }
                        try {
                            CandidateDetail.this.startActivity(CandidateDetail.this.getFacebookIntent(url, graphResponse.getJSONObject().getString(ShareConstants.WEB_DIALOG_PARAM_ID)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString(ShareConstants.WEB_DIALOG_PARAM_ID, url);
                request.setParameters(parameters);
                request.executeAsync();
            }

            public void onCancel() {
                if (CandidateDetail.this.mProgressDialog != null && CandidateDetail.this.mProgressDialog.isShowing()) {
                    CandidateDetail.this.mProgressDialog.dismiss();
                }
            }

            public void onError(FacebookException e) {
                if (CandidateDetail.this.mProgressDialog != null && CandidateDetail.this.mProgressDialog.isShowing()) {
                    CandidateDetail.this.mProgressDialog.dismiss();
                }
            }
        });
    }

    public Intent getFacebookIntent(String url, String id) {
        Uri uri;
        PackageManager pm = getPackageManager();
        try {
            if (TextUtils.isEmpty(id)) {
                uri = Uri.parse(url);
            } else {
                pm.getPackageInfo("com.facebook.katana", 0);
                uri = Uri.parse("fb://page/" + id);
            }
        } catch (NameNotFoundException e) {
            uri = Uri.parse(url);
        }
        return new Intent("android.intent.action.VIEW", uri);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
