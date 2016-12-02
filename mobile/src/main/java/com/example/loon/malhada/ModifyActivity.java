package com.example.loon.malhada;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by loon on 2016-12-01.
 */

public class ModifyActivity extends AppCompatActivity {

    RadioGroup Gradio,Gaddradio;
    CheckBox IRcheck;
    RadioButton AirRb,TvRb,LgRb,SamsungRb;
    EditText nameEt,locationEt;
    private Intent intent;
    private Button DeleteB, ModifyB;
    private int type=0, vendor = 0,position;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugmodify);
        nameEt = (EditText) findViewById(R.id.name_mEt);
        locationEt = (EditText) findViewById(R.id.location_mEt);
        Gradio = (RadioGroup) findViewById(R.id.Gradio);
        Gradio.setVisibility(View.INVISIBLE);
        Gaddradio = (RadioGroup) findViewById(R.id.Gaddradio);
        Gaddradio.setVisibility(View.INVISIBLE);
        IRcheck = (CheckBox) findViewById(R.id.IRcheck);
        intent = getIntent();
        position = intent.getExtras().getInt("position");
        nameEt.setText(intent.getExtras().getString("name"));
        locationEt.setText(intent.getExtras().getString("location"));
        DeleteB = (Button) findViewById(R.id.Delete_mB);
        ModifyB = (Button) findViewById(R.id.modify_mB);
        IRcheck.setOnTouchListener(touchListener);
        AirRb = (RadioButton) findViewById(R.id.AirRb);
        TvRb = (RadioButton) findViewById(R.id.TvRb);
        LgRb = (RadioButton) findViewById(R.id.LgRb);
        SamsungRb = (RadioButton) findViewById(R.id.SamsungRb);
    }
    private View.OnTouchListener touchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (Gradio.getVisibility() == View.INVISIBLE && !IRcheck.isChecked()) {
                    Gradio.setVisibility(View.VISIBLE);
                    Gaddradio.setVisibility(View.VISIBLE);
                } else if (Gradio.getVisibility() == View.VISIBLE && IRcheck.isChecked()) {
                    Gradio.setVisibility(View.INVISIBLE);
                    Gaddradio.setVisibility(View.INVISIBLE);
                }
            }
            return false;
        }
    };
    public void OnClickModity(View v){
        if(v == DeleteB){
            setResult(2, intent);
            finish();
        }
        else if(v == ModifyB){
            intent.putExtra("name", nameEt.getText().toString());
            intent.putExtra("location", locationEt.getText().toString());
            intent.putExtra("position",position);
            boolean end = true;
            if(!IRcheck.isChecked()){
                type = vendor = 0;
            }
            else if (IRcheck.isChecked()&&(AirRb.isChecked() || TvRb.isChecked()) && (LgRb.isChecked() || SamsungRb.isChecked())) {
                if(AirRb.isChecked()){
                    type = 1;
                }
                else{
                    type = 2;
                }
                if(LgRb.isChecked()){
                    vendor = 1;
                }
                else{
                    vendor = 2;
                }
            }
            else{
                end = false;
            }
            intent.putExtra("type", type);
            intent.putExtra("vendor", vendor);
            if(end){
                setResult(1, intent);
                finish();
            }
        }
    }
}