package com.example.loon.malhada;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Created by loon on 2016-11-14.
 */

public class AddActivity extends AppCompatActivity {

    RadioGroup Gradio;
    CheckBox IRcheck;
    Button ComBt;
    EditText NameEt, LocationEt;
    Intent intent;
    RadioButton AirRb;
    int ir = 0;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_plug);
        //AddMain = findViewById(R.id.addLayout);
        //AddMain.setOnTouchListener(touchListener);
        Gradio = (RadioGroup) findViewById(R.id.Gradio);
        Gradio.setVisibility(View.INVISIBLE);
        IRcheck = (CheckBox) findViewById(R.id.IRcheck);
        ComBt = (Button) findViewById(R.id.ComBt);
        NameEt = (EditText) findViewById(R.id.nameEt);
        LocationEt = (EditText) findViewById(R.id.locationEt);
        IRcheck.setOnTouchListener(touchListener);
        AirRb = (RadioButton) findViewById(R.id.AirRb);
        intent = getIntent();
    }
    private View.OnTouchListener touchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.v("myTag", "What!!");
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (Gradio.getVisibility() == View.INVISIBLE && !IRcheck.isChecked()) {
                    Gradio.setVisibility(View.VISIBLE);
                } else if (Gradio.getVisibility() == View.VISIBLE && IRcheck.isChecked()) {
                    Gradio.setVisibility(View.INVISIBLE);
                }
            }
            return false;
        }
    };

    public void OnClickAdd(View v)
    {
        if(v==ComBt){
            if(IRcheck.isChecked()){
                ir++;
                if(!AirRb.isChecked())
                    ir++;
            }
            //Toast.makeText(AddActivity.this, NameEt.getText() , Toast.LENGTH_LONG).show();

            intent.putExtra("name", NameEt.getText().toString());
            intent.putExtra("location", LocationEt.getText().toString());
            intent.putExtra("ir", ir);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
