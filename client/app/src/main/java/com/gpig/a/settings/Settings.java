package com.gpig.a.settings;

import android.app.Activity;

import com.gpig.a.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class Settings {
    //TODO save/load to/from file
    public static String ServerIP = "10.0.2.2";
    public static int ServerPort = 8000;
    public final static String FILENAME = "ServerSettings.json";

    public static void readFromFile(Activity activity){
        if(FileUtils.doesFileExist(activity, FILENAME)) {
            try {
                JSONObject jObject = new JSONObject(FileUtils.readFromInternalStorage(activity, FILENAME));
                ServerIP = jObject.getString("ServerIP");
                ServerPort = jObject.getInt("ServerPort");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToFile(Activity activity){
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("ServerIP", ServerIP);
            jObject.put("ServerPort", ServerPort);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileUtils.writeToInternalStorage(activity, FILENAME, jObject.toString());
    }

    public static String getUrlFromSettings(Activity activity){
        readFromFile(activity);
        return "https://" + ServerIP + ":" + Integer.toString(ServerPort) + "/controller/route";
    }
}
