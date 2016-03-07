package sg.org.pap.pickle.ui.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import java.util.ArrayList;
import java.util.List;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.api.RestClient;
import sg.org.pap.pickle.api.data.Media;
import sg.org.pap.pickle.api.data.PillarsResponse;
import sg.org.pap.pickle.models.Message;
import sg.org.pap.pickle.models.Pillar;
import sg.org.pap.pickle.models.User;
import sg.org.pap.pickle.ui.activities.ManifestDetailActivity;
import sg.org.pap.pickle.ui.activities.VideoPlayerActivity;
import sg.org.pap.pickle.ui.activities.WebviewActivity;
import sg.org.pap.pickle.ui.activities.YoutubeNewsDetailActivity;
import sg.org.pap.pickle.ui.base.BaseFragment;
import sg.org.pap.pickle.ui.base.Connection;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class ManifestoFragment extends BaseFragment {
    ManifestAdapter mAdapter;
    List<Pillar> mContent;
    private LayoutManager mLayoutManager;
    @Bind({2131624184})
    UltimateRecyclerView mListView;
    @Bind({2131624124})
    ProgressBar mLoading;
    Media mMedia;
    User mUser;

    public class ManifestAdapter extends UltimateViewAdapter<ViewHolder> {
        private List<Pillar> mDataset;

        public class ViewHolder extends UltimateRecyclerviewViewHolder {
            ImageView mImgNews;
            TextView mPreview;
            TextView mTitle;

            public ViewHolder(View v, boolean isItem) {
                super(v);
                if (isItem) {
                    this.mTitle = (TextView) v.findViewById(R.id.tv_title);
                    this.mPreview = (TextView) v.findViewById(R.id.tv_subtitle);
                    this.mImgNews = (ImageView) v.findViewById(R.id.img_bg);
                }
            }

            public void onItemSelected() {
                this.itemView.setBackgroundColor(-3355444);
            }

            public void onItemClear() {
                this.itemView.setBackgroundColor(0);
            }
        }

        public ManifestAdapter(List<Pillar> myDataset) {
            this.mDataset = new ArrayList(myDataset);
        }

        public ViewHolder getViewHolder(View view) {
            return new ViewHolder(view, false);
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_pillar, parent, false), true);
        }

        public int getAdapterItemCount() {
            return this.mDataset.size();
        }

        public long generateHeaderId(int position) {
            if (getItem(position) != null) {
                return Long.parseLong(getItem(position).getId());
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
                    final Pillar mPillar = (Pillar) this.mDataset.get(position);
                    if (mPillar != null) {
                        holder.mTitle.setText(mPillar.getTitle());
                        holder.mPreview.setText(mPillar.getCaption());
                        Glide.with(ManifestoFragment.this.getActivity()).load(mPillar.getThumbnail()).crossFade().into(holder.mImgNews);
                    }
                    holder.itemView.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            Intent intent = new Intent(ManifestoFragment.this.getActivity(), ManifestDetailActivity.class);
                            intent.putExtra(YoutubeNewsDetailActivity.ARG_CONTENT, mPillar);
                            if (mPillar.getContentType().equals(Message.TYPE_TERMS_OF_USE)) {
                                intent.putExtra(ManifestDetailActivity.CONTENT_MEDIA, ManifestoFragment.this.mMedia);
                            }
                            ManifestoFragment.this.startAnimatedActivity(intent);
                        }
                    });
                }
            }
        }

        public android.support.v7.widget.RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup viewGroup) {
            return new android.support.v7.widget.RecyclerView.ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_view_pager, viewGroup, false)) {
            };
        }

        public void onBindHeaderViewHolder(android.support.v7.widget.RecyclerView.ViewHolder viewHolder, int i) {
        }

        public Pillar getItem(int position) {
            if (this.customHeaderView != null) {
                position--;
            }
            if (position < this.mDataset.size()) {
                return (Pillar) this.mDataset.get(position);
            }
            return new Pillar();
        }
    }

    private class ManifestTask extends AsyncTask<Void, Void, PillarsResponse> {
        private ManifestTask() {
        }

        protected void onPreExecute() {
            super.onPreExecute();
            ManifestoFragment.this.mLoading.setVisibility(0);
        }

        protected PillarsResponse doInBackground(Void... voids) {
            return RestClient.getInstance().getPillars();
        }

        protected void onPostExecute(PillarsResponse pillars) {
            super.onPostExecute(pillars);
            ManifestoFragment.this.mContent = new ArrayList(pillars.getPillars());
            ManifestoFragment.this.mMedia = pillars.getMedia();
            if (!(ManifestoFragment.this.mMedia == null || !ManifestoFragment.this.mMedia.getType().equals(Message.TYPE_HQ_CONTACT) || PickleApp.getInstance().isManifest())) {
                Intent intent = new Intent(ManifestoFragment.this.getActivity(), VideoPlayerActivity.class);
                intent.putExtra(WebviewActivity.CONTENT, ManifestoFragment.this.mMedia.getVideo());
                ManifestoFragment.this.startActivity(intent);
                PickleApp.getInstance().setManifest(true);
            }
            PickleApp.getInstance().setmPillars(ManifestoFragment.this.mContent);
            ManifestoFragment.this.mAdapter = new ManifestAdapter(ManifestoFragment.this.mContent);
            ManifestoFragment.this.mListView.setAdapter(ManifestoFragment.this.mAdapter);
            ManifestoFragment.this.mLoading.setVisibility(8);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUser = PickleApp.getInstance().getUser();
        this.mContent = PickleApp.getInstance().getmPillars();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sendScreenAnalytics("/aboutUs");
        return inflater.inflate(R.layout.fragment_manifesto, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        this.mListView.setHasFixedSize(true);
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.mListView.setLayoutManager(this.mLayoutManager);
        if (Connection.connected(getActivity())) {
            new ManifestTask().execute(new Void[0]);
        } else {
            Connection.showSnackBar(getActivity(), getView());
        }
    }
}
