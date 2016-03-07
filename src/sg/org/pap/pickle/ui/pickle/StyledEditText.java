package sg.org.pap.pickle.ui.pickle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import sg.org.pap.pickle.R;
import twitter4j.TwitterResponse;

public class StyledEditText extends EditText implements Styled {
    public StyledEditText(Context context) {
        this(context, null);
    }

    public StyledEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 16842862);
    }

    public StyledEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Typeface typeface = getTypeface();
        int style = 0;
        if (typeface != null) {
            style = typeface.getStyle();
        }
        String fontFile = null;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StyledEditText);
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
