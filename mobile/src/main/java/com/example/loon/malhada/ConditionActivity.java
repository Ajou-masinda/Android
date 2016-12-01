package com.example.loon.malhada;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by loon on 2016-12-01.
 */

public class ConditionActivity extends AppCompatActivity {
    int IR;
    TextView nameText, locationText;
    Button chUpB,chdownB,tmpUpB,tmpDownB, onoffB, volUpB, volDownB;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugcondition);
        Intent intent = getIntent();
        nameText = (TextView) findViewById(R.id.nameSetT);
        locationText = (TextView) findViewById(R.id.locationSetT);
        IR = intent.getExtras().getInt("ir");
        nameText.setText(intent.getExtras().getString("name"));
        locationText.setText(intent.getExtras().getString("location"));
        chUpB = (Button) findViewById(R.id.channel_upB);
        chdownB = (Button) findViewById(R.id.channel_dwonB);
        tmpUpB = (Button) findViewById(R.id.Tmp_upB);
        tmpDownB = (Button) findViewById(R.id.tmp_downB);
        onoffB = (Button) findViewById(R.id.OnOffB);
        volUpB = (Button) findViewById(R.id.volume_upB);
        volDownB = (Button) findViewById(R.id.volume_downB);
        if(IR==0){
            chUpB.setVisibility(View.INVISIBLE);
            chdownB.setVisibility(View.INVISIBLE);
            tmpUpB.setVisibility(View.INVISIBLE);
            tmpDownB.setVisibility(View.INVISIBLE);
            volUpB.setVisibility(View.INVISIBLE);
            volDownB.setVisibility(View.INVISIBLE);
        }
        else if(IR/10==1){
            chUpB.setVisibility(View.INVISIBLE);
            chdownB.setVisibility(View.INVISIBLE);
            volUpB.setVisibility(View.INVISIBLE);
            volDownB.setVisibility(View.INVISIBLE);
        }
        else{
            tmpUpB.setVisibility(View.INVISIBLE);
            tmpDownB.setVisibility(View.INVISIBLE);
        }
    }
}
