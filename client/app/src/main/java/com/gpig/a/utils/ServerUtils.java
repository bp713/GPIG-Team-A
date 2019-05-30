package com.gpig.a.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.gpig.a.PollServer;
import com.gpig.a.settings.Settings;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ServerUtils {

    private static final String TAG = "ServerUtils";
    public static PollServer pollServer;

    public static AsyncTask<String, String, String> postToServer(String path, String data){
        return new POSTAPI().execute("https://" + Settings.ServerIP + ":" + Settings.ServerPort + "/" + path, data);
    }

    public static AsyncTask<String, String, String> getFromServer(String path) {
        String serverUrl = "https://" + Settings.ServerIP + ":" + Settings.ServerPort + "/" + path;
        return new GETAPI().execute(serverUrl);
    }

    static class POSTAPI extends AsyncTask<String, String, String> {

        POSTAPI() {
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            String data = params[1];
            OutputStream out;
            StringBuilder result = new StringBuilder();
            Log.d(TAG, "POST: " + urlString);
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                out = new BufferedOutputStream(urlConnection.getOutputStream());

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                writer.flush();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();
                    reader.close();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.i(TAG, "POST ERROR: " + e.getMessage());
                e.printStackTrace();
            }
            Log.d(TAG, "POST Done: " + result.toString());
            return result.toString();
        }
    }

    static class GETAPI extends AsyncTask<String, String, String> {

        GETAPI() {
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(3000);
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();
                    reader.close();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                Log.e(TAG, ex.toString());
                return "True";
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e(TAG, ex.toString());
            }

            return result.toString();
        }
    }
}
