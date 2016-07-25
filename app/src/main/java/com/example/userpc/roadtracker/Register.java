package com.example.userpc.roadtracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by USER PC on 7/3/2016.
 */
public class Register extends AppCompatActivity {

    SharedPreferences sharepref;

    public void register(View v)
    {
        EditText editText= (EditText) findViewById(R.id.editText);
        String id=editText.getText().toString();
        if(id.length()!=10)
        {
            editText.setError("Enter a valid 10 digit number");
            return;
        }
        SharedPreferences.Editor edit=sharepref.edit();
        edit.putString("id",id);
        edit.commit();
        startActivity(new Intent(Register.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        sharepref = getSharedPreferences("RoadPref",MODE_PRIVATE);
        if(sharepref.contains("id"))
        {
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }
    }
}
