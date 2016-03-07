package sg.org.pap.pickle.ui.activities;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesUtil;
import it.moondroid.coverflow.BuildConfig;
import java.util.Calendar;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.fragments.CandidateFragment;
import sg.org.pap.pickle.ui.fragments.ContactFragment;
import sg.org.pap.pickle.ui.fragments.FamilyFragment;
import sg.org.pap.pickle.ui.fragments.ManifestoFragment;
import sg.org.pap.pickle.ui.fragments.NewsFragment;
import sg.org.pap.pickle.ui.fragments.PrefsFragment;
import sg.org.pap.pickle.ui.fragments.TownFragment;
import sg.org.pap.pickle.ui.pickle.PickleApp;
import sg.org.pap.pickle.ui.pickle.QuickstartPreferences;
import sg.org.pap.pickle.ui.pickle.RegistrationIntentService;

public class HomeActivity extends BaseActivity implements OnNavigationItemSelectedListener {
    private static final String CONTACT_TAG = "contact";
    public static final String CONTENT = "content";
    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private static final String NAV_ITEM_ID = "navItemId";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "HomeActivity";
    ImageView imgTown;
    @Bind({2131624111})
    DrawerLayout mDrawer;
    private final Handler mDrawerActionHandler = new Handler();
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mInformationTextView;
    @Bind({2131624071})
    ProgressBar mLoading;
    private Message mMessage;
    private int mNavItemId;
    @Bind({2131624113})
    NavigationView mNavigation;
    private String mPostalCode;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private CharSequence mTitle;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    private User mUser;
    private Menu menu;
    TextView tvName;
    TextView tvTown;

    private class MessageTask extends AsyncTask<String, Void, Message> {
        private MessageTask() {
        }

        protected Message doInBackground(String... strings) {
            if (strings.length > 0) {
                return RestClient.getInstance().getMessages(strings[0]);
            }
            return null;
        }

