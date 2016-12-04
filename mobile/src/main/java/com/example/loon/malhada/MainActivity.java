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
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

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
    TextView tmpT, humT;
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
        tmpT = (TextView) findViewById(R.id.tmpT);
        humT = (TextView) findViewById(R.id.humT);
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
                 try {
                     humT.setText(getHttp("http://192.168.1.196:4242/api/query/last?timeseries=test.test%7Bhost=house1_hum%7D&back_scan=24&resolve=true")+"%");
                     tmpT.setText(getHttp("http://192.168.1.196:4242/api/query/last?timeseries=test.test%7Bhost=house1_temp%7D&back_scan=24&resolve=true")+"°C");
                     // 걍 외우는게 좋다 -_-;
                     final ImageView iv = (ImageView) findViewById(R.id.imageV);
                     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                     String currentDateTimeString;
                     URL url;
                         currentDateTimeString = dateFormat.format(new Date(System.currentTimeMillis() - 1800000));
                         url = new URL("http://192.168.1.196:4242/q?start=" + currentDateTimeString + "&end=1s-ago&m=sum:test.test%7Bhost=house1_hum,host=house1_temp%7D&o=&yrange=%5B0:%5D&wxh=800x500&style=linespoint&png");
                         InputStream is = url.openStream();
                         final Bitmap bm = BitmapFactory.decodeStream(is);
                         handler.post(new Runnable() {
                             @Override
                             public void run() { // 화면에 그려줄 작업
                                 iv.setImageBitmap(bm);
                             }
                         });
                         iv.setImageBitmap(bm); //비트맵 객체로 보여주기
                        iv.notify();
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
            DbHandler.deleteAll();
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
                DbHandler.addContact(plug);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Plist.setAdapter(adapter);
        Plist.setOnItemLongClickListener(longClickListener);
        chaingeActivity();
    }
    private AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int position, long arg3){
            if(position>0) {

                Log.v("DD", "register = " +PlugList.get(position-1).getRegister() + "position" + position);
                if(PlugList.get(position-1).getRegister() != 1)
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
                DbHandler.deleteAll();
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
                    DbHandler.addContact(plug);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            chaingeActivity();
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
                Rejson.put("ACTION","MODIFY");
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
            chaingeActivity();
        }
        else if(resultCode==2){
            DbHandler.deleteContacnt(PlugList.get((data.getIntExtra("postion",0))-1));
            JSONObject sObject = new JSONObject();//
            try {
                sObject.put("serial",PlugList.get((data.getIntExtra("position",0))-1).getSerial());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject Rejson = new JSONObject();
            try {
                Rejson.put("REQ","SET");
                Rejson.put("ACTION","DELTE");
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
            chaingeActivity();
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
    private void chaingeActivity(){
        adapter.clear();
        adapter.addItem("NAME","SERIAL");
        PlugList = DbHandler.getAllCustomer_Info();
        for(int i=0; i< PlugList.size(); i++)
            adapter.addItem(PlugList.get(i).getName(),PlugList.get(i).getSerial());
        adapter.notifyDataSetChanged();
    }
}
