package sg.org.pap.pickle.ui.fragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;
import com.google.android.gms.maps.MapView;
import sg.org.pap.pickle.R;

public class TownFragment$$ViewBinder<T extends TownFragment> implements ViewBinder<T> {
    public void bind(Finder finder, T target, Object source) {
        target.mHotline = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_hotline, "field 'mHotline'"), R.id.tv_hotline, "field 'mHotline'");
        target.mEmail = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_email, "field 'mEmail'"), R.id.tv_email, "field 'mEmail'");
        target.mWebsite = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_website, "field 'mWebsite'"), R.id.tv_website, "field 'mWebsite'");
        target.mAddress = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_address, "field 'mAddress'"), R.id.tv_address, "field 'mAddress'");
        target.mHeader = (ImageView) finder.castView((View) finder.findRequiredView(source, R.id.img_header, "field 'mHeader'"), R.id.img_header, "field 'mHeader'");
        target.mLoading = (ProgressBar) finder.castView((View) finder.findRequiredView(source, R.id.pb_loading, "field 'mLoading'"), R.id.pb_loading, "field 'mLoading'");
        target.mMap = (MapView) finder.castView((View) finder.findRequiredView(source, R.id.map, "field 'mMap'"), R.id.map, "field 'mMap'");
        target.mContentLayout = (RelativeLayout) finder.castView((View) finder.findRequiredView(source, R.id.layout_content, "field 'mContentLayout'"), R.id.layout_content, "field 'mContentLayout'");
    }

    public void unbind(T target) {
        target.mHotline = null;
        target.mEmail = null;
        target.mWebsite = null;
        target.mAddress = null;
        target.mHeader = null;
        target.mLoading = null;
        target.mMap = null;
        target.mContentLayout = null;
    }
}
