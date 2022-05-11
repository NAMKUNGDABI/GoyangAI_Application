package com.example.goyang_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhotoActivity extends AppCompatActivity implements Runnable{
    final private static String TAG = "camTage";
    Button cam_btn, al_btn, inv_btn;
    ImageView imageView;

    Uri fileUri;
    private Bitmap mBitmap = null;
    private ResultView mResultView;

    String CamPicturePath;
    final static int REQUEST_TAKE_PICTURE = 1;

    private Module mModule = null;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        cam_btn = findViewById(R.id.camera);
        al_btn = findViewById(R.id.album);
        imageView = findViewById(R.id.imageView);
        inv_btn = findViewById(R.id.inv);
        mResultView = findViewById(R.id.resultView);


        // 저장소 이용 권한 확인
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(PhotoActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        // 카메라 버튼
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

        // 앨범 버튼
        al_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(gallery,1111);
            }
        });

        // 분석 버튼
        inv_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageView.getDrawable()==null){
                    Toast.makeText(getApplicationContext(),"사진을 올려주세요",Toast.LENGTH_LONG).show();
                }
                else{
                    mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                    mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                    mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)mResultView.getWidth() / mBitmap.getWidth() : (float)mResultView.getHeight() / mBitmap.getHeight());
                    mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)mResultView.getHeight() / mBitmap.getHeight() : (float)mResultView.getWidth() / mBitmap.getWidth());

                    mStartX = (mResultView.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                    mStartY = (mResultView.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;

                    Thread thread = new Thread(PhotoActivity.this);
                    thread.start();

                }

            }
        });


        try {
            mModule = LiteModuleLoader.load(PhotoActivity.assetFilePath(getApplicationContext(), "best.torchscript.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

    }


    // 카메라, 앨범 사용시 저장소 이용 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    // 사진 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            //갤러리부분
            if(requestCode==1111){
                if(resultCode==RESULT_OK){
                    fileUri = intent.getData();
                    try{
                        ContentResolver resolver = getContentResolver();
                        InputStream inputStream = resolver.openInputStream(fileUri);
                        mBitmap = BitmapFactory.decodeStream(inputStream);
                        imageView.setImageBitmap(mBitmap);
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                //카메라부분
                if (resultCode == RESULT_OK) {
                    File file = new File(CamPicturePath);
                    fileUri=Uri.fromFile(file);
                    ImageDecoder.Source source = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        source = ImageDecoder.createSource(getContentResolver(), Uri.fromFile(file));
                    }
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            mBitmap = ImageDecoder.decodeBitmap(source);
                        }
                        if (mBitmap != null) {
                            imageView.setImageBitmap(mBitmap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

    // 파일 경로 return
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);

        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();

        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);

        Intent intent = new Intent(getApplicationContext(),ResultActivity.class);
        intent.putExtra("uri",fileUri);
        intent.putParcelableArrayListExtra("result",results);
        startActivity(intent);

    }


}