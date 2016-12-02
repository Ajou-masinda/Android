package com.example.loon.malhada;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by loon on 2016-12-01.
 */

public class ConditionActivity extends AppCompatActivity {
    private int IR,ID;
    TextView nameText, locationText;
    Button chUpB, chdownB, tmpUpB, tmpDownB, onoffB, volUpB, volDownB, modifyB;
    private Intent intent;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugcondition);
        intent = getIntent();
        nameText = (TextView) findViewById(R.id.nameSetT);
        locationText = (TextView) findViewById(R.id.locationSetT);
        IR = intent.getExtras().getInt("ir");
        ID = intent.getExtras().getInt("id");
        nameText.setText(intent.getExtras().getString("name"));
        locationText.setText(intent.getExtras().getString("location"));
        chUpB = (Button) findViewById(R.id.channel_upB);
        chdownB = (Button) findViewById(R.id.channel_dwonB);
        tmpUpB = (Button) findViewById(R.id.Tmp_upB);
        tmpDownB = (Button) findViewById(R.id.tmp_downB);
        onoffB = (Button) findViewById(R.id.OnOffB);
        volUpB = (Button) findViewById(R.id.volume_upB);
        volDownB = (Button) findViewById(R.id.volume_downB);
        modifyB = (Button) findViewById(R.id.modifyB);
        if (IR == 0) {
            chUpB.setVisibility(View.INVISIBLE);
            chdownB.setVisibility(View.INVISIBLE);
            tmpUpB.setVisibility(View.INVISIBLE);
            tmpDownB.setVisibility(View.INVISIBLE);
            volUpB.setVisibility(View.INVISIBLE);
            volDownB.setVisibility(View.INVISIBLE);
        } else if (IR / 10 == 1) {
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
            intentModifyActivity.putExtra("name",nameText.getText().toString());
            intentModifyActivity.putExtra("location",locationText.getText().toString());
            startActivityForResult(intentModifyActivity, 1);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        // 넘어갔던 화면에서 되돌아 왔을 때
        if (resultCode==2) { // 정상 반환일 경우에만 동작하겠다
            intent.putExtra("name", data.getStringExtra("name"));
            intent.putExtra("location", data.getStringExtra("location"));
            intent.putExtra("id",ID);
            setResult(2, intent);
            finish();
        }
        else if(resultCode==3){
            intent.putExtra("id",ID);
            setResult(3, intent);
            finish();
        }
    }
}
