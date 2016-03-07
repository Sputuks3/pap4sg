package sg.org.pap.pickle.ui.fragments;

import android.view.View;
import android.widget.ProgressBar;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import sg.org.pap.pickle.R;

public class ManifestoFragment$$ViewBinder<T extends ManifestoFragment> implements ViewBinder<T> {
    public void bind(Finder finder, T target, Object source) {
        target.mListView = (UltimateRecyclerView) finder.castView((View) finder.findRequiredView(source, R.id.list, "field 'mListView'"), R.id.list, "field 'mListView'");
        target.mLoading = (ProgressBar) finder.castView((View) finder.findRequiredView(source, R.id.loading, "field 'mLoading'"), R.id.loading, "field 'mLoading'");
    }

    public void unbind(T target) {
        target.mListView = null;
        target.mLoading = null;
    }
}
