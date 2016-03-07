package sg.org.pap.pickle.ui.fragments;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import butterknife.ButterKnife.Finder;
import butterknife.ButterKnife.ViewBinder;
import com.viewpagerindicator.CirclePageIndicator;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.ui.widgets.CustomSwipeToRefresh;

public class NewsFragment$$ViewBinder<T extends NewsFragment> implements ViewBinder<T> {
    public void bind(Finder finder, T target, Object source) {
        target.mBtnNews = (Button) finder.castView((View) finder.findRequiredView(source, R.id.btn_news, "field 'mBtnNews'"), R.id.btn_news, "field 'mBtnNews'");
        target.mBtnTweet = (Button) finder.castView((View) finder.findRequiredView(source, R.id.btn_tweet, "field 'mBtnTweet'"), R.id.btn_tweet, "field 'mBtnTweet'");
        target.mLChoices = (LinearLayout) finder.castView((View) finder.findRequiredView(source, R.id.choices, "field 'mLChoices'"), R.id.choices, "field 'mLChoices'");
        target.mPager = (ViewPager) finder.castView((View) finder.findRequiredView(source, R.id.pager, "field 'mPager'"), R.id.pager, "field 'mPager'");
        target.mPbLoading = (ProgressBar) finder.castView((View) finder.findRequiredView(source, R.id.pb_loading, "field 'mPbLoading'"), R.id.pb_loading, "field 'mPbLoading'");
        target.mListView = (ListView) finder.castView((View) finder.findRequiredView(source, R.id.listView, "field 'mListView'"), R.id.listView, "field 'mListView'");
        target.mIndicator = (CirclePageIndicator) finder.castView((View) finder.findRequiredView(source, R.id.circles, "field 'mIndicator'"), R.id.circles, "field 'mIndicator'");
        target.mRefresh = (CustomSwipeToRefresh) finder.castView((View) finder.findRequiredView(source, R.id.refresh_list, "field 'mRefresh'"), R.id.refresh_list, "field 'mRefresh'");
    }

    public void unbind(T target) {
        target.mBtnNews = null;
        target.mBtnTweet = null;
        target.mLChoices = null;
        target.mPager = null;
        target.mPbLoading = null;
        target.mListView = null;
        target.mIndicator = null;
        target.mRefresh = null;
    }
}
