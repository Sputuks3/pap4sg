package sg.org.pap.pickle.ui.pickle;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import com.marshalchen.ultimaterecyclerview.itemTouchHelper.SimpleItemTouchHelperCallback;

public class BitmapUtils {
    public static Bitmap createReflectedBitmap(Bitmap srcBitmap, float reflectHeight) {
        if (srcBitmap == null) {
            return null;
        }
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int reflectionWidth = srcBitmap.getWidth();
        int reflectionHeight = reflectHeight == 0.0f ? srcHeight / 3 : (int) (((float) srcHeight) * reflectHeight);
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.preScale(SimpleItemTouchHelperCallback.ALPHA_FULL, -1.0f);
        try {
            Bitmap reflectionBitmap = Bitmap.createBitmap(srcBitmap, 0, srcHeight - reflectionHeight, reflectionWidth, reflectionHeight, matrix, false);
            if (reflectionBitmap == null) {
                return null;
            }
            Canvas canvas = new Canvas(reflectionBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) reflectionBitmap.getHeight(), 1895825407, 16777215, TileMode.MIRROR));
            paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            canvas.drawRect(0.0f, 0.0f, (float) reflectionBitmap.getWidth(), (float) reflectionBitmap.getHeight(), paint);
            return reflectionBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
