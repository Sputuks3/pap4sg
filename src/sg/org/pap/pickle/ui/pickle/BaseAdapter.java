package sg.org.pap.pickle.ui.pickle;

import android.app.Activity;
import android.view.LayoutInflater;
import java.lang.ref.WeakReference;

public abstract class BaseAdapter extends android.widget.BaseAdapter {
    private Activity mActivity;
    private WeakReference<LayoutInflater> mInflater;

    protected BaseAdapter(Activity context) {
        this.mActivity = context;
    }

    public LayoutInflater getInflater() {
        if (this.mInflater == null || this.mInflater.get() == null) {
            this.mInflater = new WeakReference(this.mActivity.getLayoutInflater());
        }
        return (LayoutInflater) this.mInflater.get();
    }
}
