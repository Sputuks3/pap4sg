package sg.org.pap.pickle.ui.adapters;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.facebook.internal.AnalyticsEvents;
import java.util.ArrayList;
import java.util.List;
import sg.org.pap.pickle.R;
import sg.org.pap.pickle.models.News;
import sg.org.pap.pickle.ui.pickle.BaseAdapter;
import sg.org.pap.pickle.ui.pickle.PickleApp;

public class NewsAdapter extends BaseAdapter {
    private Activity mActivity;
    private ArrayList<News> mData;
    private ViewHolder mHolder;

    public class ViewHolder {
        TextView mDate;
        ImageView mImgNews;
        TextView mPreview;
        TextView mTitle;

        public ViewHolder(View v) {
            this.mTitle = (TextView) v.findViewById(R.id.tv_title);
            this.mPreview = (TextView) v.findViewById(R.id.tv_preview);
            this.mDate = (TextView) v.findViewById(R.id.tv_date);
            this.mImgNews = (ImageView) v.findViewById(R.id.img_news);
        }
    }

    public NewsAdapter(Activity activity, List<News> data) {
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
            convertView = getInflater().inflate(R.layout.cell_news, null);
            this.mHolder = new ViewHolder(convertView);
            convertView.setTag(this.mHolder);
        }
        News mNews = (News) this.mData.get(position);
        this.mHolder = (ViewHolder) convertView.getTag();
        if (mNews != null) {
            this.mHolder.mDate.setText(PickleApp.getDateDifference(mNews.getDate()));
            this.mHolder.mTitle.setText(mNews.getTitle());
            this.mHolder.mPreview.setText(Html.fromHtml(mNews.getContent()));
            if (mNews.getTypeName().equals(AnalyticsEvents.PARAMETER_SHARE_DIALOG_CONTENT_VIDEO)) {
                Glide.with(this.mActivity).load(mNews.getVideoThumbnail()).crossFade().into(this.mHolder.mImgNews);
            } else {
                Glide.with(this.mActivity).load(mNews.getThumbnail()).crossFade().into(this.mHolder.mImgNews);
            }
        }
        return convertView;
    }

    public void refresh(List<News> defect) {
        this.mData = new ArrayList(defect);
        notifyDataSetChanged();
    }
}
