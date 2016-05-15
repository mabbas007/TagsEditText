package mabbas007.tagsedittext;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import mabbas007.tagsedittext.utils.ResourceUtils;


/**
 * Created by Mohammad Abbas on 5/10/16.
 * Needs a lot of work
 * BETA
 */
public class TagsEditText extends EditText {

    private static final String TAG = "TagsEditText";
    private static final String SEPARATOR = " ";

    private String mLastString = "";
    private boolean afterTextEnabled = true;

    private int mTagsTextColor;
    private int mTagsBackgroundColor;
    private Drawable mCloseDrawable;

    private ArrayList<String> mTags = new ArrayList<>();

    private TagsEditListener mListener;

    public TagsEditText(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public TagsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public TagsEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagsEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (getText() != null) {
            setSelection(getText().length());
        } else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String textWithSeparator = text.toString();
        if (!textWithSeparator.endsWith(SEPARATOR)) {
            textWithSeparator += SEPARATOR;
        }
        super.setText(textWithSeparator, type);
    }

    public void setTagsTextColor(int color) {
        mTagsTextColor = ResourceUtils.getColor(getContext(), color);
        setTags();
    }

    public void setTagsBackgroundColor(int color) {
        mTagsBackgroundColor = ResourceUtils.getColor(getContext(), color);
        setTags();
    }

    public void setCloseDrawable(Drawable drawable) {
        mCloseDrawable = drawable;
        setTags();
    }

