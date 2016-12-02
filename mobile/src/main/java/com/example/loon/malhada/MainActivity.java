package com.example.loon.malhada;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    ConnectServer connectServer;
    DatabaseHandler DbHandler;
    ViewFlipper page;
    String jsonstring;
    GoogleApiClient googleClient;
    List<Plug_Info> PlugList = new ArrayList<Plug_Info>();
    Button AddBt;
    ListView Plist;
    PlugAdapter adapter = new PlugAdapter();
    float xAtDown=0, xAtUp=0;
    private SQLiteDatabase DB;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Build a new DatabaseHandler

        connectServer = new ConnectServer("192.168.1.196", 3030);
        DbHandler = new DatabaseHandler(this);
        Plist = (ListView) findViewById(R.id.PList);
        try {
            DB = DbHandler.getWritableDatabase();
        } catch (SQLiteAbortException ex){
            DB = DbHandler.getReadableDatabase();
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
        Thread t = new Thread(new Runnable() {
             @Override
             public void run() { // 오래 거릴 작업을 구현한다
                 // TODO Auto-generated method stub
                 try{
                     // 걍 외우는게 좋다 -_-;
                     final ImageView iv = (ImageView)findViewById(R.id.imageV);
                    URL url = new URL("http://image.hankookilbo.com/images201506/hankookilbo_bi.jpg");
                    InputStream is = url.openStream();
                    final Bitmap bm = BitmapFactory.decodeStream(is);
                    handler.post(new Runnable() {
                         @Override
                         public void run() { // 화면에 그려줄 작업
                             iv.setImageBitmap(bm);
                             }
                        });
                     iv.setImageBitmap(bm); //비트맵 객체로 보여주기
                    } catch(Exception e){
                    }
                }
            });
        t.start();
        final String finalMessage = "{"+"\"plug\":\"GET\""+"}";
        Thread connect = new Thread(new Runnable() {
            @Override
            public void run() {
                jsonstring = connectServer.sendJSON(finalMessage);
            }
        });
        connect.start();
        try {
            connect.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        };
        try {
            JSONArray jarray = new JSONArray(jsonstring);
            for(int i=0; i<jarray.length(); i++)
            {
                JSONObject jOBject = jarray.getJSONObject(i);
                Plug_Info plug = new Plug_Info();
                plug.setName(jOBject.getString("name"));
                plug.setLocation(jOBject.getString("location"));
                plug.setType(jOBject.getInt("type"));
                plug.setVendor(jOBject.getInt("vendor"));
                plug.setSerial(jOBject.getString("serial"));
                plug.setStatus(jOBject.getInt("status"));
                plug.setRegister(jOBject.getInt("register"));
                if(plug.getRegister()!=1){
                    DbHandler.addContact(plug);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Plist.setAdapter(adapter);
        Plist.setOnItemLongClickListener(longClickListener);
        adapter.addItem("이름",1);
        PlugList = DbHandler.getAllCustomer_Info();
        for(int i=0; i< PlugList.size(); i++)
        {
            adapter.addItem(PlugList.get(i).getName(),PlugList.get(i).getStatus());
        }
    }
    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long arg3){
            if(position>0) {
                Intent intentConditionActivity =  new Intent(MainActivity.this, ConditionActivity.class);
                intentConditionActivity.putExtra("name",PlugList.get(position-1).getName());
                intentConditionActivity.putExtra("location",PlugList.get(position-1).getLocation());
                intentConditionActivity.putExtra("ir",PlugList.get(position-1).getType());
                intentConditionActivity.putExtra("id",position);
                startActivityForResult(intentConditionActivity, 1);
                /*
                DbHandler.deleteContacnt(PlugList.get((position-1)));
                adapter.deleteItem(position);
                adapter.notifyDataSetChanged();
                */
            }
            return false;
        }
    };


    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }
    public void OnClick(View v) throws InterruptedException {
        if(v==AddBt){
            final String finalMessage = "{"+"\"plug\":\"GET\""+"}";
            Thread connect = new Thread(new Runnable() {
                @Override
                public void run() {
                    connectServer.sendJSON(finalMessage);
                }
            });
            connect.start();
            connect.join();
            Intent intentAddActivity =  new Intent(MainActivity.this, AddActivity.class);
            startActivityForResult(intentAddActivity, 1);
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
        if (resultCode==1) { // 정상 반환일 경우에만 동작하겠다
            // Plug_Info plug_info = new Plug_Info( data.getStringExtra("name"),data.getStringExtra("location"),data.getIntExtra("ir",0),"192.168.43.55",0);
            //PlugList.add(plug_info);
            //adapter.addItem(plug_info.getName(), plug_info.getStatus());
            //adapter.notifyDataSetChanged();
            //DbHandler.addContact(plug_info);
        }
        else if(resultCode == 2){
            //DbHandler.updatePlug(PlugList.get((data.getIntExtra("id",0))-1),data.getStringExtra("name"),data.getStringExtra("location"));
        }
        else if(resultCode==3){
            //DbHandler.deleteContacnt(PlugList.get((data.getIntExtra("id",0))-1));
            //adapter.deleteItem((data.getIntExtra("id",0)));
            //adapter.notifyDataSetChanged();
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
}
