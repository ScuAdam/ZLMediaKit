package com.zlmediakit.demo;

import static com.zlmediakit.demo.MainActivity.TAG_STREAMING_URL;
import static com.zlmediakit.demo.MainActivity.TAG_VIDEO_FORMAT;

import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zlmediakit.jni.ZLMediaKit;

import java.io.IOException;
import java.nio.ByteBuffer;

public class StreamingActivity extends AppCompatActivity {
    public static final String TAG = "ZLMediaKit";
    private ZLMediaKit.MediaPlayer mediaPlayer;
    private MediaCodec mediaCodec;
    private VideoFormat videoFormat = VideoFormat.H264;
    private String strStreamingUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        SurfaceView surfaceView = findViewById(R.id.surfaceview);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(TAG_VIDEO_FORMAT)) {
            videoFormat = (VideoFormat) intent.getSerializableExtra(TAG_VIDEO_FORMAT);
            strStreamingUrl = intent.getStringExtra(TAG_STREAMING_URL);
        }
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mediaCodec = MediaCodec.createDecoderByType(videoFormat.getVideoFormat());
                    MediaFormat mediaFormat = MediaFormat.createVideoFormat(videoFormat.getVideoFormat(), 1920, 1080);
//                     mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 480*720);
//                    mediaFormat.setInteger(KEY_FRAME_RATE,60);
                    mediaCodec.configure(mediaFormat, surfaceHolder.getSurface(), null, 0);
                    mediaCodec.start();
                    startPlayer(strStreamingUrl, videoFormat);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        });
    }
    private void startPlayer(String url, final VideoFormat videoFormat) {
        mediaPlayer = new ZLMediaKit.MediaPlayer(url, new ZLMediaKit.MediaPlayerCallBack() {
            @Override
            public void onPlayResult(int code, String msg) {
                Log.d(TAG, "onPlayResult:" + code + "," + msg);
            }

            @Override
            public void onShutdown(int code, String msg) {
                Log.d(TAG, "onShutdown:" + code + "," + msg);
            }

            @Override
            public void onData(ZLMediaKit.MediaFrame frame) {
                Log.d(TAG, "onData:"
                        + "trackType: " + frame.trackType + ",\n"
                        + "codecId 编码类型: " + frame.codecId + ",\n"
                        + "dts 解码时间戳: " + frame.dts + ",\n"
                        + "pts 显示时间戳: " + frame.pts + ",\n"
                        + "当前 显示时间戳: " + System.currentTimeMillis() + ",\n"
                        + "keyFrame 是否为关键帧: " + frame.keyFrame + ",\n"
                        + "prefixSize 前缀长度: " + frame.prefixSize + ",\n"
                        + "data.length: " + frame.data.length + "\n"
                );
                try {
                    long getFrameTimeMs = System.currentTimeMillis();
                    if (frame.codecId == videoFormat.getCodecId() && frame.trackType == 0) {
                        if (frame.keyFrame) {
                            mediaCodec.flush();
                        }
                        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                        int inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
                        Log.d(TAG, "inputBufferIndex= " + inputBufferIndex);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            // 将视频帧数据填充到 inputBuffer 中
                            inputBuffer.put(frame.data);
                            mediaCodec.queueInputBuffer(inputBufferIndex, 0, frame.data.length, 0, 0);
                        }
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                        Log.d(TAG, "outputBufferIndex= " + outputBufferIndex);
                        if (outputBufferIndex >= 0) {
                            mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                            long totalUseMs = System.currentTimeMillis() - getFrameTimeMs;
                            Log.d(TAG, "totalusems = " + totalUseMs);
                        }
                    }
                } catch (Exception e) {
//                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        mediaCodec.stop();
        mediaCodec.release();
    }
}