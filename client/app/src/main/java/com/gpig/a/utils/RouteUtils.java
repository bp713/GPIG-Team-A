package com.gpig.a.utils;

import android.app.Activity;
import android.util.Log;

public final class RouteUtils {

    private static final String TAG = "RouteUtils";

    private RouteUtils(){}

    public static boolean hasRouteChanged(Activity act, String storedFile, String serverResponse){
        if (FileUtils.doesFileExist(act, storedFile)) {
            String data = FileUtils.readFromInternalStorage(act, storedFile);
            return !data.equals(serverResponse);
        }
        return true;
    }
}
