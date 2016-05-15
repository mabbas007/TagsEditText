package mabbas007.myapplication;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.Arrays;
import java.util.Collection;

import mabbas007.tagsedittext.TagsEditText;

public class MainActivity extends AppCompatActivity
        implements TagsEditText.TagsEditListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private TagsEditText mTagsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTagsEditText = (TagsEditText) findViewById(R.id.tagsEditText);
        mTagsEditText.setTags("1", "2", "3", "4", "5 6", " 7 8 ");
        mTagsEditText.setTagsListener(this);

        setButtonClickListener(R.id.btnChangeTags);
        setButtonClickListener(R.id.btnChangeBackground);
        setButtonClickListener(R.id.btnChangeColor);
        setButtonClickListener(R.id.btnChangeSize);
        setButtonClickListener(R.id.btnChangeDrawableLeft);
        setButtonClickListener(R.id.btnChangeDrawableRight);
        setButtonClickListener(R.id.btnChangeClosePadding);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnChangeTags: {
                mTagsEditText.setTags("1", "2", "3");
                break;
            }
            case R.id.btnChangeBackground: {
                mTagsEditText.setTagsBackground(R.drawable.square);
                break;
            }
            case R.id.btnChangeColor: {
                mTagsEditText.setTagsTextColor(android.R.color.black);
                break;
            }
            case R.id.btnChangeSize: {
                mTagsEditText.setTagsTextSize(R.dimen.larger_text_size);
                break;
            }
            case R.id.btnChangeDrawableLeft: {
                mTagsEditText.setCloseDrawableLeft(R.drawable.tag_close);
                break;
            }
            case R.id.btnChangeDrawableRight: {
                mTagsEditText.setCloseDrawableRight(R.drawable.tag_close);
                break;
            }
            case R.id.btnChangeClosePadding: {
                mTagsEditText.setCloseDrawablePadding(R.dimen.larger_padding);
                break;
            }
        }
    }

    @Override
    public void onTagsChanged(Collection<String> tags) {
        Log.d(TAG, "Tags changed: ");
        Log.d(TAG, Arrays.toString(tags.toArray()));
    }

    @Override
    public void onEditingFinished() {
//        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(mTagsEditText.getWindowToken(), 0);
//        //mTagsEditText.clearFocus();
    }

    private void setButtonClickListener(@IdRes int id) {
        findViewById(id).setOnClickListener(this);
    }

}
