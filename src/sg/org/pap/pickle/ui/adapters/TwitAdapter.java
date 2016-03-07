package sg.org.pap.pickle.ui.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.ui.pickle.BaseAdapter;
import sg.org.pap.pickle.ui.pickle.PickleApp;
import twitter4j.Status;

public class TwitAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<Status> mData;
    private ViewHolder mHolder;

    public class ViewHolder {
        TextView mDate;
        ImageView mImgUser;
        TextView mName;
        TextView mScreenName;
        TextView mText;

        public ViewHolder(View v) {
            this.mName = (TextView) v.findViewById(R.id.tv_name);
            this.mText = (TextView) v.findViewById(R.id.tv_text);
            this.mDate = (TextView) v.findViewById(R.id.tv_date);
            this.mImgUser = (ImageView) v.findViewById(R.id.img_user);
            this.mScreenName = (TextView) v.findViewById(R.id.tv_screen_name);
        }
    }

    public TwitAdapter(Activity activity, List<Status> data) {
        super(activity);
        this.mActivity = activity;
        this.mData = new ArrayList(data);
    }

    public int getCount() {
        return this.mData.size();
    }

    public Object getItem(int i) {
        return this.mData.get(i);
    }

    public long getItemId(int i) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = getInflater().inflate(R.layout.cell_tweet, null);
            this.mHolder = new ViewHolder(convertView);
            convertView.setTag(this.mHolder);
        }
        Status mTweet = (Status) this.mData.get(position);
        this.mHolder = (ViewHolder) convertView.getTag();
        if (mTweet != null) {
            this.mHolder.mDate.setText(PickleApp.getDateDifference(mTweet.getCreatedAt()));
            this.mHolder.mName.setText(mTweet.getUser().getName());
            this.mHolder.mName.setText(mTweet.getUser().getName());
            this.mHolder.mText.setText(mTweet.getText());
            this.mHolder.mScreenName.setText("@" + mTweet.getUser().getScreenName());
        }
        return convertView;
    }

    public void refresh(List<Status> defect) {
        this.mData = new ArrayList(defect);
        notifyDataSetChanged();
    }
}
