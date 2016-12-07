package com.example.loon.malhada;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by loon on 2016-12-01.
 */

public class ConditionActivity extends AppCompatActivity {
    private int type, position;
    ConnectServer connectServer;
    TextView nameText, locationText;
    Button chUpB, chdownB, tmpUpB, tmpDownB, onB, volUpB, volDownB, modifyB,offB;
    private Intent intent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugcondition);
        connectServer = new ConnectServer("211.106.58.191", 3030);
        intent = getIntent();
        nameText = (TextView) findViewById(R.id.nameSetT);
        locationText = (TextView) findViewById(R.id.locationSetT);
        type = intent.getExtras().getInt("type");
        position = intent.getExtras().getInt("position");
        nameText.setText(intent.getExtras().getString("name"));
        locationText.setText(intent.getExtras().getString("location"));
        chUpB = (Button) findViewById(R.id.channel_upB);
        chdownB = (Button) findViewById(R.id.channel_dwonB);
        tmpUpB = (Button) findViewById(R.id.Tmp_upB);
        tmpDownB = (Button) findViewById(R.id.tmp_downB);
        onB = (Button) findViewById(R.id.OnB);
        offB = (Button) findViewById(R.id.OffB);
        volUpB = (Button) findViewById(R.id.volume_upB);
        volDownB = (Button) findViewById(R.id.volume_downB);
        modifyB = (Button) findViewById(R.id.modifyB);
        if (type == 0) {
            chUpB.setVisibility(View.INVISIBLE);
            chdownB.setVisibility(View.INVISIBLE);
            tmpUpB.setVisibility(View.INVISIBLE);
            tmpDownB.setVisibility(View.INVISIBLE);
            volUpB.setVisibility(View.INVISIBLE);
            volDownB.setVisibility(View.INVISIBLE);
        } else if (type == 1) {
            chUpB.setVisibility(View.INVISIBLE);
            chdownB.setVisibility(View.INVISIBLE);
            volUpB.setVisibility(View.INVISIBLE);
            volDownB.setVisibility(View.INVISIBLE);
        } else {
            tmpUpB.setVisibility(View.INVISIBLE);
            tmpDownB.setVisibility(View.INVISIBLE);
        }
    }
    public void OnClickCondition(View v){
        if(v == modifyB){
            Intent intentModifyActivity =  new Intent(ConditionActivity.this, ModifyActivity.class);
            intentModifyActivity.putExtra("position",position);
            intentModifyActivity.putExtra("name",nameText.getText().toString());
            intentModifyActivity.putExtra("location",locationText.getText().toString());
            startActivityForResult(intentModifyActivity, 1);
        }
        else if(v == onB){
            JSONObject Rejson = new JSONObject();
            try {
                Rejson.put("REQ","COMMAND");
                Rejson.put("NAME",nameText.getText().toString());
                Rejson.put("STATUS","ON");
                Rejson.put("MSG","");
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
        else if(v == offB){
            JSONObject Rejson = new JSONObject();
            try {
                Rejson.put("REQ","COMMAND");
                Rejson.put("NAME",nameText.getText().toString());
                Rejson.put("STATUS","OFF");
                Rejson.put("MSG","");
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
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // 넘어갔던 화면에서 되돌아 왔을 때
        if (resultCode==1) { // 정상 반환일 경우에만 동작하겠다

            intent.putExtra("name", data.getStringExtra("name"));
            intent.putExtra("location", data.getStringExtra("location"));
            intent.putExtra("position", position);
            setResult(1, intent);
            finish();
        }
        else if(resultCode==2){
            intent.putExtra("position", position);
            setResult(2, intent);
            finish();
        }
    }
}
