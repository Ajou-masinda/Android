package com.example.loon.malhada;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by loon on 2016-12-01.
 */

public class ModifyActivity extends AppCompatActivity {

    EditText nameEt,locationEt;
    private Intent intent;
    private Button DeleteB, ModifyB;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugmodify);
        nameEt = (EditText) findViewById(R.id.name_mEt);
        locationEt = (EditText) findViewById(R.id.location_mEt);
        intent = getIntent();
        nameEt.setText(intent.getExtras().getString("name"));
        locationEt.setText(intent.getExtras().getString("location"));
        DeleteB = (Button) findViewById(R.id.Delete_mB);
        ModifyB = (Button) findViewById(R.id.modify_mB);
    }
    public void OnClickModity(View v){
        if(v == DeleteB){
            setResult(3, intent);
            finish();
        }
        else if(v ==ModifyB){
            intent.putExtra("name", nameEt.getText().toString());
            intent.putExtra("location", locationEt.getText().toString());
            setResult(2, intent);
            finish();
        }
    }
}
