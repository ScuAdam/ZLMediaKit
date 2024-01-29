package com.zlmediakit.demo;

import java.io.Serializable;

enum VideoFormat implements Serializable {
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