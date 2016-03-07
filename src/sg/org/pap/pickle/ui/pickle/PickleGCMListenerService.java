package sg.org.pap.pickle.ui.pickle;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import com.facebook.share.internal.ShareConstants;
import com.google.android.gms.gcm.GcmListenerService;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.ui.activities.HomeActivity;

public class PickleGCMListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";

    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(ShareConstants.WEB_DIALOG_PARAM_MESSAGE);
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        sendNotification(message);
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(67108864);
        ((NotificationManager) getSystemService("notification")).notify(0, new Builder(this).setSmallIcon(R.drawable.ic_launcher).setContentTitle("PAP4SG").setContentText(message).setAutoCancel(true).setSound(RingtoneManager.getDefaultUri(2)).setContentIntent(PendingIntent.getActivity(this, 0, intent, 1073741824)).build());
    }
}
