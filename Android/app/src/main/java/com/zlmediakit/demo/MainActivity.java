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

enum VideoFormat{
    H264("video/avc", 0),
    H265("video/hevc", 1);

    private final String videoFormat;
    private final int codecId;

    VideoFormat(String videoFormat, int codecId) {
        this.videoFormat = videoFormat;
        this.codecId = codecId;
    }

    public String getVideoFormat() {
        return videoFormat;
    }

    public int getCodecId() {
        return codecId;
    }
}
public class MainActivity extends AppCompatActivity {
    public static final String TAG = "ZLMediaKit";
    public static final String FORMAT_H264 = "FORMAT_H264";
    public static final String FORMAT_H265 = "FORMAT_H265";
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.INTERNET"};


    private EditText etUrl;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private RadioGroup radioGroup;
    private RadioButton radioH264,radioH265;
    private MediaCodec mediaCodec;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUrl = findViewById(R.id.sample_text);
        surfaceView = findViewById(R.id.surfaceview);
        radioGroup = findViewById(R.id.radioGroup);
        radioH264 = findViewById(R.id.radioH264);
        radioH265 = findViewById(R.id.radioH265);

        surfaceHolder = surfaceView.getHolder();

        boolean permissionSuccess = true;
        for(String str : PERMISSIONS_STORAGE){
            int permission = ActivityCompat.checkSelfPermission(this, str);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE,1);
                permissionSuccess = false;
                break;
            }
        }

//        String sd_dir = Environment.getExternalStoragePublicDirectory("").toString();
//        if(permissionSuccess){
//            Toast.makeText(this,"你可以修改配置文件再启动：" + sd_dir + "/zlmediakit.ini" ,Toast.LENGTH_LONG).show();
//            Toast.makeText(this,"SSL证书请放置在：" + sd_dir + "/zlmediakit.pem" ,Toast.LENGTH_LONG).show();
//        }else{
//            Toast.makeText(this,"请给予我权限，否则无法启动测试！" ,Toast.LENGTH_LONG).show();
//        }
//        ZLMediaKit.startDemo(sd_dir);
//        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(SurfaceHolder surfaceHolder) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    try {
//                        mediaCodec = MediaCodec.createDecoderByType("video/hevc");
//                        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/hevc", 480, 720);
////                        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 480*720);
////                        format.setInteger(KEY_FRAME_RATE,60);
//                        mediaCodec.configure(mediaFormat, surfaceHolder.getSurface(), null, 0);
//                        mediaCodec.start();
//                    } catch (IOException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//
//            @Override
//            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//
//            }
//        });

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
                test_player(etUrl.getText().toString(),videoFormat);
            }
        });
    }

    private ZLMediaKit.MediaPlayer _player;
    private void test_player(String url, final VideoFormat videoFormat){
        _player = new ZLMediaKit.MediaPlayer(url, new ZLMediaKit.MediaPlayerCallBack() {
            @Override
            public void onPlayResult(int code, String msg) {
                Log.d(TAG,"onPlayResult:" + code + "," + msg);
                try {
                    mediaCodec = MediaCodec.createDecoderByType(videoFormat.getVideoFormat());
                    MediaFormat mediaFormat = MediaFormat.createVideoFormat(videoFormat.getVideoFormat(), 1920, 1080);
//                        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 480*720);
                    mediaFormat.setInteger(KEY_FRAME_RATE,60);
                    mediaCodec.configure(mediaFormat, surfaceHolder.getSurface(), null, 0);
                    mediaCodec.start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onShutdown(int code, String msg) {
                Log.d(TAG,"onShutdown:" + code + "," + msg);
            }

            @Override
            public void onData(ZLMediaKit.MediaFrame frame) {
                Log.d(TAG,"onData:"
                        + "trackType: " + frame.trackType + ",\n"
                        + "codecId 编码类型: " + frame.codecId + ",\n"
                        + "dts 解码时间戳: " + frame.dts + ",\n"
                        + "pts 显示时间戳: " + frame.pts + ",\n"
                        + "当前 显示时间戳: " + System.currentTimeMillis() + ",\n"
                        + "keyFrame 是否为关键帧: "+ frame.keyFrame + ",\n"
                        + "prefixSize 前缀长度: " + frame.prefixSize + ",\n"
                        + "data.length: " + frame.data.length + "\n"
                );
                long getFrameTimeMs = System.currentTimeMillis();
                if (frame.codecId == videoFormat.getCodecId() && frame.trackType == 0){
                    if (frame.keyFrame){
                        mediaCodec.flush();
                    }
                    ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                    int inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
                    Log.d(TAG,"inputBufferIndex= " + inputBufferIndex);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                        // 将视频帧数据填充到 inputBuffer 中
                        inputBuffer.put(frame.data);
                        mediaCodec.queueInputBuffer(inputBufferIndex, 0, frame.data.length, 0, 0);
                    }
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    Log.d(TAG,"outputBufferIndex= " + outputBufferIndex);
                    if (outputBufferIndex >= 0){
                        mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                        long totalUseMs = System.currentTimeMillis() - getFrameTimeMs;
                        Log.d(TAG,"totalusems = " + totalUseMs);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _player.release();
        mediaCodec.stop();
        mediaCodec.release();
    }
}
