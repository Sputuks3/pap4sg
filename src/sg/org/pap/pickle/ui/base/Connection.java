package sg.org.pap.pickle.ui.base;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;
import sg.org.pap.pickle.R;

public class Connection {
    public static boolean connectedOrConnecting(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static boolean connected(Context context) {
        NetworkInfo activeNetwork = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static boolean connectionType(Context context, int connectionType) {
        return connectionType == ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo().getType();
    }

    public static void showNoConnectionDialog(Context ctx1) {
        final Context ctx = ctx1;
        Builder builder = new Builder(ctx);
        builder.setCancelable(true);
        builder.setMessage(R.string.no_connection);
        builder.setTitle(R.string.no_connection_title);
        builder.setPositiveButton(R.string.settings_button_text, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ctx.startActivity(new Intent("android.settings.SETTINGS"));
            }
        });
        builder.setNegativeButton(R.string.cancel_button_text, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });
        builder.show();
    }

    public static void showSnackBar(final Context ctx1, View view) {
        Snackbar snackbar = Snackbar.make(view, "No Internet Connection.", 0).setAction("Settings", new View.OnClickListener() {
            public void onClick(View view) {
                ctx1.startActivity(new Intent("android.settings.SETTINGS"));
            }
        });
        snackbar.setActionTextColor(-65536);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(-12303292);
        ((TextView) snackbarView.findViewById(R.id.snackbar_text)).setTextColor(-256);
        snackbar.show();
    }
}
