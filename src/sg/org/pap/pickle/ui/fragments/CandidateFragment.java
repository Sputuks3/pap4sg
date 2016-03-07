package sg.org.pap.pickle.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import it.moondroid.coverflow.BuildConfig;
import java.util.ArrayList;
import java.util.List;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.Representative;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.activities.CandidateDetail;
import sg.org.pap.pickle.ui.activities.YoutubeNewsDetailActivity;
import sg.org.pap.pickle.ui.base.BaseFragment;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class CandidateFragment extends BaseFragment implements OnClickListener {
    private CandidateAdapter mAdapter;
    @Bind({2131624183})
    Button mAll;
    @Bind({2131624151})
    LinearLayout mChoices;
    private String mId = BuildConfig.FLAVOR;
    private LayoutManager mLayoutManager;
    @Bind({2131624184})
    UltimateRecyclerView mListView;
    @Bind({2131624071})
    ProgressBar mLoading;
    private List<Representative> mMyRepresentatives;
    @Bind({2131624182})
    Button mMyReps;
    private List<Representative> mRepresentatives;
    private User mUser;

    public class CandidateAdapter extends UltimateViewAdapter<ViewHolder> {
        private List<Representative> mDataset;

        public class ViewHolder extends UltimateRecyclerviewViewHolder {
            TextView mName;
            ImageView mProfile;
            TextView mRegion;

            public ViewHolder(View v, boolean isItem) {
                super(v);
                if (isItem) {
                    this.mName = (TextView) v.findViewById(R.id.tv_name);
                    this.mRegion = (TextView) v.findViewById(R.id.tv_region);
                    this.mProfile = (ImageView) v.findViewById(R.id.img_prof);
                }
            }

            public void onItemSelected() {
                this.itemView.setBackgroundColor(-12303292);
            }

            public void onItemClear() {
                this.itemView.setBackgroundColor(0);
            }
        }

        public CandidateAdapter(List<Representative> myDataset) {
            this.mDataset = new ArrayList(myDataset);
        }

        public ViewHolder getViewHolder(View view) {
            return new ViewHolder(view, false);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_candidates, parent, false), true);
        }

        public int getAdapterItemCount() {
            return this.mDataset.size();
        }

        public long generateHeaderId(int position) {
            if (getItem(position) != null) {
                return Long.parseLong(getItem(position).getRid());
            }
            return -1;
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            if (position < getItemCount()) {
                if (this.customHeaderView != null) {
                    if (position > this.mDataset.size()) {
                        return;
                    }
                } else if (position >= this.mDataset.size()) {
                    return;
                }
                if (this.customHeaderView == null || position > 0) {
                    final Representative mRep = (Representative) this.mDataset.get(position);
                    holder.mName.setText(mRep.getName());
                    holder.mRegion.setText(mRep.getConstituencyName());
                    Glide.with(CandidateFragment.this.getActivity()).load(mRep.getPhoto1()).crossFade().into(holder.mProfile);
                    holder.itemView.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Intent intent = new Intent(CandidateFragment.this.getActivity(), CandidateDetail.class);
                            intent.putExtra(YoutubeNewsDetailActivity.ARG_CONTENT, mRep);
                            CandidateFragment.this.startAnimatedActivity(intent);
                        }
                    });
                }
            }
        }

        public android.support.v7.widget.RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            return new android.support.v7.widget.RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.page_profile, viewGroup, false)) {
            };
        }

        public void onBindHeaderViewHolder(android.support.v7.widget.RecyclerView.ViewHolder viewHolder, int i) {
        }

        public Representative getItem(int position) {
            if (this.customHeaderView != null) {
                position--;
            }
            if (position < this.mDataset.size()) {
                return (Representative) this.mDataset.get(position);
            }
            return new Representative();
        }

        public void refresh(List<Representative> myDataset) {
            this.mDataset = new ArrayList(myDataset);
            notifyDataSetChanged();
        }
    }

    private class RepresentativeTask extends AsyncTask<String, Void, List<Representative>> {
        String postalId;

        private RepresentativeTask() {
            this.postalId = BuildConfig.FLAVOR;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            CandidateFragment.this.mLoading.setVisibility(0);
        }

        protected List<Representative> doInBackground(String... strings) {
            if (!TextUtils.isEmpty(strings[0])) {
                this.postalId = strings[0];
            }
            return RestClient.getInstance().getRepresentatives(this.postalId);
        }

        protected void onPostExecute(List<Representative> representativeResponse) {
            super.onPostExecute(representativeResponse);
            if (TextUtils.isEmpty(this.postalId)) {
                CandidateFragment.this.mRepresentatives = new ArrayList();
                for (Representative r : representativeResponse) {
                    if (r.getContentType().equals(Message.TYPE_PDPA)) {
                        CandidateFragment.this.mRepresentatives.add(r);
                    }
                }
            } else {
                CandidateFragment.this.mMyRepresentatives = new ArrayList();
                for (Representative r2 : representativeResponse) {
                    if (r2.getContentType().equals(Message.TYPE_PDPA)) {
                        CandidateFragment.this.mMyRepresentatives.add(r2);
                    }
                }
            }
            CandidateFragment.this.mAdapter.refresh(representativeResponse);
            CandidateFragment.this.mLoading.setVisibility(8);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUser = PickleApp.getInstance().getUser();
        if (this.mUser != null && !TextUtils.isEmpty(this.mUser.getPostalCode())) {
            this.mId = this.mUser.getPostalCode();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendScreenAnalytics("/representatives");
        return inflater.inflate(R.layout.fragment_candidate, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        this.mAdapter = new CandidateAdapter(new ArrayList());
        this.mListView.setAdapter(this.mAdapter);
        if (this.mUser == null) {
            this.mChoices.setVisibility(8);
            loadContent(BuildConfig.FLAVOR);
        } else if (TextUtils.isEmpty(this.mUser.getPostalCode())) {
            this.mChoices.setVisibility(8);
            loadContent(BuildConfig.FLAVOR);
        } else {
            this.mChoices.setVisibility(0);
            this.mAll.setOnClickListener(this);
            this.mMyReps.setOnClickListener(this);
            this.mMyReps.performClick();
        }
        this.mListView.setHasFixedSize(true);
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.mListView.setLayoutManager(this.mLayoutManager);
    }

    public void onClick(View view) {
        this.mMyReps.setSelected(false);
        this.mAll.setSelected(false);
        switch (view.getId()) {
            case R.id.btn_my_reps /*2131624182*/:
                loadContent(this.mId);
                view.setSelected(true);
                return;
            case R.id.btn_all /*2131624183*/:
                loadContent(BuildConfig.FLAVOR);
                view.setSelected(true);
                return;
            default:
                return;
        }
    }

    public void onRefresh() {
        this.mMyRepresentatives = null;
        this.mRepresentatives = null;
        if (this.mUser == null) {
            return;
        }
        if (this.mMyReps.isSelected()) {
            loadContent(this.mId);
        } else {
            loadContent(BuildConfig.FLAVOR);
        }
    }

    private void loadContent(String id) {
        if (this.mAdapter != null) {
            this.mAdapter.refresh(new ArrayList());
        }
        if (TextUtils.isEmpty(id)) {
            if (this.mRepresentatives != null) {
                this.mAdapter.refresh(this.mRepresentatives);
            } else if (Connection.connected(getActivity())) {
                new RepresentativeTask().execute(new String[]{id});
            } else {
                Connection.showSnackBar(getActivity(), getView());
            }
        } else if (this.mMyRepresentatives != null) {
            this.mAdapter.refresh(this.mMyRepresentatives);
        } else if (Connection.connected(getActivity())) {
            new RepresentativeTask().execute(new String[]{id});
        } else {
            Connection.showSnackBar(getActivity(), getView());
        }
    }
}
