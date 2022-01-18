package com.example.goyang_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoActivity extends AppCompatActivity {
    final private static String TAG = "camTage";
    Button cam_btn;
    Button al_btn;

    final static int TAKE_PICTURE = 1;

    String CamPicturePath;
    final static int REQUEST_TAKE_PICTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        cam_btn = findViewById(R.id.camera);
        al_btn = findViewById(R.id.album);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            } else { Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(PhotoActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        cam_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.camera:
                        dispatchTakePicture();
                        break;
                }

            }
        });

        al_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(gallery,1111);
            }
        });


    }

    // 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    // 카메라로 촬영한 사진을 가져오는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            //갤러리부분
            if(requestCode==1111){
                if(resultCode==RESULT_OK){
                    Uri fileUri = intent.getData();
                    Intent mainToRe = new Intent(PhotoActivity.this,ResultActivity.class);
                    mainToRe.putExtra("image",fileUri);
                    startActivityForResult(mainToRe,2222);

                }
            }
            else{
                //카메라부분
                if (resultCode == RESULT_OK) {
                    File file = new File(CamPicturePath);
                    Intent mainToRe = new Intent(PhotoActivity.this,ResultActivity.class);
                    mainToRe.putExtra("image",file);
                    startActivityForResult(mainToRe,3333);
                    //          Bitmap bitmap;
                    //              ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
                    //            try { bitmap = ImageDecoder.decodeBitmap(source);
                    //              if (bitmap != null) { iv_photo.setImageBitmap(bitmap); }
                    //        } catch (IOException e) {
                    //          e.printStackTrace();
                    //    }
                }

            }
        }
        catch (Exception error) {
            error.printStackTrace();
        }

    }

    // 사진 촬영 후 이미지를 파일로 저장
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile( imageFileName, ".jpg", storageDir );
        CamPicturePath = image.getAbsolutePath();
        return image;
    }

    // 카메라 인텐트 실행하는 부분
    private void dispatchTakePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) { }
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.goyang_project.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PICTURE); }
        }
    }


}