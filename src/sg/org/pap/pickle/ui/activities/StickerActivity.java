package sg.org.pap.pickle.ui.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.desmond.squarecamera.CameraActivity;
import com.desmond.squarecamera.ImageUtility;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import it.moondroid.coverflow.BuildConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.images.PhotoSortrView;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.pickle.CapturePhotoUtils;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class StickerActivity extends BaseActivity implements OnClickListener {
    private static final int[] IMAGES = new int[]{R.drawable.pap_stickers_01, R.drawable.pap_stickers_02, R.drawable.pap_stickers_03, R.drawable.pap_stickers_04, R.drawable.pap_stickers_05, R.drawable.pap_stickers_06, R.drawable.pap_stickers_07, R.drawable.pap_stickers_08, R.drawable.pap_stickers_09, R.drawable.pap_stickers_10, R.drawable.pap_stickers_11, R.drawable.pap_stickers_12, R.drawable.pap_stickers_13, R.drawable.pap_stickers_14, R.drawable.pap_stickers_15, R.drawable.pap_stickers_16, R.drawable.pap_stickers_17, R.drawable.pap_stickers_18, R.drawable.pap_stickers_19, R.drawable.pap_stickers_20, R.drawable.pap_stickers_21, R.drawable.pap_stickers_22, R.drawable.pap_stickers_23, R.drawable.pap_stickers_24};
    static final int REQUEST_IMAGE_CAPTURE = 2;
    private int REQUEST_CAMERA = 3;
    private int SHARE_CODE = 1;
    @Bind({2131624135})
    RelativeLayout layout;
    @Bind({2131624137})
    LinearLayout mActionsLayout;
    String mCurrentPhotoFileName;
    @Bind({2131624138})
    UltimateRecyclerView mGridSticker;
    private LayoutManager mLayoutManager;
    @Bind({2131624136})
    ImageView mPreview;
    @Bind({2131624139})
    Button mSave;
    @Bind({2131624140})
    Button mShare;
    Point mSize;
    Bitmap mStickerImage;
    String mTitle;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;
    private Menu menu;
    PhotoSortrView photoSorter;
    Uri photoUri;

    class GridAdapter extends UltimateViewAdapter<ViewHolder> {

        public class ViewHolder extends UltimateRecyclerviewViewHolder {
            ImageView imgSticker;

            public ViewHolder(View itemView) {
                super(itemView);
                this.imgSticker = (ImageView) itemView.findViewById(R.id.grid_item_image);
            }

            public void onItemSelected() {
                this.itemView.setBackgroundColor(-12303292);
            }

            public void onItemClear() {
                this.itemView.setBackgroundColor(0);
            }
        }

        GridAdapter() {
        }

        public ViewHolder getViewHolder(View view) {
            return new ViewHolder(view);
        }

        public ViewHolder onCreateViewHolder(ViewGroup viewGroup) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cell_sticker, viewGroup, false));
        }

        public int getAdapterItemCount() {
            return StickerActivity.IMAGES.length;
        }

        public long generateHeaderId(int i) {
            return (long) getItem(i);
        }

        public void onBindViewHolder(ViewHolder holder, final int position) {
            if (position < getItemCount()) {
                if (this.customHeaderView != null) {
                    if (position > StickerActivity.IMAGES.length) {
                        return;
                    }
                } else if (position >= StickerActivity.IMAGES.length) {
                    return;
                }
                if (this.customHeaderView == null || position > 0) {
                    holder.imgSticker.setImageBitmap(StickerActivity.decodeSampledBitmapFromResource(StickerActivity.this.getResources(), StickerActivity.IMAGES[position], 100, 100));
                    holder.imgSticker.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            StickerActivity.this.photoSorter.addImage(StickerActivity.IMAGES[position]);
                        }
                    });
                }
            }
        }

        public android.support.v7.widget.RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            return null;
        }

        public void onBindHeaderViewHolder(android.support.v7.widget.RecyclerView.ViewHolder viewHolder, int i) {
        }

        public int getItem(int position) {
            return StickerActivity.IMAGES[position];
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        restoreActionBar();
        startActivityForResult(new Intent(this, CameraActivity.class), this.REQUEST_CAMERA);
        this.mShare.setOnClickListener(this);
        this.mSave.setOnClickListener(this);
        this.mGridSticker.setHasFixedSize(true);
        this.mLayoutManager = new GridLayoutManager(this, 4);
        this.mGridSticker.setLayoutManager(this.mLayoutManager);
        Display display = getWindowManager().getDefaultDisplay();
        this.mSize = new Point();
        display.getSize(this.mSize);
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.squarecamera__app_name));
        if (mediaStorageDir.exists() || !mediaStorageDir.mkdirs()) {
            writeNoMediaFile(mediaStorageDir.getPath());
        } else {
            writeNoMediaFile(mediaStorageDir.getPath());
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        this.mTitle = "Stickers";
        this.mTitleView.setText(this.mTitle);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    protected void onResume() {
        super.onResume();
        sendScreenAnalytics("/stickers");
        if (this.photoSorter != null) {
            this.photoSorter.loadImages(this);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.photoSorter != null) {
            this.photoSorter.unloadImages();
        }
        if (this.mStickerImage != null) {
            this.mStickerImage = null;
        }
        if (this.photoUri != null) {
            try {
                deleteUri(this.photoUri);
            } catch (IOException e) {
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 23) {
            return super.onKeyDown(keyCode, event);
        }
        this.photoSorter.trackballClicked();
        return true;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save /*2131624139*/:
                if (this.mStickerImage == null) {
                    Toast.makeText(this, "Photo not available", 1).show();
                    return;
                } else if (CapturePhotoUtils.insertImage(getContentResolver(), this.mStickerImage, Long.toString(new Date().getTime()), BuildConfig.FLAVOR) != null) {
                    Toast.makeText(this, "Photo saved to gallery", 1).show();
                    return;
                } else {
                    PickleApp.showDialog(this, "Sorry!", "Failed to save photo to gallery", "Ok");
                    return;
                }
            case R.id.btn_share /*2131624140*/:
                Intent sendIntent = new Intent("android.intent.action.SEND");
                sendIntent.setType("image/jpeg");
                try {
                    sendIntent.putExtra("android.intent.extra.STREAM", getBitmapUri(this.mStickerImage));
                    startActivityForResult(Intent.createChooser(sendIntent, "share"), this.SHARE_CODE);
                    return;
                } catch (IOException e) {
                    return;
                }
            default:
                return;
        }
    }

    protected Bitmap convertToBitmap(View layout) {
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        return layout.getDrawingCache();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_sticker, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                break;
            case R.id.action_done /*2131624244*/:
                if (this.mGridSticker != null) {
                    this.mGridSticker.setVisibility(8);
                }
                if (this.photoSorter != null) {
                    this.photoSorter.setVisibility(8);
                }
                this.mPreview.setVisibility(0);
                this.mStickerImage = convertToBitmap(this.photoSorter);
                this.mPreview.setImageBitmap(this.mStickerImage);
                this.mActionsLayout.setVisibility(0);
                item.setVisible(false);
                break;
            case R.id.action_clear /*2131624245*/:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Uri getBitmapUri(Bitmap bitmap) throws IOException {
        File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Calendar.getInstance().getTimeInMillis() + ".png");
        FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
        bitmap.compress(CompressFormat.PNG, 80, fileOutPutStream);
        fileOutPutStream.flush();
        fileOutPutStream.close();
        return Uri.parse("file://" + imageFile.getAbsolutePath());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_IMAGE_CAPTURE || resultCode != -1) {
            if (requestCode == this.REQUEST_CAMERA && resultCode == -1) {
                this.photoUri = data.getData();
                Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(this.photoUri.getPath(), this.mSize.x, this.mSize.x);
                if (bitmap != null) {
                    this.photoSorter = new PhotoSortrView(this);
                    this.photoSorter.changePhoto(bitmap);
                    this.mGridSticker.setAdapter(new GridAdapter());
                    LayoutParams p = new LayoutParams(-1, -2);
                    p.addRule(REQUEST_IMAGE_CAPTURE, R.id.grid_sticker);
                    p.addRule(3, R.id.toolbar);
                    this.photoSorter.setLayoutParams(p);
                    this.layout.addView(this.photoSorter);
                    return;
                }
                PickleApp.showDialog(this, "Sorry!", "Failed to load photo from Camera", "Ok");
                onBackPressed();
            } else if (resultCode == 0) {
                finish();
            }
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        PickleApp.getInstance().setCurrentPhoto(null);
    }

    public static int calculateInSampleSize(Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        if (height <= reqHeight && width <= reqWidth) {
            return 1;
        }
        int heightRatio = Math.round(((float) height) / ((float) reqHeight));
        int widthRatio = Math.round(((float) width) / ((float) reqWidth));
        if (heightRatio < widthRatio) {
            return heightRatio;
        }
        return widthRatio;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_uri", this.mCurrentPhotoFileName);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mCurrentPhotoFileName = savedInstanceState.getString("file_uri");
    }

    public int getWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpHeight = ((float) outMetrics.heightPixels) / density;
        return (int) (((float) outMetrics.widthPixels) / density);
    }

    public void deleteUri(Uri mUri) throws IOException {
        String mPath = mUri.getPath();
        String newStrPath = BuildConfig.FLAVOR;
        if (mPath != null && mPath.length() > 0) {
            int endIndex = mPath.lastIndexOf("/");
            if (endIndex != -1) {
                newStrPath = mPath.substring(0, endIndex);
            }
        }
        writeNoMediaFile(newStrPath);
        deleteDirectory(new File(newStrPath));
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                    Intent mediaScannerIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
                    mediaScannerIntent.setData(Uri.fromFile(files[i]));
                    sendBroadcast(mediaScannerIntent);
                }
            }
        }
        return path.delete();
    }

    public boolean writeNoMediaFile(String directoryPath) {
        if (!"mounted".equals(Environment.getExternalStorageState())) {
            return false;
        }
        try {
            File noMedia = new File(directoryPath, ".nomedia");
            if (noMedia.exists()) {
                return true;
            }
            FileOutputStream noMediaOutStream = new FileOutputStream(noMedia);
            noMediaOutStream.write(0);
            noMediaOutStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
