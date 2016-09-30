package com.androidweardocs.wearablemessage;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Communicate with server
 * The server is constructed by Raspberry Pi, Node.js
 * Send JSON from Android to Server
 */
public class ConnectServer {
    private String serverURL = "";

    public ConnectServer(String serverURL) {
        this.serverURL = serverURL;
    }

    public ConnectServer(String serverURL, int serverPort) {
        this.serverURL = "http://" + serverURL + ":" + Integer.toString(serverPort);
    }

    public String sendJSON(String jsonMSG){
        OutputStream os = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        HttpURLConnection conn = null;
        String response = "";
        URL url = null;
        try {
            url = new URL(serverURL);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5*1000);
            conn.setReadTimeout(5*1000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Cache-control", "no-cache");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            os = conn.getOutputStream();
            os.write(jsonMSG.getBytes());
            os.flush();
            int responseCode = conn.getResponseCode();
            Log.v("==RESPONSE CODE==", String.valueOf(responseCode));

            if(responseCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] byteBuffer = new byte[1024];
                byte[] byteData = null;
                int nLength = 0;
                while((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1){
                    baos.write(byteBuffer, 0, nLength);
                }
                byteData = baos.toByteArray();
                response = new String(byteData);

                Log.v("==DATA RESPONSE==", response);
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }
}
