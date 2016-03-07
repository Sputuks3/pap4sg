package sg.org.pap.pickle.ui.fragments;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.internal.ServerProtocol;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import it.moondroid.coverflow.BuildConfig;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.api.data.ErrorResponse;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.activities.HomeActivity;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class PrefsFragment extends PreferenceFragment {
    CheckBoxPreference mCheckPref;
    EditTextPreference mEmailPref;
    EditTextPreference mNamePref;
    EditTextPreference mPostalPref;
    private boolean mSubscribed;
    private Tracker mTracker;
    private User mUser;

    private class LoginTask extends AsyncTask<String, Void, User> {
        ProgressDialog mDialog;
        private ErrorResponse mError;

        private LoginTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            this.mDialog = ProgressDialog.show(PrefsFragment.this.getActivity(), BuildConfig.FLAVOR, "Loading. Please wait...", true);
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
            if (user != null) {
                PreferenceManager.getDefaultSharedPreferences(PrefsFragment.this.getActivity()).edit().putString("name_preferences", user.getName()).commit();
                PreferenceManager.getDefaultSharedPreferences(PrefsFragment.this.getActivity()).edit().putString("postal_preferences", user.getPostalCode()).commit();
                PickleApp.getInstance().setUser(user);
                ((HomeActivity) PrefsFragment.this.getActivity()).updateHeaderNavigation(user);
            } else {
                user = PickleApp.getInstance().getUser();
                if (this.mError != null) {
                    user.setPostalCode(BuildConfig.FLAVOR);
                    user.setTownId(BuildConfig.FLAVOR);
                    PickleApp.getInstance().setUser(user);
                    PreferenceManager.getDefaultSharedPreferences(PrefsFragment.this.getActivity()).edit().clear();
                    Toast.makeText(PrefsFragment.this.getActivity(), this.mError.getMessage(), 1).show();
                    ((HomeActivity) PrefsFragment.this.getActivity()).updateHeaderNavigation(user);
                }
            }
            if (this.mDialog.isShowing()) {
                this.mDialog.dismiss();
            }
        }
    }

    private class Subscribe extends AsyncTask<User, Void, User> {
        ProgressDialog mDialog;
        String mEmail;
        String mName;
        String mPostalCode;
        String mSubscribe;

        private Subscribe() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            this.mName = PrefsFragment.this.mNamePref.getText();
            this.mPostalCode = PrefsFragment.this.mPostalPref.getText();
            this.mEmail = PrefsFragment.this.mEmailPref.getText();
            this.mDialog = ProgressDialog.show(PrefsFragment.this.getActivity(), BuildConfig.FLAVOR, "Loading. Please wait...", true);
            this.mSubscribe = PrefsFragment.this.mCheckPref.isChecked() ? Message.TYPE_PDPA : AppEventsConstants.EVENT_PARAM_VALUE_NO;
        }

        protected User doInBackground(User... users) {
            boolean z = false;
            User param = users[0];
            try {
                this.mSubscribe = PrefsFragment.this.mSubscribed ? AppEventsConstants.EVENT_PARAM_VALUE_NO : Message.TYPE_PDPA;
                PrefsFragment prefsFragment = PrefsFragment.this;
                if (!PrefsFragment.this.mSubscribed) {
                    z = true;
                }
                prefsFragment.mSubscribed = z;
                if (TextUtils.isEmpty(param.getId())) {
                    return RestClient.getInstance().updateSubscription(param.getEmail(), this.mSubscribe, this.mName, this.mPostalCode, BuildConfig.FLAVOR);
                }
                return RestClient.getInstance().updateSubscription(param.getEmail(), this.mSubscribe, this.mName, this.mPostalCode, param.getId());
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            PickleApp.getInstance().setUser(user);
            if (this.mDialog.isShowing()) {
                this.mDialog.dismiss();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        if (VERSION.SDK_INT >= 21) {
            getActivity().setTheme(R.style.AppTheme_Prefs);
        }
        super.onCreate(savedInstanceState);
        this.mTracker = PickleApp.getInstance().getDefaultTracker();
        addPreferencesFromResource(R.xml.prefs_pickle);
        this.mUser = PickleApp.getInstance().getUser();
        this.mNamePref = (EditTextPreference) findPreference("name_preferences");
        this.mPostalPref = (EditTextPreference) findPreference("postal_preferences");
        this.mCheckPref = (CheckBoxPreference) findPreference("sign_up");
        this.mEmailPref = (EditTextPreference) findPreference("email_preferences");
        if (this.mUser != null) {
            if (!TextUtils.isEmpty(this.mUser.getName())) {
                this.mNamePref.setText(this.mUser.getName());
                this.mNamePref.setSummary(this.mUser.getName());
            }
            if (!TextUtils.isEmpty(this.mUser.getPostalCode())) {
                this.mPostalPref.setText(this.mUser.getPostalCode());
                this.mPostalPref.setSummary(this.mUser.getPostalCode());
            }
            if (!TextUtils.isEmpty(this.mUser.getEmail())) {
                this.mEmailPref.setText(this.mUser.getEmail());
                this.mEmailPref.setSummary(this.mUser.getEmail());
            }
            this.mCheckPref.setChecked(this.mUser.isSubscription());
        }
        this.mNamePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary(o.toString());
                if (PrefsFragment.this.mUser == null) {
                    PrefsFragment.this.mUser = new User();
                }
                PrefsFragment.this.mUser.setName(o.toString());
                PickleApp.getInstance().setUser(PrefsFragment.this.mUser);
                ((HomeActivity) PrefsFragment.this.getActivity()).updateHeaderNavigation(PrefsFragment.this.mUser);
                return true;
            }
        });
        this.mPostalPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                preference.setSummary(o.toString());
                if (PrefsFragment.this.mUser == null) {
                    PrefsFragment.this.mUser = new User();
                }
                PrefsFragment.this.mUser.setPostalCode(o.toString());
                PickleApp.getInstance().setUser(PrefsFragment.this.mUser);
                if (!TextUtils.isEmpty(PrefsFragment.this.mUser.getPostalCode())) {
                    new LoginTask().execute(new String[]{BuildConfig.FLAVOR, PrefsFragment.this.mUser.getPostalCode(), PrefsFragment.this.mUser.getName()});
                }
                return true;
            }
        });
        this.mEmailPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().isEmpty()) {
                    PrefsFragment.this.mCheckPref.setChecked(false);
                } else {
                    PrefsFragment.this.mUser.setEmail(newValue.toString());
                    PickleApp.getInstance().setUser(PrefsFragment.this.mUser);
                    preference.setSummary(newValue.toString());
                }
                return true;
            }
        });
        this.mCheckPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (o.toString().equals(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE)) {
                    View promptsView = LayoutInflater.from(PrefsFragment.this.getActivity()).inflate(R.layout.prompt_email, null);
                    Builder alertDialogBuilder = new Builder(PrefsFragment.this.getActivity());
                    alertDialogBuilder.setView(promptsView);
                    final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                    if (PrefsFragment.this.mUser != null) {
                        userInput.setText(PrefsFragment.this.mUser.getEmail());
                    }
                    alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            PrefsFragment.this.mCheckPref.setChecked(true);
                            PrefsFragment.this.mEmailPref.setText(userInput.getText().toString());
                            PrefsFragment.this.mEmailPref.setSummary(userInput.getText().toString());
                            if (PrefsFragment.this.mUser == null) {
                                PrefsFragment.this.mUser = new User();
                            }
                            PrefsFragment.this.mUser.setEmail(userInput.getText().toString());
                            PickleApp.getInstance().setUser(PrefsFragment.this.mUser);
                            dialog.cancel();
                            new Subscribe().execute(new User[]{PrefsFragment.this.mUser});
                        }
                    }).setNegativeButton("Cancel", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            PrefsFragment.this.mCheckPref.setChecked(false);
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    LayoutParams lp = new LayoutParams();
                    lp.copyFrom(alertDialog.getWindow().getAttributes());
                    lp.width = -1;
                    lp.height = -1;
                    alertDialog.show();
                } else {
                    new Subscribe().execute(new User[]{PrefsFragment.this.mUser});
                }
                return true;
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.mTracker.setScreenName("/preferences");
        this.mTracker.send(new ScreenViewBuilder().build());
        this.mSubscribed = this.mCheckPref.isChecked();
    }

    public void onPause() {
        super.onPause();
    }
}
