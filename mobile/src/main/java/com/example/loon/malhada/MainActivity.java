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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
        AddBt = (Button) findViewById(R.id.AddBt);
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
                    URL url = new URL("http://192.168.1.196:4242/q?start=2016/12/02-00:00:00&end=1s-ago&m=sum:test.test&o=&yrange=%5B0:%5D&wxh=1280x680&style=linespoint&png");
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
                jsonstring = connectServer.sendJSON(request);
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
                plug.setLocation(jOBject.getString("locate"));
                plug.setType(jOBject.getInt("type"));
                plug.setVendor(jOBject.getInt("vendor"));
                plug.setSerial(jOBject.getString("serial"));
                plug.setStatus(jOBject.getInt("status"));
                plug.setRegister(jOBject.getInt("register"));
                plug.printAllelements();
                if(DbHandler.updatePlug(plug,plug.getName(),plug.getLocation(),plug.getType(),plug.getVendor(),0)==0){
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

                Log.v("DD", "register = " +PlugList.get(position-1).getRegister() + "position" + position);
                if(PlugList.get(position-1).getRegister() != 0)
                {
                    Intent intentModifyActivity =  new Intent(MainActivity.this, ModifyActivity.class);
                    intentModifyActivity.putExtra("position",position);
                    startActivityForResult(intentModifyActivity, 1);
                }
                else{
                    Intent intentConditionActivity =  new Intent(MainActivity.this, ConditionActivity.class);
                    intentConditionActivity.putExtra("name",PlugList.get(position-1).getName());
                    intentConditionActivity.putExtra("location",PlugList.get(position-1).getLocation());
                    intentConditionActivity.putExtra("type",PlugList.get(position-1).getType());
                    intentConditionActivity.putExtra("position",position);
                    startActivityForResult(intentConditionActivity, 1);
                }
            }
            return false;
        }
    };


    protected void onStart() {
        super.onStart();
        googleClient.connect();
    }
    public void OnClick(View v) {
        if(v==AddBt){
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
                    jsonstring = connectServer.sendJSON(request);
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
                    plug.setLocation(jOBject.getString("locate"));
                    plug.setType(jOBject.getInt("type"));
                    plug.setVendor(jOBject.getInt("vendor"));
                    plug.setSerial(jOBject.getString("serial"));
                    plug.setStatus(jOBject.getInt("status"));
                    plug.setRegister(jOBject.getInt("register"));
                    plug.printAllelements();
                    if(DbHandler.updatePlug(plug,plug.getName(),plug.getLocation(),plug.getType(),plug.getVendor(),0)==0){
                        DbHandler.addContact(plug);
                        PlugList.add(plug);
                        adapter.addItem(plug.getName(),plug.getStatus());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
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
        if(resultCode == 1){
            Log.v("dd","return position : " + data.getIntExtra("position",0));
            DbHandler.updatePlug(PlugList.get((data.getIntExtra("position",0))-1),data.getExtras().getString("name"),data.getExtras().getString("location"),data.getExtras().getInt("type"),data.getExtras().getInt("vendor"),1);

            JSONObject sObject = new JSONObject();//
            try {
                sObject.put("name",data.getExtras().getString("name"));
                sObject.put("locate",data.getExtras().getString("location"));
                sObject.put("type",data.getExtras().getInt("type"));
                sObject.put("vendor",data.getExtras().getInt("vendor"));
                sObject.put("serial",PlugList.get((data.getIntExtra("position",0))-1).getSerial());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject Rejson = new JSONObject();
            try {
                Rejson.put("REQ","SET");
                Rejson.put("MSG",sObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String request = Rejson.toString();
            Thread connect = new Thread(new Runnable() {
                @Override
                public void run() {
                    jsonstring = connectServer.sendJSON(request);
                }
            });
            connect.start();
        }
        else if(resultCode==2){
            DbHandler.deleteContacnt(PlugList.get((data.getIntExtra("postion",0))-1));
            adapter.deleteItem((data.getIntExtra("postion",0)));
            adapter.notifyDataSetChanged();
        }
    }
}
