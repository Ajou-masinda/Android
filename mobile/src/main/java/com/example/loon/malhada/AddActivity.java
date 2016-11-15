package com.example.loon.malhada;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;

/**
 * Created by loon on 2016-11-14.
 */

public class AddActivity extends AppCompatActivity {

    RadioGroup Gradio;
    CheckBox IRcheck;
    View AddMain;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_plug);
        //AddMain = findViewById(R.id.addLayout);
        //AddMain.setOnTouchListener(touchListener);
        Gradio = (RadioGroup) findViewById(R.id.Gradio);
        Gradio.setVisibility(View.INVISIBLE);
        IRcheck = (CheckBox) findViewById(R.id.IRcheck);
        IRcheck.setOnTouchListener(touchListener);
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
}
