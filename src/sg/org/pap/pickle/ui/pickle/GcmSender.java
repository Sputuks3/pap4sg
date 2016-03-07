package sg.org.pap.pickle.ui.pickle;

import com.facebook.share.internal.ShareConstants;
import it.moondroid.coverflow.BuildConfig;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class GcmSender {
    public static final String API_KEY = "AIzaSyAwcOWCxIyITCTM5bk3bim4J2fLMzpdB8o";

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2 || args[0] == null) {
            System.err.println("usage: ./gradlew run -Pargs=\"MESSAGE[,DEVICE_TOKEN]\"");
            System.err.println(BuildConfig.FLAVOR);
            System.err.println("Specify a test message to broadcast via GCM. If a device's GCM registration token is\nspecified, the message will only be sent to that device. Otherwise, the message \nwill be sent to all devices subscribed to the \"global\" topic.");
            System.err.println(BuildConfig.FLAVOR);
            System.err.println("Example (Broadcast):\nOn Windows:   .\\gradlew.bat run -Pargs=\"<Your_Message>\"\nOn Linux/Mac: ./gradlew run -Pargs=\"<Your_Message>\"");
            System.err.println(BuildConfig.FLAVOR);
            System.err.println("Example (Unicast):\nOn Windows:   .\\gradlew.bat run -Pargs=\"<Your_Message>,<Your_Token>\"\nOn Linux/Mac: ./gradlew run -Pargs=\"<Your_Message>,<Your_Token>\"");
            System.exit(1);
        }
        try {
            JSONObject jGcmData = new JSONObject();
            JSONObject jData = new JSONObject();
            jData.put(ShareConstants.WEB_DIALOG_PARAM_MESSAGE, args[0].trim());
            if (args.length <= 1 || args[1] == null) {
                jGcmData.put(ShareConstants.WEB_DIALOG_PARAM_TO, "/topics/global");
            } else {
                jGcmData.put(ShareConstants.WEB_DIALOG_PARAM_TO, args[1].trim());
            }
            jGcmData.put(ShareConstants.WEB_DIALOG_PARAM_DATA, jData);
            HttpURLConnection conn = (HttpURLConnection) new URL("https://android.googleapis.com/gcm/send").openConnection();
            conn.setRequestProperty("Authorization", "key=AIzaSyAwcOWCxIyITCTM5bk3bim4J2fLMzpdB8o");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.getOutputStream().write(jGcmData.toString().getBytes());
            System.out.println(IOUtils.toString(conn.getInputStream()));
            System.out.println("Check your device/emulator for notification or logcat for confirmation of the receipt of the GCM message.");
        } catch (IOException e) {
            System.out.println("Unable to send GCM message.");
            System.out.println("Please ensure that API_KEY has been replaced by the server API key, and that the device's registration token is correct (if specified).");
            e.printStackTrace();
        } catch (Exception e2) {
        }
    }
}
