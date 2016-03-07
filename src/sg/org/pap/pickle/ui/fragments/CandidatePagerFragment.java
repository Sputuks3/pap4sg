package sg.org.pap.pickle.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import it.moondroid.coverflow.BuildConfig;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.models.Representative;

public class CandidatePagerFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    @Bind({2131624218})
    ImageView mImage;
    @Bind({2131624144})
    TextView mName;
    @Bind({2131624217})
    TextView mQuote;
    private Representative mRepresentative;
    @Bind({2131624181})
    TextView mTown;

    public static CandidatePagerFragment newInstance(Representative myRep) {
        CandidatePagerFragment fragment = new CandidatePagerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, myRep);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.mRepresentative = (Representative) getArguments().getSerializable(ARG_PARAM1);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_profile, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (this.mRepresentative != null) {
            this.mImage.setVisibility(8);
            this.mName.setText(this.mRepresentative.getName());
            this.mTown.setText(this.mRepresentative.getTownName());
            this.mQuote.setText(this.mRepresentative.getDescription());
            return;
        }
        this.mImage.setVisibility(8);
        this.mName.setText(BuildConfig.FLAVOR);
        this.mTown.setText(BuildConfig.FLAVOR);
        this.mQuote.setText(BuildConfig.FLAVOR);
    }
}
