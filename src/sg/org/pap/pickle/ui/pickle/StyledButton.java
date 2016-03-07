package sg.org.pap.pickle.ui.pickle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Button;
import sg.org.pap.pickle.R;
import twitter4j.TwitterResponse;

public class StyledButton extends Button implements Styled {
    public StyledButton(Context context) {
        this(context, null);
    }

    public StyledButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public StyledButton(Context context, AttributeSet attrs, int defStyle) {
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
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StyledButton);
        if (ta != null) {
            int n = ta.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = ta.getIndex(i);
                switch (attr) {
                    case TwitterResponse.NONE /*0*/:
                        fontFile = ta.getString(attr);
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
