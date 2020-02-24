package com.example.yy.chiprreader.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.yy.chiprreader.R;
import com.example.yy.chiprreader.model.Param;
import com.example.yy.chiprreader.play.AudioPlayer;
import com.example.yy.chiprreader.process.AudioRecorder;
import com.example.yy.chiprreader.utils.Algorithm;
import com.example.yy.chiprreader.utils.SignalProc;
import com.example.yy.chiprreader.utils.Test;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener, AudioRecorder.RecordingCallback {

    private int fmin;
    private int fmax;
    private int B;
    private int fs;
    private float T;
    private float micDist;

    private Button controlBtn;
    private Button backBtn;
    private Button playBtn;

    private LineGraphSeries<DataPoint> mSeries;
    private GraphView fftView;


    private AudioRecorder mRecorder;
    private AudioPlayer mPlayer;

    //同步数据
    private LinkedList<Short> dataList = new LinkedList<>();//待处理数据集
    private float[] syncChirp;

    //处理数据
    private short[] leftChannelData;
    private short[] rightChannelData;
    private int leftIdx;
    private int rightIdx;
    private boolean firstInput = true;
    private int lag;

    private ExecutorService exec = Executors.newCachedThreadPool();

    static {
        System.loadLibrary("JNITest");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        getSupportActionBar().hide();

        getParam();//获取参数

        initView();//初始化视图

        /**************test************/
        /*Test.testFFTJni();
        Test.testMixJni();
        Test.testNormJni();
        Test.testCorr();*/

        Test.testCorr2();
    }

    private void initView() {
        controlBtn = findViewById(R.id.control_btn);
        controlBtn.setOnClickListener(this);
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(this);
        playBtn = findViewById(R.id.play_btn);
        playBtn.setOnClickListener(this);

        fftView = findViewById(R.id.graph_spectrum);
        mSeries = new LineGraphSeries<>(generateData());
        fftView.addSeries(mSeries);


    }

    private void getParam() {
        Intent intent = getIntent();
        String paramJson = intent.getStringExtra("param");
        Gson gson = new Gson();
        Param param = gson.fromJson(paramJson, Param.class);
        fmin = param.getFmin();
        fmax = param.getFmax();
        B = param.getB();
        T = param.getT();
        fs = param.getFs();
        micDist = param.getMicDist();
        syncChirp = SignalProc.upChirp(fs, fmin, fmax, 0.01f);//同步码元采用10ms

        leftChannelData = new short[(int) (fs*T)];
        rightChannelData = new short[(int) (fs*T)];

        //初始化音频处理对象
        mPlayer = new AudioPlayer(fs, T, fmin, fmax);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control_btn: {
                if (controlBtn.getText().toString().equals("开始")) {
                    controlBtn.setText("停止");
                    mRecorder = new AudioRecorder(fs);
                    mRecorder.registerCallback(this);
                    if (!mRecorder.isRecording()) {
                        mRecorder.startRecord();
                    }
                } else {
                    controlBtn.setText("开始");
                    mRecorder.finishRecord();
                    mRecorder = null;
                    //Collections.sort(mixList);
                }
                break;
            }
            case R.id.back_btn: {
                Intent intent = new Intent(RecordActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.play_btn: {
                if (playBtn.getText().toString().equals("play")) {
                    playBtn.setText("stop");
                    mPlayer.startPlay();
                } else {
                    mPlayer.finishPlay();
                    playBtn.setText("play");
                }
                break;
            }
        }
    }

    //每次来4096个
    @Override
    public void onDataReady(short[] data, int bytelen) {
        Log.d("test", "len:" + data.length);

        //syncSignal(data);

        if (!syncReady){
            syncSignal(data);
        }else {
            addData(data);
        }

        /*final float[] temp = new float[data.length];
        for (int i = 0;i<data.length;i++){
            temp[i] = data[i];
        }
        exec.execute(new Runnable() {
            @Override
            public void run() {
                float max = Algorithm.correlationJni(syncChirp,temp);
                if (max > 50000){
                    Log.d("test","max:" + max);
                }

            }
        });*/
    }

    private volatile boolean syncReady = false;
    private void syncSignal(short[] data){
        short[] temp = new short[data.length];
        System.arraycopy(data,0,temp,0,data.length);

        final short[] left = new short[temp.length/2];
        final short[] right = new short[temp.length/2];
        for (int i = 0;i<temp.length/2;i++){
            left[i] = temp[2*i];
            right[i] = temp[2*i+1];
            Log.d("test","right:" + right[i] + " left:" + left[i]);
        }
        exec.execute(new Runnable() {
            @Override
            public void run() {
                long sT = System.currentTimeMillis();
                int lag = Algorithm.correlationJni2(left,right);
                long eT = System.currentTimeMillis();
                if(lag > 16 && lag <20 && RecordActivity.this.mPlayer.isPlaying()){
                    syncReady = true;
                    RecordActivity.this.lag = Math.abs(lag);
                }
                Log.d("test","lag:" + lag + " time:" + (eT-sT));
            }
        });
    }


    private void addData(short[] data){
        short[] temp = new short[8192];
        System.arraycopy(data,0,temp,0,8192);
        for (int i = 0;i<4096;i++){
            if (firstInput){
                firstInput = false;
                if(2*i + 2*lag < 8192){
                    leftChannelData[leftIdx++] = temp[2*i+2*lag];
                }
            }else {
                leftChannelData[leftIdx++] = temp[2*i];
            }
            rightChannelData[rightIdx++] = temp[2*i+1];

            if (leftIdx >= fs*T/2 && rightIdx >= fs*T/2){
                processData();
                leftIdx = 0;
                rightIdx = 0;
            }
        }
    }

    //处理数据
    private void processData(){
        final short[] leftTemp = new short[leftChannelData.length];
        final short[] rightTemp = new short[rightChannelData.length];
        System.arraycopy(leftChannelData,0,leftTemp,0,leftChannelData.length);
        System.arraycopy(rightChannelData,0,rightTemp,0,rightChannelData.length);
        leftChannelData = new short[(int) (fs*T)];
        rightChannelData = new short[(int) (fs*T)];
        exec.execute(new Runnable() {
            @Override
            public void run() {
                //norm
                float[] leftNormed = Algorithm.normolizeArrayJni(leftTemp);//fs*T
                float[] rightNormed = Algorithm.normolizeArrayJni(rightTemp);
                //mixing
                float[] mixed = Algorithm.mixFrequenceJni(leftNormed,rightNormed);//fs*T
                //fft
                final float[] ffted = Algorithm.fftJni(mixed,leftTemp.length);
                Log.d("test","ffted len:" + ffted.length);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateView(ffted);
                        Log.d("test","updateView");
                    }
                });
            }
        });
    }

    private void updateView(float[] s) {
        DataPoint[] values = new DataPoint[s.length];
        Log.d("test","view len:" + s.length);
        for (int i = 0; i < s.length; i++) {
            float xx = i;
            float yy = s[i];
            DataPoint vv = new DataPoint(xx, yy);
            values[i] = vv;
        }
        //Log.e("record", "sample length = " + s.length);
        mSeries.resetData(values);
    }

    private DataPoint[] generateData() {
        int count = 200;
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double x = i;
            double y = Math.random() + 0.3;
            DataPoint v = new DataPoint(x, y);

            values[i] = v;
        }
        return values;
    }
}
