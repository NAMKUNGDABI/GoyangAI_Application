package com.example.goyang_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button predict_btn, loc_btn;
    private Spinner spinnerGu, spinnerDong;
    private ArrayAdapter<String> arrayAdapter;
    private TextView cur_loc;

    public static final String EXTRA_ADDRESS = "address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, (String[])getResources().getStringArray(R.array.goyangsi));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerGu = (Spinner)findViewById(R.id.loc_gu);
        spinnerGu.setAdapter(arrayAdapter);
        spinnerDong = (Spinner)findViewById(R.id.loc_dong);

        initAddressSpinner();

        loc_btn = findViewById(R.id.save_loc);
        loc_btn.setOnClickListener(this);

        predict_btn = findViewById(R.id.predict);
        predict_btn.setOnClickListener(this);

        cur_loc = findViewById(R.id.cur_loc);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.save_loc) {
            String address = "고양시 " + spinnerGu.getSelectedItem().toString() + " " + spinnerDong.getSelectedItem().toString();
            cur_loc.setText("현재 위치 : " + address);

        }
        else if(view.getId() == R.id.predict){
            Intent intent = new Intent(getApplicationContext(),PhotoActivity.class);
            startActivity(intent);
        }
    }

    private void initAddressSpinner() {
        spinnerGu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setRegionSpinnerAdapterItem(R.array.duckyanggu);
                        break;
                    case 1:
                        setRegionSpinnerAdapterItem(R.array.ilsanseogu);
                        break;
                    case 2:
                        setRegionSpinnerAdapterItem(R.array.ilsandonggu);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

    }

    private void setRegionSpinnerAdapterItem(int array_resource) {
        if (arrayAdapter != null) {
            spinnerDong.setAdapter(null);
            arrayAdapter = null;
        }

        if (spinnerGu.getSelectedItemPosition() > 1) {
            spinnerDong.setAdapter(null);
        }

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, (String[])getResources().getStringArray(array_resource));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDong.setAdapter(arrayAdapter);
    }

}