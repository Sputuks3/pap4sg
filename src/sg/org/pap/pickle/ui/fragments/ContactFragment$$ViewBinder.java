package sg.org.pap.pickle.ui.fragments;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;
import sg.org.pap.pickle.R;

public class ContactFragment$$ViewBinder<T extends ContactFragment> implements ViewBinder<T> {
    public void bind(Finder finder, T target, Object source) {
        target.mFacebook = (ImageButton) finder.castView((View) finder.findRequiredView(source, R.id.btn_fb, "field 'mFacebook'"), R.id.btn_fb, "field 'mFacebook'");
        target.mInsta = (ImageButton) finder.castView((View) finder.findRequiredView(source, R.id.btn_ig, "field 'mInsta'"), R.id.btn_ig, "field 'mInsta'");
        target.mTwitter = (ImageButton) finder.castView((View) finder.findRequiredView(source, R.id.btn_tw, "field 'mTwitter'"), R.id.btn_tw, "field 'mTwitter'");
        target.mPinterest = (ImageButton) finder.castView((View) finder.findRequiredView(source, R.id.btn_pin, "field 'mPinterest'"), R.id.btn_pin, "field 'mPinterest'");
        target.mEmail = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_email, "field 'mEmail'"), R.id.tv_email, "field 'mEmail'");
        target.mWebsite = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_website, "field 'mWebsite'"), R.id.tv_website, "field 'mWebsite'");
        target.mSubscribe = (Button) finder.castView((View) finder.findRequiredView(source, R.id.btn_subscribe, "field 'mSubscribe'"), R.id.btn_subscribe, "field 'mSubscribe'");
        target.mContentLayout = (RelativeLayout) finder.castView((View) finder.findRequiredView(source, R.id.content_layout, "field 'mContentLayout'"), R.id.content_layout, "field 'mContentLayout'");
        target.mLoading = (ProgressBar) finder.castView((View) finder.findRequiredView(source, R.id.pb_loading, "field 'mLoading'"), R.id.pb_loading, "field 'mLoading'");
        target.mRelativeEmail = (RelativeLayout) finder.castView((View) finder.findRequiredView(source, R.id.rl_email, "field 'mRelativeEmail'"), R.id.rl_email, "field 'mRelativeEmail'");
        target.mRelativeWeb = (RelativeLayout) finder.castView((View) finder.findRequiredView(source, R.id.rl_web, "field 'mRelativeWeb'"), R.id.rl_web, "field 'mRelativeWeb'");
    }

    public void unbind(T target) {
        target.mFacebook = null;
        target.mInsta = null;
        target.mTwitter = null;
        target.mPinterest = null;
        target.mEmail = null;
        target.mWebsite = null;
        target.mSubscribe = null;
        target.mContentLayout = null;
        target.mLoading = null;
        target.mRelativeEmail = null;
        target.mRelativeWeb = null;
    }
}
