package com.example.loon.malhada;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    GoogleApiClient googleClient;
    ConnectServer connectServer;
    String jsonstring;
    String hum,tmp;
    public void onCreate(){
        connectServer = new ConnectServer("202.30.29.209", 3030);
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleClient.connect();
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra("message");
            Log.v("dd","message = "+ message );
            try {
                JSONObject jOBject =new JSONObject(message);
                Log.v("dd","REQ = " + jOBject.getString("REQ"));
                if(jOBject.getString("REQ").equals("GET")){
                    if(jOBject.getString("TYPE").equals("LIST")){ // 리스트 요청시
                        JSONObject sObject = new JSONObject();
                        try {
                            sObject.put("REQ","GET");
                            sObject.put("MSG","LIST");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        final String request = sObject.toString();
                        Thread connect = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("MAIN ACTIVITY", "GET Plug LIST - Request to Deudnunda");
                                jsonstring = connectServer.sendJSON(request);
                            }
                        });
                        connect.start();
                        try {
                            connect.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        };
                        sObject.put("REQ","SET");
                        sObject.put("TYPE","LIST");
                        sObject.put("MSG",jsonstring);
                        new SendToDataLayerThread("/message_path",sObject.toString()).start();
                    }
                    else{ // 온도, 습도 요청시
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() { // 오래 거릴 작업을 구현한다
                                // TODO Auto-generated method stub
                                try {
                                    hum = getHttp("http://202.30.29.209:4243/api/query/last?timeseries=test.test%7Bhost=house1_hum%7D&back_scan=24&resolve=true");
                                    tmp = getHttp("http://202.30.29.209:4243/api/query/last?timeseries=test.test%7Bhost=house1_temp%7D&back_scan=24&resolve=true");
                                }
                                catch(Exception e){
                                }

                            }
                        });
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        JSONObject Rejson = new JSONObject();
                        try {
                            Rejson.put("REQ","SET");
                            Rejson.put("TYPE","ENVIR");
                            Rejson.put("HUM",hum);
                            Rejson.put("TMP",tmp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.v("dd","Rejson = " + Rejson.toString());
                        new SendToDataLayerThread("/message_path",Rejson.toString()).start();
                    }
                }
                else{// 커맨드
                    Thread connect = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connectServer.sendJSON(message);
                        }
                    });
                    connect.start();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    class SendToDataLayerThread extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {

            NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("myTag", "ERROR: failed to send Message");
                }
            }
        }
    }
    private String getHttp(String url)throws Exception{
        HttpClient client = new DefaultHttpClient();
        //String url = "http://google.co.kr";
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        HttpEntity resEntity = response.getEntity();
        if(resEntity != null){
            String res = EntityUtils.toString(resEntity);
            JSONArray jarray = new JSONArray(res);
            JSONObject jObject = jarray.getJSONObject(0);
            return jObject.getString("value");
        }
        return null;
    }
}
