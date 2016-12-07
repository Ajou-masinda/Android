package com.example.loon.malhada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private TextView humT,tmpT;
    private String hum="",tmp="";
    private ViewFlipper ViewF;
    private ArrayList<String> mResult;
    private String mSelectedString;
    private final int GOOGLE_STT = 1000;
    PlugAdapter adapter = new PlugAdapter();
    private ListView pList;
    private Button STT, REFRESH;
    private List<PlugList> PlugList = new ArrayList<PlugList>();
    private double xAtDown,xAtUp;
    GoogleApiClient googleClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                humT = (TextView) findViewById(R.id.humT);
                tmpT = (TextView) findViewById(R.id.tmpT);
                STT = (Button) findViewById(R.id.STTB);
                REFRESH = (Button) findViewById(R.id.refreshB);
                ViewF = (ViewFlipper) findViewById(R.id.ViewF);
                pList = (ListView) findViewById(R.id.pList);
                pList.setAdapter(adapter);
                ViewF.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if(event.getAction() == MotionEvent.ACTION_DOWN){
                            xAtDown = event.getX();
                        }
                        else if(event.getAction() == MotionEvent.ACTION_UP){
                            xAtUp = event.getX();
                            if(xAtUp < xAtDown){
                                ViewF.showNext();
                            }
                            else if(xAtUp > xAtDown){
                                ViewF.showPrevious();
                            }
                        }
                        return true;
                    }
                });
            }
        });
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        getREQ("LIST");
        getREQ("RNVIR");
    }

    protected void onClick(View v){
        if(v==STT){
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "말하세요.");
            startActivityForResult(i, GOOGLE_STT);
        }
        else if(v==REFRESH){
            getREQ("LIST");
            getREQ("RNVIR");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == GOOGLE_STT)) {
            String key = "";
            key = RecognizerIntent.EXTRA_RESULTS;
            mResult = data.getStringArrayListExtra(key);
            String[] result = new String[mResult.size()];
            mResult.toArray(result);
            mSelectedString = mResult.get(0);
            JSONObject Rejson = new JSONObject();
            try {
                Rejson.put("REQ","COMMAND");
                Rejson.put("TYPE","VOICE");
                Rejson.put("MSG",mSelectedString.replace(" ",""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new SendToDataLayerThread("/message_path", Rejson.toString()).start();
        } else {
            String msg = null;

            switch (resultCode) {
                case SpeechRecognizer.ERROR_AUDIO:
                    msg = "ERROR_AUDIO.";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    msg = "ERROR_CLIENT.";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    msg = "ERROR_INSUFFICIENT_PERMISSIONS.";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    msg = "ERROR_NETWORK_TIMEOUT.";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    msg = "ERROR_NO_MATCH.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    msg = "ERROR_RECOGNIZER_BUSY.";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    msg = "ERROR_SERVER.";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    msg = "ERROR_SPEECH_TIMEOUT.";
                    break;
            }
        }
    }

    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }
    private void getREQ(String type){
        JSONObject Rejson = new JSONObject();
        try {
            Rejson.put("REQ","GET");
            Rejson.put("TYPE",type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String request = Rejson.toString();
        new SendToDataLayerThread("/message_path", request).start();
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

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v("dd","message = " + message);
            try {
                JSONObject jOBject = new JSONObject(message);
                if(jOBject.getString("REQ").equals("SET")){
                    if(jOBject.getString("TYPE").equals("ENVIR")){
                        hum = jOBject.getString("HUM");
                        tmp = jOBject.getString("TMP");
                        humT.setText(hum+"%");
                        tmpT.setText(tmp+"°C");
                    }
                    else{
                        JSONArray jarray = new JSONArray(jOBject.getString("MSG"));
                        adapter.clear();;
                        for(int i=0; i<jarray.length(); i++){
                            JSONObject listOject = jarray.getJSONObject(i);
                            adapter.addItem(listOject.getString("name"),listOject.getString("serial"),Integer.parseInt(listOject.getString("register")));
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Display message in UI
        }
    }
}
