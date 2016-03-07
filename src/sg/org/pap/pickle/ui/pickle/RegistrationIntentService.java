package sg.org.pap.pickle.ui.pickle;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.iid.InstanceID;
import java.io.IOException;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.User;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = new String[]{"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            synchronized (TAG) {
                String token = InstanceID.getInstance(this).getToken(getString(R.string.gcm_defaultSenderId), "GCM", null);
                Log.i(TAG, "GCM Registration Token: " + token);
                sendRegistrationToServer(token);
                subscribeTopics(token);
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    private void sendRegistrationToServer(String token) {
        PickleApp.getInstance().setToken(token);
        User mUser = PickleApp.getInstance().getUser();
        RestClient.getInstance().login(mUser.getId(), mUser.getPostalCode(), mUser.getName(), token);
    }

    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub.getInstance(this).subscribe(token, "/topics/" + topic, null);
        }
    }
}
