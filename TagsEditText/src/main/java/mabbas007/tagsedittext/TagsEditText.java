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
import android.os.Parcel;
import android.os.Parcelable;
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

    private List<TagSpan> mTagSpans = new ArrayList<>();
    private List<Tag> mTags = new ArrayList<>();

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

    /**
     * do not use this method to set tags
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        String textWithSeparator = text.toString();
        if (!textWithSeparator.endsWith(NEW_LINE) && !TextUtils.isEmpty(textWithSeparator)) {
            textWithSeparator += NEW_LINE;
        }
        super.setText(textWithSeparator, type);
    }

    /**
     * use this method to set tags
     */
    public void setTags(CharSequence ... tags) {
        mTagSpans.clear();
        mTags.clear();
        getText().clear();

        int length = tags.length;
        int position = 0;
        for (int i = 0; i < length; i++, position = getText().length()) {
            Tag tag = new Tag();
            tag.setIndex(i);
            tag.setPosition(position);
            String source = tags[i].toString().trim();
            tag.setSource(source);
            tag.setSpan(true);
            mTags.add(tag);
            getText().append(source).append(SEPARATOR);
        }
        mLastString = getText().toString();
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
        if (str.endsWith(NEW_LINE)) {
            isEnterClicked = true;
        }

        boolean isDeleting = mLastString.length() > str.length();
        if (mLastString.endsWith(SEPARATOR)
                && !str.endsWith(NEW_LINE)
                && isDeleting
                && !mTagSpans.isEmpty()) {
            TagSpan toRemoveSpan = mTagSpans.get(mTagSpans.size() - 1);
            Tag tag = toRemoveSpan.getTag();
            if (tag.getPosition() + tag.getSource().length() == str.length()) {
                removeTagSpan(editable, toRemoveSpan, false);
                str = editable.toString();
            }
        }

        if (str.endsWith(NEW_LINE) && !isDeleting) {
            buildTags(str);
        }

        mLastString = getText().toString();
        afterTextEnabled = true;
        if (isEnterClicked && mListener != null) {
            mListener.onEditingFinished();
        }
    }

    private void buildTags(String str) {
        if (str.length() != 0) {
            updateTags(str);

            SpannableStringBuilder sb = new SpannableStringBuilder();
            for (final TagSpan tagSpan : mTagSpans) {
                addTagSpan(sb, tagSpan);
            }

            int size = mTags.size();
            for (int i = mTagSpans.size(); i < size; i++) {
                Tag tag = mTags.get(i);
                String source = tag.getSource();
                if (tag.isSpan()) {
                    TextView tv = createTextView(source);
                    Drawable bd = convertViewToDrawable(tv);
                    bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
                    final TagSpan span = new TagSpan(bd, source);
                    addTagSpan(sb, span);
                    span.setTag(tag);
                    mTagSpans.add(span);
                } else {
                    sb.append(source);
                }
            }

            getText().clear();
            getText().append(sb);
            setMovementMethod(LinkMovementMethod.getInstance());
            setSelection(sb.length());
            if (mListener != null && !str.equals(mLastString)) {
                mListener.onTagsChanged(convertTagSpanToString(mTagSpans));
            }
        }
    }

    private void updateTags(String newString) {
        StringBuilder builder = new StringBuilder();
        for (Tag tag : mTags) {
            if (!tag.isSpan()) continue;
            builder.append(tag.getSource()).append(SEPARATOR);
        }
        String source = newString.replace(builder.toString(), "");
        if (!TextUtils.isEmpty(source)
                && !source.equals(NEW_LINE)) {
            boolean isSpan = source.endsWith(NEW_LINE);
            if (isSpan) {
                source = source.substring(0, source.length() - 1);
                source = source.trim();
            }
            Tag tag = new Tag();
            tag.setSource(source);
            tag.setSpan(isSpan);
            int size = mTags.size();
            if (size <= 0) {
                tag.setIndex(0);
                tag.setPosition(0);
            } else {
                Tag lastTag = mTags.get(size - 1);
                tag.setIndex(size);
                tag.setPosition(lastTag.getPosition() + lastTag.getSource().length() + 1);
            }
            mTags.add(tag);
        }
    }

    private void addTagSpan(SpannableStringBuilder sb, final TagSpan tagSpan) {
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
    }

    private void removeTagSpan(Editable editable, TagSpan span, boolean includeSpace) {
        int extraLength = includeSpace ? 1 : 0;
        // inlcude space
        Tag tag = span.getTag();
        int tagPosition = tag.getPosition();
        int tagIndex = tag.getIndex();
        int tagLength = span.getSource().length() + extraLength;
        editable.replace(tagPosition, tagPosition + tagLength, "");
        int size = mTags.size();
        for (int i = tagIndex + 1; i < size; i++) {
            Tag newTag = mTags.get(i);
            newTag.setIndex(i - 1);
            newTag.setPosition(newTag.getPosition() - tagLength);
        }
        mTags.remove(tagIndex);
        mTagSpans.remove(tagIndex);
        if (mListener == null) return;
        mListener.onTagsChanged(convertTagSpanToString(mTagSpans));
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

    private static final class Tag implements Parcelable {

        private int mPosition;
        private int mIndex;
        private String mSource;
        private boolean mSpan;

        private Tag() {}

        private void setPosition(int pos) {
            mPosition = pos;
        }

        private int getPosition() {
            return mPosition;
        }

        private void setIndex(int index) {
            mIndex = index;
        }

        private int getIndex() {
            return mIndex;
        }

        public void setSource(String source) {
            mSource = source;
        }

        public String getSource() {
            return mSource;
        }

        public void setSpan(boolean span) {
            mSpan = span;
        }

        public boolean isSpan() {
            return mSpan;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mPosition);
            dest.writeInt(mIndex);
            dest.writeString(mSource);
            dest.writeInt(mSpan ? 1 : 0);
        }

        protected Tag(Parcel in) {
            mPosition = in.readInt();
            mIndex = in.readInt();
            mSource = in.readString();
            mSpan = in.readInt() == 1;
        }

        public static final Creator<Tag> CREATOR = new Creator<Tag>() {
            @Override
            public Tag createFromParcel(Parcel in) {
                return new Tag(in);
            }

            @Override
            public Tag[] newArray(int size) {
                return new Tag[size];
            }
        };

    }

    private static final class TagSpan extends ImageSpan {

        private Tag mTag;

        public TagSpan(Drawable d, String source) {
            super(d, source);
        }

        private void setTag(Tag tag) {
            mTag = tag;
        }

        public Tag getTag() {
            return mTag;
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
