package com.gpig.a.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.gpig.a.R;

public final class IconUtils {

    private IconUtils(){}

    public static Drawable getMapIcon(Context c, String type){
        Drawable drawable = null;
        switch (type) {
            case "current":
                drawable = ContextCompat.getDrawable(c, R.drawable.current_location);
                drawable.setColorFilter(Color.rgb(0, 191, 255), PorterDuff.Mode.SRC_IN);
                break;
            case "src":
                drawable = ContextCompat.getDrawable(c, R.drawable.location);
                drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                break;
            case "des":
                drawable = ContextCompat.getDrawable(c, R.drawable.location);
                drawable.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                break;
        }
        return drawable;
    }
}
