package mabbas007.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.Collection;

import mabbas007.tagsedittext.TagsEditText;

public class MainActivity extends AppCompatActivity implements TagsEditText.TagsEditListener {

    private static final String TAG = "MainActivity";
    private TagsEditText mTagsEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTagsEditText = (TagsEditText) findViewById(R.id.tagsEditText);
        mTagsEditText.setTags("1", "2", "3", "4", "5 6", " 7 8 ");
        Button button = (Button) findViewById(R.id.btnChangeColor);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagsEditText.setTags("1", "2", "3");
                mTagsEditText.setTagsBackgroundColor(android.R.color.holo_orange_dark);
                //tagsEditText.setTagsTextColor(R.color.colorPrimary);
            }
        });
        mTagsEditText.setTagsListener(this);
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

}
