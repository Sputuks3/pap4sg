package sg.org.pap.pickle.ui.fragments;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import sg.org.pap.pickle.R;

public class CandidateFragment$$ViewBinder<T extends CandidateFragment> implements ViewBinder<T> {
    public void bind(Finder finder, T target, Object source) {
        target.mListView = (UltimateRecyclerView) finder.castView((View) finder.findRequiredView(source, R.id.list, "field 'mListView'"), R.id.list, "field 'mListView'");
        target.mLoading = (ProgressBar) finder.castView((View) finder.findRequiredView(source, R.id.pb_loading, "field 'mLoading'"), R.id.pb_loading, "field 'mLoading'");
        target.mChoices = (LinearLayout) finder.castView((View) finder.findRequiredView(source, R.id.choices, "field 'mChoices'"), R.id.choices, "field 'mChoices'");
        target.mMyReps = (Button) finder.castView((View) finder.findRequiredView(source, R.id.btn_my_reps, "field 'mMyReps'"), R.id.btn_my_reps, "field 'mMyReps'");
        target.mAll = (Button) finder.castView((View) finder.findRequiredView(source, R.id.btn_all, "field 'mAll'"), R.id.btn_all, "field 'mAll'");
    }

    public void unbind(T target) {
        target.mListView = null;
        target.mLoading = null;
        target.mChoices = null;
        target.mMyReps = null;
        target.mAll = null;
    }
}