        protected void onPostExecute(Message message) {
            super.onPostExecute(message);
            HomeActivity.this.mMessage = message;
            PickleApp.getInstance().setMessage(HomeActivity.this.mMessage);
            HomeActivity.this.onMessageAttached();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        this.mPostalCode = getIntent().getStringExtra(CONTENT);
        this.mUser = PickleApp.getInstance().getUser();
        this.mMessage = PickleApp.getInstance().getMessage();
        if (savedInstanceState == null) {
            this.mNavItemId = R.id.nav_news;
        } else {
            this.mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }
        if (Connection.connected(this)) {
            if (this.mMessage == null) {
                new MessageTask().execute(new String[]{getMessageType()});
            } else {
                onMessageAttached();
            }
            this.mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    boolean sentToken = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                }
            };
            if (checkPlayServices()) {
                startService(new Intent(this, RegistrationIntentService.class));
                return;
            }
            return;
        }
        Connection.showNoConnectionDialog(this);
    }

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mRegistrationBroadcastReceiver, new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mRegistrationBroadcastReceiver);
        super.onPause();
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

    private void onMessageAttached() {
        ActionBar mActionbar = getSupportActionBar();
        mActionbar.setDisplayHomeAsUpEnabled(true);
        mActionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        View mHeader = ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.drawer_header, null, false);
        this.tvName = (TextView) mHeader.findViewById(R.id.tv_name);
        this.tvTown = (TextView) mHeader.findViewById(R.id.tv_town);
        this.imgTown = (ImageView) mHeader.findViewById(R.id.img_town);
        if (this.mMessage != null) {
            this.mTitle = formatTitle(this.mMessage.getTitle());
        } else {
            this.mTitle = getResources().getString(R.string.app_name);
        }
        if (this.mUser != null) {
            Glide.with(this).load(this.mUser.getConstituencyPhoto()).crossFade().into(this.imgTown);
            this.tvName.setText(this.mUser.getName());
            this.tvTown.setText(this.mUser.getConstituencyName());
            if (!(TextUtils.isEmpty(this.mUser.getName()) || this.mUser.getName().equals("null"))) {
                this.mTitle += ", " + this.mUser.getName();
            }
            if (TextUtils.isEmpty(this.mUser.getPostalCode()) || TextUtils.isEmpty(this.mUser.getTownId())) {
                this.mNavigation.getMenu().findItem(R.id.nav_town).setVisible(false);
            } else {
                this.mNavigation.getMenu().findItem(R.id.nav_town).setVisible(true);
            }
        } else {
            this.tvName.setText(BuildConfig.FLAVOR);
            this.tvTown.setText(BuildConfig.FLAVOR);
            this.mNavigation.getMenu().findItem(R.id.nav_town).setVisible(false);
        }
        this.mNavigation.addHeaderView(mHeader);
        this.mNavigation.setNavigationItemSelectedListener(this);
        this.mNavigation.getMenu().findItem(R.id.nav_news).setChecked(true);
        this.mDrawerToggle = new ActionBarDrawerToggle(this, this.mDrawer, this.mToolbar, R.string.open, R.string.close);
        this.mDrawer.setDrawerListener(this.mDrawerToggle);
        this.mDrawerToggle.syncState();
        onSectionAttached(R.id.nav_news);
        restoreActionBar();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    public void onSectionAttached(int number) {
        MenuItem mSticker = null;
        if (this.menu != null) {
            mSticker = this.menu.findItem(R.id.action_sticker);
            mSticker.setVisible(false);
        }
        setTheme(R.style.AppTheme);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        android.support.v4.app.FragmentTransaction supportTransaction = getSupportFragmentManager().beginTransaction();
        this.mLoading.setVisibility(0);
        if (this.mUser == null) {
            this.mTitle = formatTitle(this.mMessage.getTitle());
        } else if (this.mUser.getName() == null) {
            this.mTitle = formatTitle(this.mMessage.getTitle());
        } else if (!this.mUser.getName().equals("null")) {
            this.mTitle = formatTitle(this.mMessage.getTitle()) + ", " + this.mUser.getName();
        }
        if (getSupportFragmentManager().findFragmentByTag(CONTACT_TAG) != null) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        switch (number) {
            case R.id.nav_news /*2131624246*/:
                if (mSticker != null) {
                    mSticker.setVisible(true);
                }
                transaction.replace(R.id.container, new NewsFragment());
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
                this.mLoading.setVisibility(8);
                break;
            case R.id.nav_manifest /*2131624247*/:
                this.mTitle = getString(R.string.manifesto);
                transaction.replace(R.id.container, new ManifestoFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                this.mLoading.setVisibility(8);
                this.mTitle = getString(R.string.manifesto);
                break;
            case R.id.nav_yayf /*2131624248*/:
                this.mTitle = getString(R.string.you_family);
                transaction.replace(R.id.container, new FamilyFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                this.mLoading.setVisibility(8);
                break;
            case R.id.nav_candidates /*2131624249*/:
                this.mTitle = getString(R.string.candidates);
                transaction.replace(R.id.container, new CandidateFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                this.mLoading.setVisibility(8);
                break;
            case R.id.nav_touch /*2131624250*/:
                this.mTitle = getString(R.string.keep_touch);
                supportTransaction.replace(R.id.container, new ContactFragment(), CONTACT_TAG);
                supportTransaction.addToBackStack(null);
                supportTransaction.commit();
                this.mLoading.setVisibility(8);
                break;
            case R.id.nav_prefs /*2131624251*/:
                this.mTitle = getString(R.string.prefereces);
                transaction.replace(R.id.container, new PrefsFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                this.mLoading.setVisibility(8);
                break;
            case R.id.nav_town /*2131624252*/:
                this.mTitle = this.mUser.getTownName();
                transaction.replace(R.id.container, new TownFragment());
                transaction.addToBackStack(null);
                transaction.commit();
                this.mLoading.setVisibility(8);
                break;
        }
        restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(BuildConfig.FLAVOR);
        this.mTitleView.setText(this.mTitle);
    }

    public void restoreActionBar(String title) {
        this.mTitle = title;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(BuildConfig.FLAVOR);
        this.mTitleView.setText(this.mTitle);
    }

    public boolean onNavigationItemSelected(final MenuItem menuItem) {
        menuItem.setChecked(true);
        this.mNavItemId = menuItem.getItemId();
        this.mDrawer.closeDrawer(8388611);
        this.mDrawerActionHandler.postDelayed(new Runnable() {
            public void run() {
                HomeActivity.this.onSectionAttached(menuItem.getItemId());
                HomeActivity.this.restoreActionBar();
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.home) {
            return this.mDrawerToggle.onOptionsItemSelected(item);
        }
        if (item.getItemId() == 16908332) {
            this.mDrawer.openDrawer(8388611);
            return true;
        }
        if (item.getItemId() == R.id.action_sticker) {
            startActivity(new Intent(this, StickerActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (this.mDrawer.isDrawerOpen(8388611)) {
            this.mDrawer.closeDrawer(8388611);
        } else {
            super.onBackPressed();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, this.mNavItemId);
    }

    private String formatTitle(String s) {
        s = s.toLowerCase();
        StringBuilder result = new StringBuilder(s.length());
        String[] words = s.split("\\s");
        int l = words.length;
        for (int i = 0; i < l; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
        }
        return result.toString();
    }

    public void updateHeaderNavigation(User user) {
        if (user != null) {
            this.mUser = user;
            this.tvName.setText(user.getName());
            if (TextUtils.isEmpty(this.mUser.getPostalCode()) || TextUtils.isEmpty(this.mUser.getTownId())) {
                this.imgTown.setVisibility(8);
                this.tvTown.setText(BuildConfig.FLAVOR);
                this.mNavigation.getMenu().findItem(R.id.nav_town).setVisible(false);
            } else {
                this.imgTown.setVisibility(0);
                Glide.with(this).load(user.getConstituencyPhoto()).crossFade().into(this.imgTown);
                this.tvTown.setText(user.getConstituencyName());
                if (!TextUtils.isEmpty(user.getName())) {
                    this.mTitle = formatTitle(this.mMessage.getTitle());
                    this.mTitle += ", " + user.getName();
                }
                this.mNavigation.getMenu().findItem(R.id.nav_town).setVisible(true);
            }
            restoreActionBar();
            return;
        }
        this.mNavigation.getMenu().findItem(R.id.nav_town).setVisible(false);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == 0) {
            return true;
        }
        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        } else {
            Log.i(TAG, "This device is not supported.");
            finish();
        }
        return false;
    }
}
