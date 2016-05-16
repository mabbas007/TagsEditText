package mabbas007.tagsedittext;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mabbas007.tagsedittext.utils.ResourceUtils;


/**
 * Created by Mohammad Abbas on 5/10/16.
 * Needs a lot of work
 * BETA
 */
public class TagsEditText extends AutoCompleteTextView {

    private static final String SEPARATOR = " ";
    public static final String NEW_LINE = "\n";

    private static final String LAST_STRING = "lastString";
    private static final String TAGS = "tags";
    private static final String SUPER_STATE = "superState";
    private static final String UNDER_CONSTRUCTION_TAG = "underConstructionTag";
    private static final String ALLOW_SPACES_IN_TAGS = "allowSpacesInTags";

    private static final String TAGS_BACKGROUND = "tagsBackground";
    private static final String TAGS_TEXT_COLOR = "tagsTextColor";
    private static final String TAGS_TEXT_SIZE = "tagsTextSize";
    private static final String LEFT_DRAWABLE_RESOURCE = "leftDrawable";
    private static final String RIGHT_DRAWABLE_RESOURCE = "rightDrawable";
    private static final String DRAWABLE_PADDING = "drawablePadding";

    private String mLastString = "";
    private boolean mIsAfterTextWatcherEnabled = true;

    private int mTagsTextColor;
    private float mTagsTextSize;
    private int mTagsBackground;
    private Drawable mLeftDrawable;
    private int mLeftDrawableResouce = 0;

    private Drawable mRightDrawable;
    private int mRightDrawableResouce = 0;

    private int mDrawablePadding;

    private boolean mIsSpacesAllowedInTags = false;
    private boolean mIsSetTextDisabled = false;

    private List<TagSpan> mTagSpans = new ArrayList<>();
    private List<Tag> mTags = new ArrayList<>();

