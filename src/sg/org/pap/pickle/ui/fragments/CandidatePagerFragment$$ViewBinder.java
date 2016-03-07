package sg.org.pap.pickle.ui.fragments;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;
import sg.org.pap.pickle.R;

public class CandidatePagerFragment$$ViewBinder<T extends CandidatePagerFragment> implements ViewBinder<T> {
    public void bind(Finder finder, T target, Object source) {
        target.mName = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_name, "field 'mName'"), R.id.tv_name, "field 'mName'");
        target.mTown = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_town, "field 'mTown'"), R.id.tv_town, "field 'mTown'");
        target.mQuote = (TextView) finder.castView((View) finder.findRequiredView(source, R.id.tv_quote, "field 'mQuote'"), R.id.tv_quote, "field 'mQuote'");
        target.mImage = (ImageView) finder.castView((View) finder.findRequiredView(source, R.id.profile_image, "field 'mImage'"), R.id.profile_image, "field 'mImage'");
    }

    public void unbind(T target) {
        target.mName = null;
        target.mTown = null;
        target.mQuote = null;
        target.mImage = null;
    }
}
