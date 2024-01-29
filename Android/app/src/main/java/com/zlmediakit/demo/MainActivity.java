package com.zlmediakit.demo;

import static android.media.MediaFormat.KEY_FRAME_RATE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.zlmediakit.jni.ZLMediaKit;

import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ZLMediaKit";
    public static final String TAG_VIDEO_FORMAT = "TAG_VIDEO_FORMAT";
    public static final String TAG_STREAMING_URL = "TAG_STREAMING_URL";
    private EditText etUrl;
    private RadioGroup radioGroup;
    private RadioButton radioH264, radioH265;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUrl = findViewById(R.id.sample_text);
        radioGroup = findViewById(R.id.radioGroup);
        radioH264 = findViewById(R.id.radioH264);
        radioH265 = findViewById(R.id.radioH265);

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoFormat videoFormat;
                int selectedRadioId = radioGroup.getCheckedRadioButtonId();
                if (selectedRadioId == radioH264.getId()) {
                    videoFormat = VideoFormat.H264;
                } else if (selectedRadioId == radioH265.getId()) {
                    videoFormat = VideoFormat.H265;
                } else {
                    videoFormat = VideoFormat.H264;
                }
                Intent intent = new Intent(MainActivity.this, StreamingActivity.class);
                intent.putExtra(TAG_VIDEO_FORMAT, videoFormat);
                intent.putExtra(TAG_STREAMING_URL, etUrl.getText().toString());
                startActivity(intent);
            }
        });
    }
}
