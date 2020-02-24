package com.example.yy.chiprreader.play;

import android.annotation.TargetApi;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.yy.chiprreader.utils.SignalProc;

import java.io.DataInputStream;

public class AudioPlayer implements IAudioPlayer{

    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int PLAY_CHANNELS = AudioFormat.CHANNEL_OUT_STEREO;
    private AudioTrack audioTrack;
    private int bufferSizeInBytes;

    private volatile PlayStatus status = PlayStatus.PLAY_NO_READY;

    private byte[] headChirp;
    private byte[] chirp;
    private int fs;
    private double T;
    private int fmin;
    private int fmax;


    public AudioPlayer(int fs,double T,int fmin,int fmax){
        this.fs = fs;
        Log.d("player","fs:" + fs);
        this.T = T;
        this.fmin = fmin;
        this.fmax = fmax;
        headChirp = SignalProc.upChirpForPlay(fs,fmin,fmax,0.01);
        chirp = SignalProc.upChirpForPlay(fs,fmin,fmax,T);
        init();
    }

    private void init(){
        bufferSizeInBytes = AudioTrack.getMinBufferSize(fs,PLAY_CHANNELS,AUDIO_FORMAT);
        //bufferSizeInBytes = bufferSizeInBytes * 2;
        Log.d("player","buffer size:" + bufferSizeInBytes);
        if (bufferSizeInBytes <= 0){
            throw new IllegalStateException("AudioTrack is not available " + bufferSizeInBytes);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            audioTrack = new AudioTrack.Builder()
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(fs)
                        .setChannelMask(PLAY_CHANNELS)
                        .build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build();
        }else {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,fs,PLAY_CHANNELS,
                    AUDIO_FORMAT,bufferSizeInBytes,AudioTrack.MODE_STREAM);
        }
        status = PlayStatus.PLAY_READY;
    }

    @Override
    public void startPlay() {
        if (status == PlayStatus.PLAY_NO_READY || audioTrack == null){
            throw new IllegalStateException("播放器未初始化");
        }
        if (status == PlayStatus.PLAY_START){
            throw new IllegalStateException("播放早已开始");
        }
        Log.d("player","===start===");
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                playAudioData();
            }
        }).start();
        status = PlayStatus.PLAY_START;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void playAudioData(){
        byte[] bytes = new byte[bufferSizeInBytes];
        int len;
        audioTrack.play();
        /*float[] buffer = new float[bufferSizeInBytes];//16000
        int index = 0;
        for (;index<headChirp.length;index++){
            buffer[index] = headChirp[index];
        }
        audioTrack.write(buffer,0,headChirp.length,AudioTrack.WRITE_BLOCKING);*/


        for (int i = 0;i<100;i++){
            audioTrack.write(headChirp,0,headChirp.length,AudioTrack.WRITE_BLOCKING);
        }

        int cnt = 0;
        while (status == PlayStatus.PLAY_START){
            audioTrack.write(chirp,0,chirp.length,AudioTrack.WRITE_BLOCKING);
        }
    }



    @Override
    public void finishPlay() {
        if (status != PlayStatus.PLAY_START){
            throw new IllegalStateException("播放尚未开始");
        }else {
            audioTrack.stop();
            status = PlayStatus.PLAY_STOP;
            if (audioTrack != null){
                audioTrack.release();
                audioTrack = null;
            }
            status = PlayStatus.PLAY_NO_READY;
        }
    }

    @Override
    public boolean isPlaying() {
        return status == PlayStatus.PLAY_START;
    }
}
