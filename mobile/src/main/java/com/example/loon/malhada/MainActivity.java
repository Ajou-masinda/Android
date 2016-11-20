package com.example.loon.malhada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    DatabaseHandler DbHandler;
    ViewFlipper page;
    GoogleApiClient googleClient;
    Button AddBt;
    float xAtDown=0, xAtUp=0;
    private SQLiteDatabase DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Build a new DatabaseHandler
        DbHandler = new DatabaseHandler(this);
        try {
            DB = DbHandler.getWritableDatabase();
        } catch (SQLiteAbortException ex){
            DB = DbHandler.getWritableDatabase();
        }
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Intent intent = new Intent(MainActivity.this,MainService.class);
        startService(intent);
        page = (ViewFlipper) findViewById(R.id.page);
        page.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    xAtDown = event.getX();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    xAtUp = event.getX();
                    if(xAtUp < xAtDown){
                        page.showNext();
                    }
                    else if(xAtUp > xAtDown){
                        page.showPrevious();
                    }
                }
                return true;
            }
        });
        AddBt = (Button) findViewById(R.id.AddBt);
    }


    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }
    public void OnClick(View v)
    {
        if(v==AddBt){
            Intent intenthealthActivity =  new Intent(MainActivity.this, AddActivity.class);
            startActivityForResult(intenthealthActivity, 1);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }



    @Override
    public void onConnectionSuspended(int i) {
  /*      if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();*/
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // 넘어갔던 화면에서 되돌아 왔을 때
        if (resultCode==RESULT_OK) { // 정상 반환일 경우에만 동작하겠다
            Toast.makeText(MainActivity.this, data.getStringExtra("name") + data.getStringExtra("location") + data.getIntExtra("ir",0) , Toast.LENGTH_LONG).show();
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
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v("myTag", "Main activity received message: " + message);
            // Display message in UI
        }
    }
}
