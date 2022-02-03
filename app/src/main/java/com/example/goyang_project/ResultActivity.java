package com.example.goyang_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {


    private ImageView image;
    private Button moreInfo;

    private Bitmap mBitmap = null;
    private ResultView mResultView;
    private ArrayList<Result> result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels * 0.9);
        getWindow().getAttributes().width = width;
        int height = (int) (dm.heightPixels * 0.8);
        getWindow().getAttributes().height = height;

        moreInfo = findViewById(R.id.moreInfo);
        image = findViewById(R.id.resultImage);
        mResultView = findViewById(R.id.resultView);

        Uri saveImage = getIntent().getParcelableExtra("uri");
        result = getIntent().getParcelableArrayListExtra("result");

        ContentResolver resolver = getContentResolver();

        try{
            InputStream inputStream = resolver.openInputStream(saveImage);
            mBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        image.setImageBitmap(mBitmap);
        mResultView.setResults(result);

    }

}