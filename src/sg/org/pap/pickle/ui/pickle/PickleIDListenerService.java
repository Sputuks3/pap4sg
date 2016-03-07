package sg.org.pap.pickle.ui.pickle;

import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;

public class PickleIDListenerService extends InstanceIDListenerService {
    private static final String TAG = "MyInstanceIDLS";

    public void onTokenRefresh() {
        startService(new Intent(this, RegistrationIntentService.class));
    }
}