    private TagsEditListener mListener;

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mIsAfterTextWatcherEnabled) {
                setTags();
            }
        }
    };


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
        if (mIsSetTextDisabled) return;
        if (!TextUtils.isEmpty(text)) {
            String source = mIsSpacesAllowedInTags ? text.toString().trim() : text.toString().replaceAll(" ", "");
            if (mTags.isEmpty()) {
                Tag tag = new Tag();
                tag.setIndex(0);
                tag.setPosition(0);
                tag.setSource(source);
                tag.setSpan(true);
                mTags.add(tag);
            } else {
                int size = mTags.size();
                Tag lastTag = mTags.get(size - 1);
                if (!lastTag.isSpan()) {
                    lastTag.setSource(source);
                    lastTag.setSpan(true);
                } else {
                    Tag newTag = new Tag();
                    newTag.setIndex(size);
                    newTag.setPosition(lastTag.getPosition() + lastTag.getSource().length() + 1);
                    newTag.setSource(source);
                    newTag.setSpan(true);
                    mTags.add(newTag);
                }
            }
            buildStringWithTags(mTags);
            mTextWatcher.afterTextChanged(getText());
        } else {
            super.setText(text, type);
        }
    }

    /**
     * use this method to set tags
     */
    public void setTags(CharSequence... tags) {
        mTagSpans.clear();
        mTags.clear();

        int length = tags != null ? tags.length : 0;
        int position = 0;
        for (int i = 0; i < length; i++) {
            Tag tag = new Tag();
            tag.setIndex(i);
            tag.setPosition(position);
            String source = mIsSpacesAllowedInTags ? tags[i].toString().trim() : tags[i].toString().replaceAll(" ", "");
            tag.setSource(source);
            tag.setSpan(true);
            mTags.add(tag);
            position += source.length() + 1;
        }
        buildStringWithTags(mTags);
        mTextWatcher.afterTextChanged(getText());
    }

    @Override
    public Parcelable onSaveInstanceState() {

        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState());

        Tag[] tags = new Tag[mTags.size()];
        mTags.toArray(tags);

        bundle.putParcelableArray(TAGS, tags);
        bundle.putString(LAST_STRING, mLastString);
        bundle.putString(UNDER_CONSTRUCTION_TAG, getNewTag(getText().toString()));

        bundle.putInt(TAGS_TEXT_COLOR, mTagsTextColor);
        bundle.putInt(TAGS_BACKGROUND, mTagsBackground);
        bundle.putFloat(TAGS_TEXT_SIZE, mTagsTextSize);
        bundle.putInt(LEFT_DRAWABLE_RESOURCE, mLeftDrawableResouce);
        bundle.putInt(RIGHT_DRAWABLE_RESOURCE, mRightDrawableResouce);
        bundle.putInt(DRAWABLE_PADDING, mDrawablePadding);
        bundle.putBoolean(ALLOW_SPACES_IN_TAGS, mIsSpacesAllowedInTags);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Context context = getContext();
            Bundle bundle = (Bundle) state;

            mTagsTextColor = bundle.getInt(TAGS_TEXT_COLOR, mTagsTextColor);
            mTagsBackground = bundle.getInt(TAGS_BACKGROUND, mTagsBackground);
            mTagsTextSize = bundle.getFloat(TAGS_TEXT_SIZE, mTagsTextSize);

            mLeftDrawableResouce = bundle.getInt(LEFT_DRAWABLE_RESOURCE, mLeftDrawableResouce);
            if (mLeftDrawableResouce != 0) {
                mLeftDrawable = ResourceUtils.getDrawable(context, mLeftDrawableResouce);
            }

            mRightDrawableResouce = bundle.getInt(RIGHT_DRAWABLE_RESOURCE, mRightDrawableResouce);
            if (mRightDrawableResouce != 0) {
                mRightDrawable = ResourceUtils.getDrawable(context, mRightDrawableResouce);
            }

            mDrawablePadding = bundle.getInt(DRAWABLE_PADDING, mDrawablePadding);
            mIsSpacesAllowedInTags = bundle.getBoolean(ALLOW_SPACES_IN_TAGS, mIsSpacesAllowedInTags);

            mLastString = bundle.getString(LAST_STRING);
            Tag[] tags = (Tag[]) bundle.getParcelableArray(TAGS);

            if (tags != null) {
                Collections.addAll(mTags, tags);
                buildStringWithTags(mTags);
                mTextWatcher.afterTextChanged(getText());
            }
            state = bundle.getParcelable(SUPER_STATE);
            mIsSetTextDisabled = true;
            super.onRestoreInstanceState(state);
            mIsSetTextDisabled = false;

            String temp = bundle.getString(UNDER_CONSTRUCTION_TAG);
            if (!TextUtils.isEmpty(temp))
                getText().append(temp);
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void buildStringWithTags(List<Tag> tags) {
        mIsAfterTextWatcherEnabled = false;
        getText().clear();
        for (Tag tag : tags) {
            getText().append(tag.getSource()).append(SEPARATOR);
        }
        mLastString = getText().toString();
        if (!TextUtils.isEmpty(mLastString)) {
            getText().append(NEW_LINE);
        }
        mIsAfterTextWatcherEnabled = true;
    }

    public void setTagsTextColor(@ColorRes int color) {
        mTagsTextColor = ResourceUtils.getColor(getContext(), color);
        setTags(convertTagSpanToArray(mTagSpans));
    }

    public void setTagsTextSize(@DimenRes int textSize) {
        mTagsTextSize = ResourceUtils.getDimension(getContext(), textSize);
        setTags(convertTagSpanToArray(mTagSpans));
    }

    public void setTagsBackground(@DrawableRes int background) {
        mTagsBackground = background;
        setTags(convertTagSpanToArray(mTagSpans));
    }

    public void setCloseDrawableLeft(@DrawableRes int drawable) {
        mLeftDrawable = ResourceUtils.getDrawable(getContext(), drawable);
        mLeftDrawableResouce = drawable;
        setTags(convertTagSpanToArray(mTagSpans));
    }

    public void setCloseDrawableRight(@DrawableRes int drawable) {
        mRightDrawable = ResourceUtils.getDrawable(getContext(), drawable);
        mRightDrawableResouce = drawable;
        setTags(convertTagSpanToArray(mTagSpans));
    }

    public void setCloseDrawablePadding(@DimenRes int padding) {
        mDrawablePadding = ResourceUtils.getDimensionPixelSize(getContext(), padding);
        setTags(convertTagSpanToArray(mTagSpans));
    }

    public void setTagsWithSpacesEnabled(boolean isSpacesAllowedInTags) {
        mIsSpacesAllowedInTags = isSpacesAllowedInTags;
        setTags(convertTagSpanToArray(mTagSpans));
    }

    public void setTagsListener(TagsEditListener listener) {
        mListener = listener;
    }

    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Context context = getContext();
        if (attrs == null) {
            mIsSpacesAllowedInTags = false;
            mTagsTextColor = ResourceUtils.getColor(context, R.color.defaultTagsTextColor);
            mTagsTextSize = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsTextSize);
            mTagsBackground = R.drawable.oval;
            mRightDrawable = ResourceUtils.getDrawable(context, R.drawable.tag_close);
            mDrawablePadding = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsCloseImagePadding);
        } else {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TagsEditText, defStyleAttr, defStyleRes);
            try {
                mIsSpacesAllowedInTags = typedArray.getBoolean(R.styleable.TagsEditText_allowSpaceInTag, false);
                mTagsTextColor = typedArray.getColor(R.styleable.TagsEditText_tagsTextColor,
                        ResourceUtils.getColor(context, R.color.defaultTagsTextColor));
                mTagsTextSize = typedArray.getDimensionPixelSize(R.styleable.TagsEditText_tagsTextSize,
                        ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsTextSize));
                mTagsBackground = typedArray.getInt(R.styleable.TagsEditText_tagsBackground,
                        R.drawable.oval);
                mRightDrawable = typedArray.getDrawable(R.styleable.TagsEditText_tagsCloseImageRight);
                mLeftDrawable = typedArray.getDrawable(R.styleable.TagsEditText_tagsCloseImageLeft);
                mDrawablePadding = ResourceUtils.getDimensionPixelSize(context, R.dimen.defaultTagsCloseImagePadding);
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
                    addTextChangedListener(mTextWatcher);
                    mTextWatcher.afterTextChanged(getText());
                }
            });
        }
    }

    private void setTags() {
        mIsAfterTextWatcherEnabled = false;
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

        if (getFilter() != null) {
            performFiltering(getNewTag(str), 0);
        }

        if (str.endsWith(NEW_LINE) || (!mIsSpacesAllowedInTags && str.endsWith(SEPARATOR)) && !isDeleting) {
            buildTags(str);
        }

        mLastString = getText().toString();
        mIsAfterTextWatcherEnabled = true;
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
                mListener.onTagsChanged(convertTagSpanToList(mTagSpans));
            }
        }
    }

    private void updateTags(String newString) {
        String source = getNewTag(newString);
        if (!TextUtils.isEmpty(source) && !source.equals(NEW_LINE)) {
            boolean isSpan = source.endsWith(NEW_LINE) ||
                    (!mIsSpacesAllowedInTags && source.endsWith(SEPARATOR));
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

    private String getNewTag(String newString) {
        StringBuilder builder = new StringBuilder();
        for (Tag tag : mTags) {
            if (!tag.isSpan()) continue;
            builder.append(tag.getSource()).append(SEPARATOR);
        }
        return newString.replace(builder.toString(), "");
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
                mIsAfterTextWatcherEnabled = false;
                removeTagSpan(editable, tagSpan, true);
                mIsAfterTextWatcherEnabled = true;
            }
        }, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void removeTagSpan(Editable editable, TagSpan span, boolean includeSpace) {
        int extraLength = includeSpace ? 1 : 0;
        // include space
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
        mListener.onTagsChanged(convertTagSpanToList(mTagSpans));
    }

    private static List<String> convertTagSpanToList(List<TagSpan> tagSpans) {
        List<String> tags = new ArrayList<>(tagSpans.size());
        for (TagSpan tagSpan : tagSpans) {
            tags.add(tagSpan.getSource());
        }
        return tags;
    }

    private static CharSequence[] convertTagSpanToArray(List<TagSpan> tagSpans) {
        int size = tagSpans.size();
        CharSequence[] values = new CharSequence[size];
        for (int i = 0; i < size; i++) {
            values[i] = tagSpans.get(i).getSource();
        }
        return values;
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
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTagsTextSize);
        textView.setTextColor(mTagsTextColor);
        textView.setBackgroundResource(mTagsBackground);
        textView.setCompoundDrawablesWithIntrinsicBounds(mLeftDrawable, null, mRightDrawable, null);
        textView.setCompoundDrawablePadding(mDrawablePadding);
        return textView;
    }


    private static final class Tag implements Parcelable {

        private int mPosition;
        private int mIndex;
        private String mSource;
        private boolean mSpan;

        private Tag() {
        }

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
