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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import mabbas007.tagsedittext.utils.ResourceUtils;


/**
 * Created by Mohammad Abbas on 5/10/16.
 * Needs a lot of work
 * BETA
 */
public class TagsEditText extends EditText {

    private static final String TAG = "TagsEditText";
    private static final String SEPARATOR = " ";
    public static final String NEW_LINE = "\n";

    private String mLastString = "";
    private boolean afterTextEnabled = true;

    private int mTagsTextColor;
    private int mTagsBackgroundColor;
    private Drawable mCloseDrawable;

    private List<TagSpan> mTags = new ArrayList<>();

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
        if (!textWithSeparator.endsWith(SEPARATOR) && !TextUtils.isEmpty(textWithSeparator)) {
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

        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
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
                    setText(getText());
                }
            });
        }
    }

    private void setTags() {
        afterTextEnabled = false;
        boolean isEnterClicked = false;

        final Editable editable = getText();
        String str = editable.toString();
        if (str.contains(NEW_LINE)) {
            str = str.replaceAll(NEW_LINE, SEPARATOR);
            isEnterClicked = true;
        }

        boolean isDeleting = mLastString.length() > str.length();

        if (mLastString.endsWith(SEPARATOR) && !str.endsWith(SEPARATOR) && isDeleting) {
            TagSpan[] toRemoveSpans = editable.getSpans(0, str.length(), TagSpan.class);
            if (toRemoveSpans.length > 0) {
                removeTagSpan(editable, toRemoveSpans[toRemoveSpans.length - 1], false);
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
        if (str.length() != 0) {
            List<String> tags = Arrays.asList(str.split("\\s+"));

            SpannableStringBuilder sb = new SpannableStringBuilder();
            int size = tags.size();
            boolean tagsEndWithSpace = str.endsWith(SEPARATOR);
            if (!tagsEndWithSpace) {
                tags.remove(size - 1);
                size = tags.size();
            }
            for (final TagSpan tagSpan : mTags) {
                addTagSpan(sb, tagSpan);
            }
            for (int i = mTags.size(); i < size; i++) {
                String tag = tags.get(i);
                TextView tv = createTextView(tag);
                Drawable bd = convertViewToDrawable(tv);
                bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
                final TagSpan span = new TagSpan(bd, tag);
                int position = addTagSpan(sb, span);
                span.setPosition(position);
                span.setIndex(i);
                mTags.add(span);
            }

            if (!tagsEndWithSpace) {
                sb.append(tags.get(tags.size() - 1));
            }

            getText().clear();
            getText().append(sb);
            setMovementMethod(LinkMovementMethod.getInstance());
            setSelection(sb.length());
            if (mListener != null && !str.equals(mLastString)) {
                mListener.onTagsChanged(convertTagSpanToString(mTags));
            }
        }
    }

    private int addTagSpan(SpannableStringBuilder sb, final TagSpan tagSpan) {
        String source = tagSpan.getSource();
        sb.append(source).append(SEPARATOR);
        int length = sb.length();
        int startSpan = length - (source.length() + 1);
        int endSpan = length - 1;
        sb.setSpan(tagSpan, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Editable editable = ((EditText) widget).getText();
                afterTextEnabled = false;
                removeTagSpan(editable, tagSpan, true);
                afterTextEnabled = true;
            }
        }, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return startSpan;
    }

    private void removeTagSpan(Editable editable, TagSpan span, boolean includeSpace) {
        int extraLength = includeSpace ? 1 : 0;
        // inlcude space
        int tagPosition = span.getPosition();
        int tagIndex = span.getIndex();
        int tagLength = span.getSource().length() + extraLength;
        editable.replace(tagPosition, tagPosition + tagLength, "");
        int size = mTags.size();
        for (int i = tagIndex + 1; i < size; i++) {
            TagSpan tagSpan = mTags.get(i);
            tagSpan.setIndex(i - 1);
            tagSpan.setPosition(tagSpan.getPosition() - tagLength);
        }
        mTags.remove(tagIndex);
        if (mListener == null) return;
        mListener.onTagsChanged(convertTagSpanToString(mTags));
    }

    private static List<String> convertTagSpanToString(List<TagSpan> tagSpans) {
        List<String> tags = new ArrayList<>(tagSpans.size());
        for (TagSpan tagSpan : tagSpans) {
            tags.add(tagSpan.getSource());
        }
        return tags;
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
        private int mIndex;

        public TagSpan(Drawable d, String source) {
            super(d, source);
        }

        // private constructors

        private TagSpan(Context context, Bitmap b) {
            super(context, b);
        }

        private TagSpan(Context context, Bitmap b, int verticalAlignment) {
            super(context, b, verticalAlignment);
        }

        private TagSpan(Drawable d) {
            super(d);
        }

        private TagSpan(Drawable d, int verticalAlignment) {
            super(d, verticalAlignment);
        }

        private TagSpan(Drawable d, String source, int verticalAlignment) {
            super(d, source, verticalAlignment);
        }

        private TagSpan(Context context, Uri uri) {
            super(context, uri);
        }

        private TagSpan(Context context, Uri uri, int verticalAlignment) {
            super(context, uri, verticalAlignment);
        }

        private TagSpan(Context context, int resourceId) {
            super(context, resourceId);
        }

        private TagSpan(Context context, int resourceId, int verticalAlignment) {
            super(context, resourceId, verticalAlignment);
        }

        public void setPosition(int pos) {
            mPosition = pos;
        }

        public int getPosition() {
            return mPosition;
        }

        public void setIndex(int index) {
            mIndex = index;
        }

        public int getIndex() {
            return mIndex;
        }

    }

    public interface TagsEditListener {

        void onTagsChanged(Collection<String> tags);
        void onEditingFinished();

    }

    public static class TagsEditListenerAdapter implements TagsEditListener {

        @Override
        public void onTagsChanged(Collection<String> tags) {
        }

        @Override
        public void onEditingFinished() {
        }

    }

}
