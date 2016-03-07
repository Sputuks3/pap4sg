package sg.org.pap.pickle.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import it.moondroid.coverflow.BuildConfig;
import java.util.Calendar;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.api.data.ErrorResponse;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class OnboardingActivity extends BaseActivity {
    @Bind({2131624127})
    ImageView mBackground;
    private Message mContent;
    @Bind({2131624128})
    TextView mGreeting;
    private boolean mLoading;
    @Bind({2131624133})
    Button mLogin;
    @Bind({2131624130})
    TextView mMessage;
    private String mMessageType;
    @Bind({2131624131})
    EditText mName;
    private String mNameValue;
    private int mOptCount;
    @Bind({2131624132})
    EditText mPostal;
    private String mPostalValue;
    @Bind({2131624134})
    TextView mRemind;
    private Message mReminder;
    private User mUser;

    private class LoginTask extends AsyncTask<String, Void, User> {
        ProgressDialog mDialog;
        private ErrorResponse mError;

        private LoginTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            this.mDialog = ProgressDialog.show(OnboardingActivity.this, BuildConfig.FLAVOR, "Loading. Please wait...", true);
        }

        protected User doInBackground(String... strings) {
            User mUser = RestClient.getInstance().login(strings[0], strings[1], strings[2], PickleApp.getInstance().getToken());
            if (mUser != null) {
                if (TextUtils.isEmpty(mUser.getName())) {
                    mUser.setName(strings[2]);
                }
                if (TextUtils.isEmpty(mUser.getPostalCode())) {
                    mUser.setPostalCode(strings[1]);
                }
                if (TextUtils.isEmpty(mUser.getId())) {
                    mUser.setId(strings[0]);
                }
            } else {
                this.mError = RestClient.getInstance().loginFailed(strings[0], strings[1], strings[2]);
            }
            return mUser;
        }

        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            if (this.mDialog.isShowing()) {
                this.mDialog.dismiss();
            }
            if (user != null) {
                PreferenceManager.getDefaultSharedPreferences(OnboardingActivity.this).edit().putString("name_preferences", user.getName()).commit();
                PreferenceManager.getDefaultSharedPreferences(OnboardingActivity.this).edit().putString("postal_preferences", user.getPostalCode()).commit();
                PickleApp.getInstance().setUser(user);
                OnboardingActivity.this.startActivity(new Intent(OnboardingActivity.this, HomeActivity.class));
                OnboardingActivity.this.finish();
            } else if (this.mError != null) {
                PickleApp.showDialog(OnboardingActivity.this, OnboardingActivity.this.getString(R.string.error), this.mError.getMessage(), OnboardingActivity.this.getString(R.string.ok));
            }
        }
    }

    private class MessageTask extends AsyncTask<String, Void, Message> {
        private MessageTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            OnboardingActivity.this.mLoading = true;
        }

        protected Message doInBackground(String... strings) {
            Message mMessage = null;
            if (strings.length > 0) {
                mMessage = RestClient.getInstance().getMessages(strings[0]);
            }
            OnboardingActivity.this.mReminder = RestClient.getInstance().getMessages("4");
            return mMessage;
        }

        protected void onPostExecute(Message message) {
            super.onPostExecute(message);
            OnboardingActivity.this.mLoading = false;
            OnboardingActivity.this.mContent = message;
            PickleApp.getInstance().setMessage(OnboardingActivity.this.mContent);
            if (TextUtils.isEmpty(OnboardingActivity.this.mNameValue) || TextUtils.isEmpty(OnboardingActivity.this.mPostalValue)) {
                if (OnboardingActivity.this.mOptCount == 10) {
                    OnboardingActivity.this.mOptCount = 0;
                    PickleApp.getInstance().setOptOutCount(OnboardingActivity.this.mOptCount);
                    if (OnboardingActivity.this.mReminder == null || TextUtils.isEmpty(OnboardingActivity.this.mReminder.getContent())) {
                        PickleApp.showDialog(OnboardingActivity.this, OnboardingActivity.this.getString(R.string.reminder), OnboardingActivity.this.getString(R.string.opt_out_message), OnboardingActivity.this.getString(R.string.ok));
                    } else {
                        PickleApp.showDialog(OnboardingActivity.this, OnboardingActivity.this.getString(R.string.reminder), OnboardingActivity.this.mReminder.getContent(), OnboardingActivity.this.getString(R.string.ok));
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        Animation fadeInAnimation = AnimationUtils.loadAnimation(OnboardingActivity.this, R.anim.anim_fade_in);
                        OnboardingActivity.this.mRemind.startAnimation(fadeInAnimation);
                        OnboardingActivity.this.mLogin.startAnimation(fadeInAnimation);
                        OnboardingActivity.this.mName.startAnimation(fadeInAnimation);
                        OnboardingActivity.this.mPostal.startAnimation(fadeInAnimation);
                        OnboardingActivity.this.mGreeting.startAnimation(fadeInAnimation);
                        OnboardingActivity.this.mMessage.startAnimation(fadeInAnimation);
                        OnboardingActivity.this.mRemind.setOnClickListener(new OnClickListener() {
                            public void onClick(View view) {
                                int ctr = PickleApp.getInstance().getOptOutCount();
                                if (ctr == 10) {
                                    PickleApp.getInstance().setOptOutCount(0);
                                    if (OnboardingActivity.this.mReminder != null) {
                                        PickleApp.showDialog(OnboardingActivity.this, OnboardingActivity.this.mReminder.getTitle(), OnboardingActivity.this.mReminder.getContent(), OnboardingActivity.this.getString(R.string.ok));
                                        return;
                                    } else {
                                        PickleApp.showDialog(OnboardingActivity.this, OnboardingActivity.this.getString(R.string.reminder), OnboardingActivity.this.getString(R.string.opt_out_message), OnboardingActivity.this.getString(R.string.ok));
                                        return;
                                    }
                                }
                                PickleApp.getInstance().setOptOutCount(ctr + 1);
                                PreferenceManager.getDefaultSharedPreferences(OnboardingActivity.this).edit().clear().commit();
                                PickleApp.getInstance().setUser(null);
                                OnboardingActivity.this.startActivity(new Intent(OnboardingActivity.this, HomeActivity.class));
                                OnboardingActivity.this.finish();
                            }
                        });
                        fadeInAnimation.setAnimationListener(new AnimationListener() {
                            public void onAnimationStart(Animation animation) {
                                OnboardingActivity.this.mGreeting.setVisibility(0);
                                OnboardingActivity.this.mMessage.setVisibility(0);
                                OnboardingActivity.this.bindView();
                            }

                            public void onAnimationRepeat(Animation animation) {
                            }

                            public void onAnimationEnd(Animation animation) {
                            }
                        });
                    }
                }, 500);
                OnboardingActivity.this.mLogin.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        String name = OnboardingActivity.this.mName.getText().toString();
                        String postal = OnboardingActivity.this.mPostal.getText().toString();
                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(postal)) {
                            if (TextUtils.isEmpty(OnboardingActivity.this.mName.getText().toString())) {
                                OnboardingActivity.this.mName.setError(OnboardingActivity.this.getString(R.string.field_required));
                            }
                            if (TextUtils.isEmpty(OnboardingActivity.this.mPostal.getText().toString())) {
                                OnboardingActivity.this.mPostal.setError(OnboardingActivity.this.getString(R.string.field_required));
                                return;
                            }
                            return;
                        }
                        String userId = BuildConfig.FLAVOR;
                        if (PickleApp.getInstance().getUser() != null) {
                            userId = PickleApp.getInstance().getUser().getId();
                        }
                        new LoginTask().execute(new String[]{userId, postal, name});
                    }
                });
                OnboardingActivity.this.mPostal.setOnEditorActionListener(new OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == 6) {
                            String name = OnboardingActivity.this.mName.getText().toString();
                            String postal = OnboardingActivity.this.mPostal.getText().toString();
                            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(postal)) {
                                if (TextUtils.isEmpty(OnboardingActivity.this.mName.getText().toString())) {
                                    OnboardingActivity.this.mName.setError(OnboardingActivity.this.getString(R.string.field_required));
                                }
                                if (TextUtils.isEmpty(OnboardingActivity.this.mPostal.getText().toString())) {
                                    OnboardingActivity.this.mPostal.setError(OnboardingActivity.this.getString(R.string.valid_post));
                                }
                            } else {
                                new LoginTask().execute(new String[]{BuildConfig.FLAVOR, postal, name});
                            }
                        }
                        return false;
                    }
                });
                return;
            }
            new LoginTask().execute(new String[]{BuildConfig.FLAVOR, OnboardingActivity.this.mPostalValue, OnboardingActivity.this.mNameValue});
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUser = PickleApp.getInstance().getUser();
        this.mOptCount = PickleApp.getInstance().getOptOutCount();
        if (this.mUser == null) {
            if (this.mOptCount <= 0 || this.mOptCount >= 10) {
                setContentView(R.layout.activity_onboarding);
                ButterKnife.bind(this);
                this.mGreeting.setVisibility(4);
                this.mMessage.setVisibility(4);
                this.mMessageType = getMessageType();
                this.mNameValue = PreferenceManager.getDefaultSharedPreferences(this).getString("name_preferences", BuildConfig.FLAVOR);
                this.mPostalValue = PreferenceManager.getDefaultSharedPreferences(this).getString("postal_preferences", BuildConfig.FLAVOR);
                return;
            }
            this.mOptCount++;
            PickleApp.getInstance().setOptOutCount(this.mOptCount);
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
            PickleApp.getInstance().setUser(null);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else if (TextUtils.isEmpty(this.mUser.getPostalCode()) && TextUtils.isEmpty(this.mUser.getName())) {
            setContentView(R.layout.activity_onboarding);
            ButterKnife.bind(this);
            this.mGreeting.setVisibility(4);
            this.mMessage.setVisibility(4);
            this.mMessageType = getMessageType();
            this.mNameValue = PreferenceManager.getDefaultSharedPreferences(this).getString("name_preferences", BuildConfig.FLAVOR);
            this.mPostalValue = PreferenceManager.getDefaultSharedPreferences(this).getString("postal_preferences", BuildConfig.FLAVOR);
        } else {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    protected void onResume() {
        super.onResume();
        sendScreenAnalytics("/login");
        if (this.mContent != null) {
            return;
        }
        if (Connection.connected(this)) {
            new MessageTask().execute(new String[]{this.mMessageType});
            return;
        }
        Connection.showNoConnectionDialog(this);
    }

    private String getMessageType() {
        String messageType = Message.TYPE_MORNING;
        int mHourOfDay = Calendar.getInstance().get(11);
        if (mHourOfDay > 5 && mHourOfDay < 12) {
            return Message.TYPE_MORNING;
        }
        if (mHourOfDay < 12 || mHourOfDay >= 19) {
            return Message.TYPE_EVENING;
        }
        return Message.TYPE_AFTERNOON;
    }

    private void bindView() {
        if (this.mContent != null) {
            Glide.with(this).load(this.mContent.getPhoto()).fitCenter().crossFade().into(this.mBackground);
            this.mGreeting.setText(this.mContent.getTitle());
            this.mMessage.setText(this.mContent.getContent());
            this.mName.setText(this.mNameValue);
            this.mPostal.setText(this.mPostalValue);
            return;
        }
        this.mGreeting.setText(BuildConfig.FLAVOR);
        this.mMessage.setText(BuildConfig.FLAVOR);
    }
}
