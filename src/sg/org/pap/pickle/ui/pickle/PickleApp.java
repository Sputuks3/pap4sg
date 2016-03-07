package sg.org.pap.pickle.ui.pickle;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.splunk.mint.Mint;
import it.moondroid.coverflow.BuildConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.data.HomeResponse;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.MessageContact;
import sg.org.pap.pickle.models.Pillar;
import sg.org.pap.pickle.models.Town;
import sg.org.pap.pickle.models.User;

public class PickleApp extends Application {
    private static final String DEVICE_TOKEN = "token";
    private static final String MANIFESTO_MEDIA = "manifesto_media";
    private static final String OPT_OUT_CTR = "out_ctr";
    private static final String PHOTO = "current_photo";
    private static final String PICKLE_PREF = "pap_pref";
    private static final String USER = "user";
    private static final String YAYF_MEDIA = "yayf_media";
    public static PickleApp singleton;
    private MessageContact mContact;
    private String mCurrentPhoto;
    private HomeResponse mHome;
    private boolean mManifest;
    private Message mMessage;
    private List<Pillar> mPillars;
    private SharedPreferences mPrefs;
    private String mToken;
    private Town mTown;
    private Tracker mTracker;
    private User mUser;
    private boolean mYayf;
    private int optOutCount;

    public void onCreate() {
        super.onCreate();
        this.mPrefs = getSharedPreferences(PICKLE_PREF, 0);
        singleton = this;
        Mint.initAndStartSession(this, "e4801382");
    }

    public synchronized Tracker getDefaultTracker() {
        if (this.mTracker == null) {
            this.mTracker = GoogleAnalytics.getInstance(this).newTracker(R.xml.global_tracker);
        }
        return this.mTracker;
    }

    public static PickleApp getInstance() {
        return singleton;
    }

    public User getUser() {
        Gson gson = new Gson();
        String json = this.mPrefs.getString(USER, BuildConfig.FLAVOR);
        if (TextUtils.isEmpty(json)) {
            this.mUser = null;
        } else {
            this.mUser = (User) gson.fromJson(json, User.class);
        }
        return this.mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
        Editor prefsEditor = this.mPrefs.edit();
        prefsEditor.putString(USER, new Gson().toJson(mUser));
        prefsEditor.commit();
    }

    public Message getMessage() {
        return this.mMessage;
    }

    public void setMessage(Message mMessage) {
        this.mMessage = mMessage;
    }

    public HomeResponse getmHome() {
        return this.mHome;
    }

    public void setmHome(HomeResponse mHome) {
        this.mHome = mHome;
    }

    public List<Pillar> getmPillars() {
        return this.mPillars;
    }

    public void setmPillars(List<Pillar> mPillars) {
        this.mPillars = mPillars;
    }

    public MessageContact getmContact() {
        return this.mContact;
    }

    public void setmContact(MessageContact mContact) {
        this.mContact = mContact;
    }

    public Town getmTown() {
        return this.mTown;
    }

    public void setmTown(Town mTown) {
        this.mTown = mTown;
    }

    public static String getDateDifference(Date date) {
        long diffInSec = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - date.getTime());
        long seconds = diffInSec % 60;
        diffInSec /= 60;
        long minutes = diffInSec % 60;
        diffInSec /= 60;
        long hours = diffInSec % 24;
        diffInSec /= 24;
        long days = diffInSec;
        if (days == 1) {
            return String.format("%d d", new Object[]{Long.valueOf(days)});
        } else if (days > 1) {
            return String.format("%d d", new Object[]{Long.valueOf(days)});
        } else if (hours == 1) {
            return String.format("%d h", new Object[]{Long.valueOf(hours)});
        } else if (hours > 1) {
            return String.format("%d h", new Object[]{Long.valueOf(hours)});
        } else if (minutes == 1) {
            return String.format("%d m", new Object[]{Long.valueOf(minutes)});
        } else if (minutes > 1) {
            return String.format("%d m", new Object[]{Long.valueOf(minutes)});
        } else if (diffInSec == 1) {
            return String.format("%d s", new Object[]{Long.valueOf(diffInSec)});
        } else {
            return String.format("%d s", new Object[]{Long.valueOf(diffInSec)});
        }
    }

    public static String getDateDifference(String date) {
        Date paramDate;
        try {
            paramDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            paramDate = new Date();
        }
        return getDateDifference(paramDate);
    }

    public static String getFormalDate(String date) {
        Date paramDate;
        try {
            paramDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (ParseException e) {
            paramDate = new Date();
        }
        return new SimpleDateFormat("EEEE, dd MMM yyyy").format(paramDate);
    }

    public static void showDialog(Context ctx1, String title, String message, String button) {
        Builder builder = new Builder(ctx1);
        builder.setCancelable(true);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton(button, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public int getOptOutCount() {
        this.optOutCount = this.mPrefs.getInt(OPT_OUT_CTR, 0);
        return this.optOutCount;
    }

    public void setOptOutCount(int optOutCount) {
        this.optOutCount = optOutCount;
        this.mPrefs.edit().putInt(OPT_OUT_CTR, this.optOutCount).commit();
    }

    public static String getLastPath(String url) {
        URI uri;
        URISyntaxException e;
        try {
            URI uri2 = new URI(url);
            try {
                String path = uri2.getPath();
                uri = uri2;
                return path.substring(path.lastIndexOf(47) + 1);
            } catch (URISyntaxException e2) {
                e = e2;
                uri = uri2;
                e.printStackTrace();
                return BuildConfig.FLAVOR;
            }
        } catch (URISyntaxException e3) {
            e = e3;
            e.printStackTrace();
            return BuildConfig.FLAVOR;
        }
    }

    public String getCurrentPhoto() {
        this.mCurrentPhoto = this.mPrefs.getString(PHOTO, BuildConfig.FLAVOR);
        return this.mCurrentPhoto;
    }

    public void setCurrentPhoto(String mCurrentPhoto) {
        this.mCurrentPhoto = mCurrentPhoto;
        this.mPrefs.edit().putString(PHOTO, this.mCurrentPhoto).commit();
    }

    public boolean isYayf() {
        this.mYayf = this.mPrefs.getBoolean(YAYF_MEDIA, false);
        return this.mYayf;
    }

    public void setYayf(boolean mYayf) {
        this.mYayf = mYayf;
        this.mPrefs.edit().putBoolean(YAYF_MEDIA, this.mYayf).commit();
    }

    public boolean isManifest() {
        this.mManifest = this.mPrefs.getBoolean(MANIFESTO_MEDIA, false);
        return this.mManifest;
    }

    public void setManifest(boolean mManifest) {
        this.mManifest = mManifest;
        this.mPrefs.edit().putBoolean(MANIFESTO_MEDIA, this.mManifest).commit();
    }

    public String getToken() {
        this.mToken = this.mPrefs.getString(DEVICE_TOKEN, BuildConfig.FLAVOR);
        return this.mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
        this.mPrefs.edit().putString(DEVICE_TOKEN, this.mToken).commit();
    }

    public static boolean appInstalled(Activity activity, String uri) {
        try {
            activity.getPackageManager().getPackageInfo(uri, 1);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