    public void setTagsListener(TagsEditListener listener) {
        mListener = listener;
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs == null) {
            mTagsTextColor = ResourceUtils.getColor(getContext(), android.R.color.white);
            mTagsBackgroundColor = ResourceUtils.getColor(getContext(), android.R.color.holo_green_light);
        } else {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TagsEditText, defStyleAttr, defStyleRes);
            try {
                mTagsTextColor = typedArray.getColor(R.styleable.TagsEditText_tagsTextColor,
                        ResourceUtils.getColor(getContext(), android.R.color.white));
                mTagsBackgroundColor = typedArray.getColor(R.styleable.TagsEditText_tagsBackgroundColor,
                        ResourceUtils.getColor(getContext(), android.R.color.holo_green_light));
                mCloseDrawable = typedArray.getDrawable(R.styleable.TagsEditText_tagsCloseImage);
                if (mCloseDrawable == null) {
                    mCloseDrawable = ResourceUtils.getDrawable(getContext(), R.drawable.tag_close);
                }
            } finally {
                typedArray.recycle();
            }
        }

        setMovementMethod(LinkMovementMethod.getInstance());
        setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (afterTextEnabled) {
                    setTags();
                }
            }
        });
    }

    private void setTags() {
        afterTextEnabled = false;
        boolean isEnterClicked = false;

        final Editable editable = getText();
        String str = editable.toString();
        if (str.contains("\n")) {
            str = str.replaceAll("\n", SEPARATOR);
            isEnterClicked = true;
        }

        boolean isDeleting = mLastString.length() > str.length();

        if (mLastString.endsWith(SEPARATOR) && !str.endsWith(SEPARATOR) && isDeleting) {
            TagSpan[] toRemoveSpans = editable.getSpans(0, str.length(), TagSpan.class);
            if (toRemoveSpans.length > 0) {
                removeSpan(editable, toRemoveSpans[toRemoveSpans.length - 1], false);
                str = editable.toString();
            }
        }

        if (str.endsWith(SEPARATOR) && !isDeleting) {
            buildTags(str);
        }

        mLastString = str;
        afterTextEnabled = true;
        if (isEnterClicked && mListener != null) {
            mListener.onEditingFinished();
        }
    }

    private void buildTags(String str) {
        mTags.clear();
        if (str.length() != 0) {
            String[] tags = str.split("\\s+");
            Collections.addAll(mTags, tags);

            SpannableStringBuilder sb = new SpannableStringBuilder();

            int startSpan, endSpan;

            int size = tags.length;
            boolean tagsEndWithSpace = str.endsWith(SEPARATOR);
            if (!tagsEndWithSpace) {
                size = tags.length - 1;
                mTags.remove(mTags.size() - 1);
            }
            for (int i = 0; i < size; i++) {
                TextView tv = createTextView(tags[i]);
                Drawable bd = convertViewToDrawable(tv);
                bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());

                sb.append(tags[i]);
                sb.append(SEPARATOR);
                startSpan = sb.length() - (tags[i].length() + 1);
                endSpan = sb.length() - 1;

                final TagSpan span = new TagSpan(bd, tags[i]);
                span.setTagPosition(startSpan);
                sb.setSpan(span, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Editable editable = ((EditText) widget).getText();
                        afterTextEnabled = false;
                        removeSpan(editable, span, true);
                        afterTextEnabled = true;
                    }
                }, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }


            if (!tagsEndWithSpace) {
                sb.append(tags[tags.length - 1]);
            }

            getText().clear();
            getText().append(sb);
            setMovementMethod(LinkMovementMethod.getInstance());
            setSelection(sb.length());
            if (mListener != null && !str.equals(mLastString)) {
                mListener.onTagsChanged(mTags);
            }
        }
    }

    private void removeSpan(Editable editable, TagSpan span, boolean includeSpace) {
        if (includeSpace) {
            // inlcude space
            editable.replace(span.getTagPosition(), span.getTagPosition() + span.getSource().length() + 1, "");
            buildTags(editable.toString());
            // if build tags found nothing won't notify listener
            if (mListener != null && mTags.size() == 0) {
                mListener.onTagsChanged(mTags);
            }
        } else {
            editable.replace(span.getTagPosition(), span.getTagPosition() + span.getSource().length(), "");
            mTags.remove(mTags.size() - 1);
            if (mListener != null) {
                mListener.onTagsChanged(mTags);
            }
        }
    }

    private Drawable convertViewToDrawable(View view) {
        int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();
        return new BitmapDrawable(getResources(), viewBmp);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(getContext());
        if (getWidth() > 0) {
            textView.setMaxWidth(getWidth() - 50);
        }
        textView.setText(text);
        textView.setTextSize(16);
        textView.setTextColor(mTagsTextColor);
        textView.setBackgroundResource(R.drawable.oval);
        ((GradientDrawable) textView.getBackground()).setColor(mTagsBackgroundColor);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, mCloseDrawable, null);
        textView.setCompoundDrawablePadding(10);
        return textView;
    }

    private static final class TagSpan extends ImageSpan {

        private int mPosition;

        public TagSpan(Context context, Bitmap b) {
            super(context, b);
        }

        public TagSpan(Context context, Bitmap b, int verticalAlignment) {
            super(context, b, verticalAlignment);
        }

        public TagSpan(Drawable d) {
            super(d);
        }

        public TagSpan(Drawable d, int verticalAlignment) {
            super(d, verticalAlignment);
        }

        public TagSpan(Drawable d, String source) {
            super(d, source);
        }

        public TagSpan(Drawable d, String source, int verticalAlignment) {
            super(d, source, verticalAlignment);
        }

        public TagSpan(Context context, Uri uri) {
            super(context, uri);
        }

        public TagSpan(Context context, Uri uri, int verticalAlignment) {
            super(context, uri, verticalAlignment);
        }

        public TagSpan(Context context, int resourceId) {
            super(context, resourceId);
        }

        public TagSpan(Context context, int resourceId, int verticalAlignment) {
            super(context, resourceId, verticalAlignment);
        }

        public void setTagPosition(int pos) {
            mPosition = pos;
        }

        public int getTagPosition() {
            return mPosition;
        }

    }

    public interface TagsEditListener {

        void onTagsChanged(ArrayList<String> tags);
        void onEditingFinished();

    }

    public static class TagsEditListenerAdapter implements TagsEditListener {

        @Override
        public void onTagsChanged(ArrayList<String> tags) {
        }

        @Override
        public void onEditingFinished() {
        }

    }

}
