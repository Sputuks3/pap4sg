package sg.org.pap.pickle.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
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
import sg.org.pap.pickle.models.MessageContact;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.activities.HomeActivity;
import sg.org.pap.pickle.ui.activities.WebplayerActivity;
import sg.org.pap.pickle.ui.activities.WebviewActivity;
import sg.org.pap.pickle.ui.base.BaseSupportFragment;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class ContactFragment extends BaseSupportFragment implements OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private CallbackManager mCallbackManager;
    @Bind({2131624185})
    RelativeLayout mContentLayout;
    @Bind({2131624100})
    TextView mEmail;
    @Bind({2131624078})
    ImageButton mFacebook;
    @Bind({2131624079})
    ImageButton mInsta;
    @Bind({2131624071})
    ProgressBar mLoading;
    @Bind({2131624081})
    ImageButton mPinterest;
    private ProgressDialog mProgressDialog;
    @Bind({2131624098})
    RelativeLayout mRelativeEmail;
    @Bind({2131624102})
    RelativeLayout mRelativeWeb;
    @Bind({2131624187})
    Button mSubscribe;
    @Bind({2131624080})
    ImageButton mTwitter;
    private User mUser;
    private MessageContact mValues;
    @Bind({2131624104})
    TextView mWebsite;

    private class ContactsTask extends AsyncTask<Void, Void, MessageContact> {
        private ContactsTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            ContactFragment.this.mContentLayout.setVisibility(8);
            ContactFragment.this.mLoading.setVisibility(0);
        }

        protected MessageContact doInBackground(Void... voids) {
            return RestClient.getInstance().getContacts();
        }

        protected void onPostExecute(MessageContact messageContact) {
            super.onPostExecute(messageContact);
            ContactFragment.this.mValues = messageContact;
            PickleApp.getInstance().setmContact(ContactFragment.this.mValues);
            if (ContactFragment.this.mValues != null) {
                ContactFragment.this.mEmail.setText(ContactFragment.this.mValues.getContent().getEmail());
                ContactFragment.this.mWebsite.setText(ContactFragment.this.mValues.getContent().getWebsite());
                ContactFragment.this.mFacebook.setOnClickListener(ContactFragment.this);
                ContactFragment.this.mInsta.setOnClickListener(ContactFragment.this);
                ContactFragment.this.mTwitter.setOnClickListener(ContactFragment.this);
                ContactFragment.this.mPinterest.setOnClickListener(ContactFragment.this);
                ContactFragment.this.mEmail.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        Intent intent = new Intent("android.intent.action.SEND", Uri.fromParts("mailto:", ContactFragment.this.mValues.getContent().getEmail(), null));
                        intent.setType("message/rfc822");
                        intent.putExtra("android.intent.extra.EMAIL", new String[]{ContactFragment.this.mValues.getContent().getEmail()});
                        intent.putExtra("android.intent.extra.SUBJECT", "Mail from #PAP4SG App");
                        intent.putExtra("android.intent.extra.TEXT", BuildConfig.FLAVOR);
                        ContactFragment.this.startActivity(Intent.createChooser(intent, "Send Email"));
                    }
                });
                ContactFragment.this.mRelativeEmail.setOnClickListener(ContactFragment.this);
                ContactFragment.this.mWebsite.setOnClickListener(ContactFragment.this);
                ContactFragment.this.mSubscribe.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        ((HomeActivity) ContactFragment.this.getActivity()).onSectionAttached(R.id.nav_prefs);
                    }
                });
            }
            ContactFragment.this.mContentLayout.setVisibility(0);
            ContactFragment.this.mLoading.setVisibility(8);
        }
    }

    class FacebookAsync extends AsyncTask<Void, Void, Void> {
        private final String mUrl;

        public FacebookAsync(String url) {
            this.mUrl = url;
            ContactFragment.this.mProgressDialog = new ProgressDialog(ContactFragment.this.getActivity());
        }

        protected void onPreExecute() {
            super.onPreExecute();
            ContactFragment.this.mProgressDialog.setMessage("Loading Faceboook...");
            ContactFragment.this.mProgressDialog.show();
        }

        protected Void doInBackground(Void... voids) {
            ContactFragment.this.callFacebook(this.mUrl);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mValues = PickleApp.getInstance().getmContact();
        this.mUser = PickleApp.getInstance().getUser();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendScreenAnalytics("/contact");
        return inflater.inflate(R.layout.fragment_contact, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (this.mValues != null) {
            this.mContentLayout.setVisibility(0);
            this.mLoading.setVisibility(8);
            this.mEmail.setText(this.mValues.getContent().getEmail());
            this.mWebsite.setText(this.mValues.getContent().getWebsite());
            this.mFacebook.setOnClickListener(this);
            this.mInsta.setOnClickListener(this);
            this.mTwitter.setOnClickListener(this);
            this.mPinterest.setOnClickListener(this);
            this.mEmail.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent("android.intent.action.SEND", Uri.fromParts("mailto:", ContactFragment.this.mValues.getContent().getEmail(), null));
                    intent.setType("message/rfc822");
                    intent.putExtra("android.intent.extra.EMAIL", new String[]{ContactFragment.this.mValues.getContent().getEmail()});
                    intent.putExtra("android.intent.extra.SUBJECT", "Mail from #PAP4SG App");
                    intent.putExtra("android.intent.extra.TEXT", BuildConfig.FLAVOR);
                    ContactFragment.this.startActivity(Intent.createChooser(intent, "Send Email"));
                }
            });
            this.mRelativeEmail.setOnClickListener(this);
            this.mWebsite.setOnClickListener(this);
            this.mSubscribe.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ((HomeActivity) ContactFragment.this.getActivity()).onSectionAttached(R.id.nav_prefs);
                }
            });
        } else if (Connection.connected(getActivity())) {
            new ContactsTask().execute(new Void[0]);
        } else {
            Connection.showSnackBar(getActivity(), getView());
        }
    }

    public void onClick(View view) {
        Intent i = new Intent(getActivity(), WebplayerActivity.class);
        String url = "https://www.pap.org.sg";
        Intent intent;
        switch (view.getId()) {
            case R.id.btn_fb /*2131624078*/:
                url = this.mValues.getContent().getFacebook();
                if (!PickleApp.appInstalled(getActivity(), "com.facebook.katana")) {
                    intent = new Intent(getActivity(), WebplayerActivity.class);
                    intent.putExtra(WebviewActivity.CONTENT, url);
                    startActivity(intent);
                    break;
                }
                new FacebookAsync(url).execute(new Void[0]);
                break;
            case R.id.btn_ig /*2131624079*/:
                url = this.mValues.getContent().getInstagram();
                break;
            case R.id.btn_tw /*2131624080*/:
                url = this.mValues.getContent().getTwitter();
                try {
                    startAnimatedActivity(new Intent("android.intent.action.VIEW", Uri.parse(String.format("twitter://user?screen_name=%s", new Object[]{PickleApp.getLastPath(url)}))));
                    break;
                } catch (Exception e) {
                    intent = new Intent(getActivity(), WebplayerActivity.class);
                    intent.putExtra(WebviewActivity.CONTENT, url);
                    startAnimatedActivity(intent);
                    break;
                }
            case R.id.btn_pin /*2131624081*/:
                url = this.mValues.getContent().getPinterest();
                break;
            case R.id.rl_email /*2131624098*/:
                intent = new Intent("android.intent.action.SEND", Uri.fromParts("mailto:", this.mValues.getContent().getEmail(), null));
                intent.setType("message/rfc822");
                intent.putExtra("android.intent.extra.EMAIL", new String[]{this.mValues.getContent().getEmail()});
                intent.putExtra("android.intent.extra.SUBJECT", "Mail from #PAP4SG App");
                intent.putExtra("android.intent.extra.TEXT", BuildConfig.FLAVOR);
                startActivity(Intent.createChooser(intent, "Send Email"));
                break;
            case R.id.rl_web /*2131624102*/:
            case R.id.tv_website /*2131624104*/:
                url = this.mValues.getContent().getWebsite();
                break;
        }
        if (view.getId() != R.id.btn_tw && view.getId() != R.id.btn_fb && view.getId() != R.id.rl_email) {
            i.putExtra(WebviewActivity.CONTENT, url);
            startAnimatedActivity(i);
        }
    }

    public void callFacebook(final String url) {
        FacebookSdk.sdkInitialize(getActivity());
        this.mCallbackManager = Factory.create();
        LoginManager login = LoginManager.getInstance();
        Collection permissions = new ArrayList();
        permissions.add("public_profile");
        login.logInWithReadPermissions((Fragment) this, permissions);
        login.registerCallback(this.mCallbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newGraphPathRequest(loginResult.getAccessToken(), "/", new Callback() {
                    public void onCompleted(GraphResponse graphResponse) {
                        Log.d("GraphResponse", graphResponse.toString());
                        if (ContactFragment.this.mProgressDialog != null && ContactFragment.this.mProgressDialog.isShowing()) {
                            ContactFragment.this.mProgressDialog.dismiss();
                        }
                        try {
                            ContactFragment.this.startAnimatedActivity(ContactFragment.this.getFacebookIntent(url, graphResponse.getJSONObject().getString(ShareConstants.WEB_DIALOG_PARAM_ID)));
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
                if (ContactFragment.this.mProgressDialog != null && ContactFragment.this.mProgressDialog.isShowing()) {
                    ContactFragment.this.mProgressDialog.dismiss();
                }
            }

            public void onError(FacebookException e) {
                if (ContactFragment.this.mProgressDialog != null && ContactFragment.this.mProgressDialog.isShowing()) {
                    ContactFragment.this.mProgressDialog.dismiss();
                }
            }
        });
    }

    public Intent getFacebookIntent(String url, String id) {
        Uri uri;
        PackageManager pm = getActivity().getPackageManager();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
