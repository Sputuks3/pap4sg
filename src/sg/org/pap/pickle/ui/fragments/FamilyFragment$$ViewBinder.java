package sg.org.pap.pickle.ui.fragments;

import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;
import sg.org.pap.pickle.R;

public class FamilyFragment$$ViewBinder<T extends FamilyFragment> implements ViewBinder<T> {
    public void bind(Finder finder, T target, Object source) {
        target.mVideoView = (WebView) finder.castView((View) finder.findRequiredView(source, R.id.video_view, "field 'mVideoView'"), R.id.video_view, "field 'mVideoView'");
        target.mLoading = (ProgressBar) finder.castView((View) finder.findRequiredView(source, R.id.pb_loading, "field 'mLoading'"), R.id.pb_loading, "field 'mLoading'");
    }

    public void unbind(T target) {
        target.mVideoView = null;
        target.mLoading = null;
    }
}
