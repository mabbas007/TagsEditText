package mabbas007.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import mabbas007.tagsedittext.TagsEditText;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TagsEditText tagsEditText = (TagsEditText) findViewById(R.id.tagsEditText);
        Button button = (Button) findViewById(R.id.btnChangeColor);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tagsEditText.setTagsBackgroundColor(android.R.color.holo_orange_dark);
                //tagsEditText.setTagsTextColor(R.color.colorPrimary);
            }
        });
    }
}
