package sg.org.pap.pickle.ui.pickle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import sg.org.pap.pickle.R;
import twitter4j.TwitterResponse;

public class StyledTextView extends TextView implements Styled {

    private class ImageGetter implements android.text.Html.ImageGetter {
        private ImageGetter() {
        }

        public Drawable getDrawable(String source) {
            if (StyledTextView.this.isInEditMode()) {
                return null;
            }
            Drawable d = StyledTextView.this.getResources().getDrawable(StyledTextView.this.getResources().getIdentifier(source, "drawable", StyledTextView.this.getContext().getPackageName()));
            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            return d;
        }
    }

    public StyledTextView(Context context) {
        this(context, null);
    }

    public StyledTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842884);
    }

    public StyledTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(context, attrs);
    }

    private void setTypeface(Context context, AttributeSet attrs) {
        Typeface typeface = getTypeface();
        int style = 0;
        if (typeface != null) {
            style = typeface.getStyle();
        }
        String fontFile = null;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StyledTextView);
        if (ta != null) {
            int n = ta.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = ta.getIndex(i);
                switch (attr) {
                    case TwitterResponse.NONE /*0*/:
                        fontFile = ta.getString(attr);
                        break;
                    case TwitterResponse.READ /*1*/:
                        setText(Html.fromHtml(ta.getString(attr), new ImageGetter(), null));
                        break;
                    default:
                        break;
                }
            }
            if (!isInEditMode() && !TextUtils.isEmpty(fontFile)) {
                if (_font.get(fontFile) == null) {
                    _font.put(fontFile, Typeface.createFromAsset(getResources().getAssets(), "fonts/" + fontFile));
                }
                typeface = (Typeface) _font.get(fontFile);
                if (typeface != null) {
                    setTypeface(typeface, style);
                }
            }
        }
    }
}
