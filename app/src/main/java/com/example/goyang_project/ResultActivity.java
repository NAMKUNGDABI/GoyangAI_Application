package com.example.goyang_project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ResultActivity extends AppCompatActivity {

    ImageView image;
    TextView info;
    Button moreInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        image = findViewById(R.id.resultImage);
        info = findViewById(R.id.information);
        moreInfo = findViewById(R.id.moreInfo);

        //각 모델에 따른 input , output shape 각자 맞게 변환
        // mobilenetcheck.h5 일시 224 * 224 * 3
        float[][][][] input = new float[1][640][640][3];
        float[][][] output = new float[1][27][25200]; //tflite에 버섯 종류 5개라서 (내기준)


        Uri saveImage = getIntent().getParcelableExtra("image");
        ContentResolver resolver = getContentResolver();

        try{
            int batchNum=0;

            InputStream inputStream = resolver.openInputStream(saveImage);
            Bitmap imgBit = BitmapFactory.decodeStream(inputStream);
            //image.setImageBitmap(imgBit);
            inputStream.close();


            // x,y 최댓값 사진 크기에 따라 달라짐 (조절 해줘야함)
            for (int x = 0; x < 640; x++) {
                for (int y = 0; y < 640; y++) {
                    int pixel = imgBit.getPixel(x, y);
                    input[batchNum][x][y][0] = Color.red(pixel) / 1.0f;
                    input[batchNum][x][y][1] = Color.green(pixel) / 1.0f;
                    input[batchNum][x][y][2] = Color.blue(pixel) / 1.0f;
                }
            }

            // 자신의 tflite 이름 써주기
            Interpreter lite = getTfliteInterpreter("best-fp16.tflite");
            lite.run(input, output);

            image.setScaleType(ImageView.ScaleType.FIT_XY);
            image.setImageBitmap(imgBit);




        } catch (IOException e) {
            e.printStackTrace();
        }


        int i;

        // 텍스트뷰에 무슨 버섯인지 띄우기 but error남 ㅜㅜ 붉은 사슴뿔만 주구장창
        /*for (i = 0; i < 22; i++) {
            if (output[0][i] * 100 > 90) {
                info.setText(String.format("결과 : %d   정확도 : %.5f", i, output[0][i] * 100));
            } else
                continue;
        }*/


    }

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(ResultActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    public MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}