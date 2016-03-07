package sg.org.pap.pickle.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.viewpagerindicator.CirclePageIndicator;
import it.moondroid.coverflow.BuildConfig;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.api.data.HomeResponse;
import sg.org.pap.pickle.api.twitter.TwitterConst;
import sg.org.pap.pickle.models.News;
import sg.org.pap.pickle.models.Representative;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.activities.CandidateDetail;
import sg.org.pap.pickle.ui.activities.NewsDetailActivity;
import sg.org.pap.pickle.ui.activities.WebplayerActivity;
import sg.org.pap.pickle.ui.activities.WebviewActivity;
import sg.org.pap.pickle.ui.activities.YoutubeNewsDetailActivity;
import sg.org.pap.pickle.ui.adapters.NewsAdapter;
import sg.org.pap.pickle.ui.adapters.TwitAdapter;
import sg.org.pap.pickle.ui.base.BaseFragment;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.pickle.PickleApp;
import sg.org.pap.pickle.ui.widgets.CustomSwipeToRefresh;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public class NewsFragment extends BaseFragment implements OnRefreshListener, OnClickListener {
    Runnable Update = new Runnable() {
        public void run() {
            int size = NewsFragment.this.mContent.getRepresentatives().size() - 1;
            if (!TextUtils.isEmpty(NewsFragment.this.mContent.getConstituencyPhoto())) {
                size = NewsFragment.this.mContent.getRepresentatives().size();
            }
            if (NewsFragment.this.page > size) {
                NewsFragment.this.page = 0;
            }
            ViewPager viewPager = NewsFragment.this.mPager;
            NewsFragment newsFragment = NewsFragment.this;
            int i = newsFragment.page;
            newsFragment.page = i + 1;
            viewPager.setCurrentItem(i, true);
        }
    };
    private ConfigurationBuilder builder;
    Handler handler = new Handler();
    private NewsAdapter mAdapter;
    @Bind({2131624153})
    Button mBtnNews;
    @Bind({2131624154})
    Button mBtnTweet;
    private HomeResponse mContent;
    @Bind({2131624189})
    CirclePageIndicator mIndicator;
    @Bind({2131624151})
    LinearLayout mLChoices;
    private LayoutManager mLayoutManager;
    private LayoutManager mLayoutTwitter;
    @Bind({2131624191})
    ListView mListView;
    @Bind({2131624152})
    ViewPager mPager;
    @Bind({2131624071})
    ProgressBar mPbLoading;
    @Bind({2131624190})
    CustomSwipeToRefresh mRefresh;
    private TwitAdapter mTAdapter;
    private List<Status> mTweets;
    private User mUser;
    int page = 1;
    private View stickyViewSpacer;
    Timer swipeTimer;
    Timer timer;

    public class AskOAuth extends AsyncTask<Void, Void, List<Status>> {
        protected void onPreExecute() {
            super.onPreExecute();
            NewsFragment.this.mRefresh.setRefreshing(true);
        }

        protected List<Status> doInBackground(Void... voids) {
            try {
                NewsFragment.this.builder = new ConfigurationBuilder();
                NewsFragment.this.builder.setApplicationOnlyAuthEnabled(true);
                NewsFragment.this.builder.setOAuthConsumerKey(TwitterConst.CONSUMER_KEY).setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET);
                OAuth2Token token = new TwitterFactory(NewsFragment.this.builder.build()).getInstance().getOAuth2Token();
                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setApplicationOnlyAuthEnabled(true);
                Twitter twitter = new TwitterFactory(cb.build()).getInstance();
                twitter.setOAuthConsumer(TwitterConst.CONSUMER_KEY, TwitterConst.CONSUMER_SECRET);
                twitter.setOAuth2Token(token);
                for (twitter4j.User user : twitter.lookupUsers("Papsingapore")) {
                    if (user.getStatus() != null) {
                        return twitter.getUserTimeline(user.getScreenName());
                    }
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(List<Status> result) {
            super.onPostExecute(result);
            NewsFragment.this.mPbLoading.setVisibility(8);
            NewsFragment.this.mRefresh.setRefreshing(false);
            if (result != null) {
                NewsFragment.this.mTweets = result;
                NewsFragment.this.mTAdapter = new TwitAdapter(NewsFragment.this.getActivity(), result);
                NewsFragment.this.mListView.setAdapter(NewsFragment.this.mTAdapter);
                return;
            }
            Snackbar.make(NewsFragment.this.getView(), "Unable to provide access for Twitter", 0).show();
        }
    }

    class CustomPagerAdapter extends PagerAdapter {
        ImageView mConstImage;
        Context mContext;
        List<Representative> mData;
        ImageView mImage;
        LayoutInflater mLayoutInflater = ((LayoutInflater) this.mContext.getSystemService("layout_inflater"));
        TextView mName;
        TextView mQuote;
        TextView mTown;

        public CustomPagerAdapter(Context context, List<Representative> myReps) {
            this.mContext = context;
            this.mData = new ArrayList(myReps);
        }

        public int getCount() {
            if (TextUtils.isEmpty(NewsFragment.this.mContent.getConstituencyPhoto())) {
                return this.mData.size();
            }
            return this.mData.size() + 1;
        }

        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = this.mLayoutInflater.inflate(R.layout.pager_profile, container, false);
            container.addView(itemView);
            this.mImage = (ImageView) itemView.findViewById(R.id.profile_image);
            this.mName = (TextView) itemView.findViewById(R.id.tv_name);
            this.mTown = (TextView) itemView.findViewById(R.id.tv_town);
            this.mQuote = (TextView) itemView.findViewById(R.id.tv_quote);
            this.mConstImage = (ImageView) itemView.findViewById(R.id.img_photo_const);
            if (position != 0 || TextUtils.isEmpty(NewsFragment.this.mContent.getConstituencyPhoto())) {
                this.mImage.setVisibility(0);
                this.mName.setVisibility(0);
                this.mTown.setVisibility(0);
                this.mQuote.setVisibility(0);
                this.mConstImage.setVisibility(8);
                int index = position;
                if (!TextUtils.isEmpty(NewsFragment.this.mContent.getConstituencyPhoto())) {
                    index = position - 1;
                }
                final Representative mRepresentative = (Representative) this.mData.get(index);
                if (mRepresentative != null) {
                    Glide.with(NewsFragment.this.getActivity()).load(mRepresentative.getPhoto2()).crossFade().fitCenter().into(this.mImage);
                    this.mName.setText(mRepresentative.getName());
                    this.mTown.setText(mRepresentative.getDesignation());
                    if (TextUtils.isEmpty(mRepresentative.getDescription())) {
                        this.mQuote.setText("\n\n\n\n");
                    } else {
                        String desc = mRepresentative.getDescription();
                        this.mQuote.setText(mRepresentative.getDescription());
                        for (int j = this.mQuote.getLineCount(); j < 4; j++) {
                            desc = desc + "\n";
                        }
                        this.mQuote.setText(desc);
                    }
                } else {
                    this.mImage.setVisibility(8);
                    this.mName.setText(BuildConfig.FLAVOR);
                    this.mTown.setText(BuildConfig.FLAVOR);
                    this.mQuote.setText("\n\n\n");
                }
                itemView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(NewsFragment.this.getActivity(), CandidateDetail.class);
                        intent.putExtra(YoutubeNewsDetailActivity.ARG_CONTENT, mRepresentative);
                        NewsFragment.this.startAnimatedActivity(intent);
                    }
                });
            } else {
                this.mImage.setVisibility(8);
                this.mName.setVisibility(8);
                this.mTown.setVisibility(8);
                this.mQuote.setVisibility(8);
                this.mConstImage.setVisibility(0);
                Glide.with(NewsFragment.this.getActivity()).load(NewsFragment.this.mContent.getConstituencyPhoto()).crossFade().fitCenter().into(this.mConstImage);
            }
            return itemView;
        }

        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((FrameLayout) object);
        }
    }

    private class HomeTask extends AsyncTask<String, Void, HomeResponse> {
        private HomeTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            NewsFragment.this.mPbLoading.setVisibility(0);
            NewsFragment.this.mRefresh.setRefreshing(true);
        }

        protected HomeResponse doInBackground(String... strings) {
            try {
                String postal = strings[0];
                if (TextUtils.isEmpty(postal)) {
                    return RestClient.getInstance().getNews("750319");
                }
                return RestClient.getInstance().getNews(postal);
            } catch (Exception e) {
                return null;
            }
        }

        protected void onPostExecute(HomeResponse homeResponse) {
            super.onPostExecute(homeResponse);
            NewsFragment.this.mPbLoading.setVisibility(8);
            NewsFragment.this.mContent = homeResponse;
            if (NewsFragment.this.mContent != null) {
                PickleApp.getInstance().setmHome(NewsFragment.this.mContent);
                NewsFragment.this.mAdapter = new NewsAdapter(NewsFragment.this.getActivity(), NewsFragment.this.mContent.getNews());
                if (NewsFragment.this.mPager.getVisibility() == 0) {
                    NewsFragment.this.mPager.setAdapter(new CustomPagerAdapter(NewsFragment.this.getActivity(), NewsFragment.this.mContent.getRepresentatives()));
                    NewsFragment.this.pageSwitcher(5);
                    NewsFragment.this.mIndicator.setFillColor(-16776961);
                    NewsFragment.this.mIndicator.setPageColor(-65536);
                    NewsFragment.this.mIndicator.setStrokeColor(0);
                    NewsFragment.this.mIndicator.setViewPager(NewsFragment.this.mPager);
                }
                NewsFragment.this.mListView.setAdapter(NewsFragment.this.mAdapter);
                NewsFragment.this.mListView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent;
                        if (NewsFragment.this.mBtnNews.isSelected()) {
                            String video = ((News) NewsFragment.this.mContent.getNews().get(i)).getVideo();
                            if (TextUtils.isEmpty(video) || !video.contains("youtube")) {
                                intent = new Intent(NewsFragment.this.getActivity(), NewsDetailActivity.class);
                                intent.putExtra(YoutubeNewsDetailActivity.ARG_CONTENT, (Serializable) NewsFragment.this.mContent.getNews().get(i));
                                NewsFragment.this.startAnimatedActivity(intent);
                                return;
                            }
                            intent = new Intent(NewsFragment.this.getActivity(), YoutubeNewsDetailActivity.class);
                            intent.putExtra(YoutubeNewsDetailActivity.ARG_CONTENT, (Serializable) NewsFragment.this.mContent.getNews().get(i));
                            NewsFragment.this.startAnimatedActivity(intent);
                            return;
                        }
                        try {
                            NewsFragment.this.startAnimatedActivity(new Intent("android.intent.action.VIEW", Uri.parse(String.format("twitter://status?user_id=%s&status_id=%s", new Object[]{((Status) NewsFragment.this.mTweets.get(i)).getUser().getScreenName(), Long.toString(((Status) NewsFragment.this.mTweets.get(i)).getId())}))));
                        } catch (Exception e) {
                            intent = new Intent(NewsFragment.this.getActivity(), WebplayerActivity.class);
                            intent.putExtra(WebviewActivity.CONTENT, "https://twitter.com/" + userName + "/status/" + statusId);
                            NewsFragment.this.startAnimatedActivity(intent);
                        }
                    }
                });
                NewsFragment.this.mPbLoading.setVisibility(8);
                NewsFragment.this.mRefresh.setRefreshing(false);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUser = PickleApp.getInstance().getUser();
        this.mContent = PickleApp.getInstance().getmHome();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendScreenAnalytics("/news");
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        this.mBtnNews.setOnClickListener(this);
        this.mBtnTweet.setOnClickListener(this);
        this.mRefresh.setOnRefreshListener(this);
        this.mRefresh.setDistanceToTriggerSync(-2);
        if (this.mUser == null) {
            this.mPager.setVisibility(8);
            this.mIndicator.setVisibility(8);
        } else if (TextUtils.isEmpty(this.mUser.getPostalCode())) {
            this.mPager.setVisibility(8);
            this.mIndicator.setVisibility(8);
        } else {
            this.mPager.setVisibility(0);
            this.mIndicator.setVisibility(0);
        }
        if (!Connection.connected(getActivity())) {
            Connection.showSnackBar(getActivity(), getView());
        } else if (this.mUser != null) {
            this.mBtnNews.setSelected(true);
            if (TextUtils.isEmpty(this.mUser.getPostalCode())) {
                new HomeTask().execute(new String[]{BuildConfig.FLAVOR});
            } else {
                new HomeTask().execute(new String[]{this.mUser.getPostalCode()});
            }
        } else {
            this.mBtnNews.setSelected(true);
            new HomeTask().execute(new String[]{BuildConfig.FLAVOR});
        }
        Float mDistanceToTriggerSync = Float.valueOf(0.001f);
        try {
            Field field = SwipeRefreshLayout.class.getDeclaredField("mDistanceToTriggerSync");
            field.setAccessible(true);
            field.setFloat(this.mRefresh, mDistanceToTriggerSync.floatValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        this.mBtnNews.setSelected(false);
        this.mBtnTweet.setSelected(false);
        view.setSelected(true);
        this.mListView.setAdapter(null);
        this.mPbLoading.setVisibility(0);
        switch (view.getId()) {
            case R.id.btn_news /*2131624153*/:
                if (this.mAdapter == null) {
                    new HomeTask().execute(new String[0]);
                    return;
                }
                this.mPbLoading.setVisibility(8);
                this.mListView.setAdapter(this.mAdapter);
                return;
            case R.id.btn_tweet /*2131624154*/:
                new AskOAuth().execute(new Void[0]);
                return;
            default:
                return;
        }
    }

    public void onRefresh() {
        if (!Connection.connected(getActivity())) {
            Connection.showSnackBar(getActivity(), getView());
        } else if (this.mBtnNews.isSelected()) {
            if (this.mUser != null) {
                this.mBtnNews.setSelected(true);
                if (TextUtils.isEmpty(this.mUser.getPostalCode())) {
                    new HomeTask().execute(new String[]{BuildConfig.FLAVOR});
                    return;
                }
                new HomeTask().execute(new String[]{this.mUser.getPostalCode()});
                return;
            }
            this.mBtnNews.setSelected(true);
            new HomeTask().execute(new String[]{BuildConfig.FLAVOR});
        } else if (this.mBtnTweet.isSelected()) {
            new AskOAuth().execute(new Void[0]);
        }
    }

    public void pageSwitcher(int seconds) {
        this.swipeTimer = new Timer();
        this.swipeTimer.schedule(new TimerTask() {
            public void run() {
                NewsFragment.this.handler.post(NewsFragment.this.Update);
            }
        }, (long) (seconds * 1000), (long) (seconds * 2000));
    }
}
