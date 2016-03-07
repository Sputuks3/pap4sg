package sg.org.pap.pickle.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher.ViewFactory;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import it.moondroid.coverflow.BuildConfig;
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow.OnScrollPositionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.Representative;
import sg.org.pap.pickle.ui.base.BaseActivity;
import sg.org.pap.pickle.ui.base.BaseAdapter;

public class CandidateFlowActivity extends BaseActivity {
    CandidateAdapter mAdapter;
    FeatureCoverFlow mFlow;
    List<Representative> mReps;
    TextSwitcher mTitle;
    @Bind({2131624035})
    TextView mTitleView;
    @Bind({2131624070})
    Toolbar mToolbar;

    private class CandidateAdapter extends BaseAdapter {
        List<Representative> mData = new ArrayList(0);
        ViewHolder mHolder;

        class ViewHolder {
            ImageView mImgProfile;

            public ViewHolder(View v) {
                this.mImgProfile = (ImageView) v.findViewById(R.id.img_profile);
            }
        }

        protected CandidateAdapter(Activity context, List<Representative> data) {
            super(context);
            this.mData = new ArrayList(data);
        }

        public int getCount() {
            return this.mData.size();
        }

        public Object getItem(int position) {
            return this.mData.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = getInflater().inflate(R.layout.cell_candidate_flow, null);
                this.mHolder = new ViewHolder(rowView);
                rowView.setTag(this.mHolder);
            }
            Representative mReps = (Representative) this.mData.get(position);
            this.mHolder = (ViewHolder) rowView.getTag();
            if (!(mReps == null || TextUtils.isEmpty(mReps.getPhoto1()))) {
                Glide.with(CandidateFlowActivity.this).load(mReps.getPhoto1()).crossFade().into(this.mHolder.mImgProfile);
            }
            return rowView;
        }

        public void refresh(List<Representative> data) {
            this.mData = new ArrayList(data);
            notifyDataSetChanged();
        }
    }

    private class CandidateTask extends AsyncTask<Void, Void, List<Representative>> {
        private CandidateTask() {
        }

        protected List<Representative> doInBackground(Void... params) {
            List<Representative> mResponse = RestClient.getInstance().getRepresentatives(BuildConfig.FLAVOR);
            List<Representative> mFeatured = new ArrayList();
            for (Representative r : mResponse) {
                if (r.getIsFeatured().equals(Message.TYPE_PDPA)) {
                    mFeatured.add(r);
                }
            }
            return mFeatured;
        }

        protected void onPostExecute(final List<Representative> representatives) {
            super.onPostExecute(representatives);
            CandidateFlowActivity.this.mReps = new ArrayList(representatives);
            if (CandidateFlowActivity.this.mAdapter == null) {
                CandidateFlowActivity.this.mFlow.setAdapter(new CandidateAdapter(CandidateFlowActivity.this, CandidateFlowActivity.this.mReps));
            } else {
                CandidateFlowActivity.this.mAdapter.refresh(CandidateFlowActivity.this.mReps);
            }
            CandidateFlowActivity.this.mFlow.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Intent intent = new Intent(CandidateFlowActivity.this, CandidateDetail.class);
                    intent.putExtra(YoutubeNewsDetailActivity.ARG_CONTENT, (Serializable) representatives.get(position));
                    CandidateFlowActivity.this.startActivity(intent);
                }
            });
            CandidateFlowActivity.this.mFlow.setOnScrollPositionListener(new OnScrollPositionListener() {
                public void onScrolledToPosition(int position) {
                    CandidateFlowActivity.this.mTitle.setText(((Representative) representatives.get(position)).getName() + "\n" + ((Representative) representatives.get(position)).getConstituencyName());
                }

                public void onScrolling() {
                    CandidateFlowActivity.this.mTitle.setText(BuildConfig.FLAVOR);
                }
            });
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_flow);
        ButterKnife.bind(this);
        setSupportActionBar(this.mToolbar);
        restoreActionBar();
        this.mTitle = (TextSwitcher) findViewById(R.id.titleswitch);
        this.mTitle.setFactory(new ViewFactory() {
            public View makeView() {
                return (TextView) LayoutInflater.from(CandidateFlowActivity.this).inflate(R.layout.candidate_info, null);
            }
        });
        Animation in = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        this.mTitle.setInAnimation(in);
        this.mTitle.setOutAnimation(out);
        this.mFlow = (FeatureCoverFlow) findViewById(R.id.coverflow);
        this.mReps = new ArrayList();
        this.mReps.add(new Representative());
        this.mAdapter = new CandidateAdapter(this, this.mReps);
        this.mFlow.setAdapter(this.mAdapter);
        new CandidateTask().execute(new Void[0]);
    }

    protected void onResume() {
        super.onResume();
        sendScreenAnalytics("/representatives/specialFeatured");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_candidate_flow, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        onBackPressed();
        return true;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        this.mTitleView.setText("Featured");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
