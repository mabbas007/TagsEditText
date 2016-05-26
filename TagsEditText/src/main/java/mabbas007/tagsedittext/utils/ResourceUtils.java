package mabbas007.tagsedittext.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;

/**
 * Created by Mohammad Abbas on 5/10/2016.
 */
public final class ResourceUtils {

    private ResourceUtils() throws InstantiationException {
        throw new InstantiationException("This utility class is created for instantiation");
    }

    public static int getColor(Context context, @ColorRes int resourceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(resourceId);
        } else {
            return context.getResources().getColor(resourceId);
        }
    }

    public static Drawable getDrawable(Context context, @DrawableRes int resourceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(resourceId);
        } else {
            return context.getResources().getDrawable(resourceId);
        }
    }

    public static float getDimension(Context context, @DimenRes int resourceId) {
        return context.getResources().getDimension(resourceId);
    }

    public static int getDimensionPixelSize(Context context, @DimenRes int resourceId) {
        return context.getResources().getDimensionPixelSize(resourceId);
    }

}
