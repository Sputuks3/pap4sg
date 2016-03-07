package sg.org.pap.pickle.ui.pickle;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import com.facebook.share.internal.ShareConstants;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class CapturePhotoUtils {
    public static final String insertImage(ContentResolver cr, Bitmap source, String title, String description) {
        OutputStream imageOut;
        ContentValues values = new ContentValues();
        values.put(ShareConstants.WEB_DIALOG_PARAM_TITLE, title);
        values.put("_display_name", title);
        values.put(ShareConstants.WEB_DIALOG_PARAM_DESCRIPTION, description);
        values.put("mime_type", "image/jpeg");
        values.put("date_added", Long.valueOf(System.currentTimeMillis()));
        values.put("datetaken", Long.valueOf(System.currentTimeMillis()));
        Uri url = null;
        try {
            url = cr.insert(Media.EXTERNAL_CONTENT_URI, values);
            if (source != null) {
                imageOut = cr.openOutputStream(url);
                source.compress(CompressFormat.JPEG, 50, imageOut);
                imageOut.close();
                long id = ContentUris.parseId(url);
                storeThumbnail(cr, Thumbnails.getThumbnail(cr, id, 1, null), id, 50.0f, 50.0f, 3);
                if (url == null) {
                    return url.toString();
                }
                return null;
            }
            cr.delete(url, null, null);
            url = null;
            if (url == null) {
                return null;
            }
            return url.toString();
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Throwable th) {
            imageOut.close();
        }
    }

    private static final Bitmap storeThumbnail(ContentResolver cr, Bitmap source, long id, float width, float height, int kind) {
        Matrix matrix = new Matrix();
        matrix.setScale(width / ((float) source.getWidth()), height / ((float) source.getHeight()));
        Bitmap thumb = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        ContentValues values = new ContentValues(4);
        values.put("kind", Integer.valueOf(kind));
        values.put("image_id", Integer.valueOf((int) id));
        values.put("height", Integer.valueOf(thumb.getHeight()));
        values.put("width", Integer.valueOf(thumb.getWidth()));
        try {
            OutputStream thumbOut = cr.openOutputStream(cr.insert(Thumbnails.EXTERNAL_CONTENT_URI, values));
            thumb.compress(CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e2) {
            return null;
        }
    }
}
