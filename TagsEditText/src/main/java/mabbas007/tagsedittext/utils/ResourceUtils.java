package mabbas007.tagsedittext.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Created by Engin on 5/10/2016.
 */
public class ResourceUtils
{
    public static int getColor(Context context, int resourceId)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            return context.getColor(resourceId);
        }
        else
        {
            return context.getResources().getColor(resourceId);
        }
    }

    public static Drawable getDrawable(Context context, int resourceId)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return context.getDrawable(resourceId);
        }
        else
        {
            return context.getResources().getDrawable(resourceId);
        }
    }
}
