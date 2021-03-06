package com.example.loon.malhada;

import android.content.Intent;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    ConnectServer connectServer;
    DatabaseHandler DbHandler;
    ViewFlipper page;
    String jsonstring;
    ArrayList<String> mResult;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Build a new DatabaseHandler

        connectServer = new ConnectServer("211.106.58.191", 3030);
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
                    humT.setText(getHttp("http://211.106.58.191:4243/api/query/last?timeseries=test.test%7Bhost=house1_hum%7D&back_scan=24&resolve=true")+"%");
                    tmpT.setText(getHttp("http://211.106.58.191:4243/api/query/last?timeseries=test.test%7Bhost=house1_temp%7D&back_scan=24&resolve=true")+"°C");
                    // 걍 외우는게 좋다 -_-;
                    final ImageView iv = (ImageView) findViewById(R.id.imageV);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
                    String currentDateTimeString;
                    URL url;
                    currentDateTimeString = dateFormat.format(new Date(System.currentTimeMillis() - 1800000));
                    Log.v("MAIN ACTIVITY", "CURRENT TIME : " + currentDateTimeString);
                    url = new URL("http://211.106.58.191:4243/q?start=" + currentDateTimeString + "&end=1s-ago&m=sum:test.test%7Bhost=house1_hum,host=house1_temp%7D&o=&yrange=%5B0:%5D&wxh=800x500&style=linespoint&png");
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
        Plist.setAdapter(adapter);
        Plist.setOnItemClickListener(ClickListener);
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
        chaingeActivity();
    }

    private AdapterView.OnItemClickListener ClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.v("DD", "register = " +PlugList.get(position).getRegister() + "position" + position);
            if(PlugList.get(position).getRegister() != 1)
            {
                Intent intentModifyActivity =  new Intent(MainActivity.this, ModifyActivity.class);
                intentModifyActivity.putExtra("position",position);
                startActivityForResult(intentModifyActivity, 1);
            }
            else{
                Intent intentConditionActivity =  new Intent(MainActivity.this, ConditionActivity.class);
                intentConditionActivity.putExtra("name",PlugList.get(position).getName());
                intentConditionActivity.putExtra("location",PlugList.get(position).getLocation());
                intentConditionActivity.putExtra("type",PlugList.get(position).getType());
                intentConditionActivity.putExtra("vendor",PlugList.get(position).getVendor());
                intentConditionActivity.putExtra("position",position);
                startActivityForResult(intentConditionActivity, 1);
            }
            return;
        }
    };

    protected void onStart() {
        super.onStart();
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
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // 넘어갔던 화면에서 되돌아 왔을 때
        if(requestCode==100){
            if(resultCode==RESULT_OK){
                String key = "";
                key = RecognizerIntent.EXTRA_RESULTS;
                mResult = data.getStringArrayListExtra(key);
                String[] result = new String[mResult.size()];
                mResult.toArray(result);
                mResult.set(0,mResult.get(0).replace(" ", ""));
                JSONObject Rejson = new JSONObject();
                try {
                    Rejson.put("REQ","COMMAND");
                    Rejson.put("TYPE","VOICE");
                    Rejson.put("MSG",mResult.get(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String request = Rejson.toString();
                Thread connect = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectServer.sendJSON(request);
                    }
                });
                connect.start();
            }
        }
        else{
            if(resultCode == 1){
                Log.v("dd","return position : " + data.getIntExtra("position",0));
                DbHandler.updatePlug(PlugList.get((data.getIntExtra("position",0))),data.getExtras().getString("name"),data.getExtras().getString("location"),data.getExtras().getInt("type"),data.getExtras().getInt("vendor"),1);

                JSONObject sObject = new JSONObject();//
                try {
                    sObject.put("name",data.getExtras().getString("name"));
                    sObject.put("locate",data.getExtras().getString("location"));
                    sObject.put("type",data.getExtras().getInt("type"));
                    //sObject.put("status",data.getExtras().getInt("status"));
                    sObject.put("vendor",data.getExtras().getInt("vendor"));
                    sObject.put("serial",PlugList.get((data.getIntExtra("position",0))).getSerial());
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
                        connectServer.sendJSON(request);
                    }
                });
                connect.start();
                chaingeActivity();
            }
            else if(resultCode==2){
                DbHandler.deleteContacnt(PlugList.get((data.getIntExtra("position",0))));
                JSONObject sObject = new JSONObject();//
                try {
                    sObject.put("serial",PlugList.get((data.getIntExtra("position",0))).getSerial());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject Rejson = new JSONObject();
                try {
                    Rejson.put("REQ","SET");
                    Rejson.put("ACTION","DELETE");
                    Rejson.put("MSG",sObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String request = Rejson.toString();
                Thread connect = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectServer.sendJSON(request);
                    }
                });
                connect.start();
                chaingeActivity();
            }
            else{
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
        //adapter.addItem("NAME","SERIAL");
        PlugList = DbHandler.getAllCustomer_Info();
        for(int i=0; i< PlugList.size(); i++)
            adapter.addItem(PlugList.get(i).getName(),PlugList.get(i).getSerial(),PlugList.get(i).getRegister(), PlugList.get(i).getStatus());
        adapter.notifyDataSetChanged();
    }

    public void operateMalhanda(View v) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "말하세요.");
        startActivityForResult(i, 100);
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
}
