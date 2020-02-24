package com.example.yy.chiprreader.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yy.chiprreader.model.Param;
import com.example.yy.chiprreader.R;
import com.example.yy.chiprreader.utils.SPUtil;
import com.example.yy.chiprreader.utils.Test;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText fminInput;
    private EditText fmaxInput;
    private EditText BInput;
    private EditText fsInput;
    private EditText TInput;
    private EditText micInput;
    private Button confirmBtn;

    private static final String FMIN_KEY = "fmin";
    private static final String FMAX_KEY = "fmax";
    private static final String B_KEY = "b";
    private static final String FS_KEY = "fs";
    private static final String T_KEY = "t";
    private static final String MIC_KEY = "mic";

    private static final String FMIN_FILE = "fmin";
    private static final String FMAX_FILE = "fmax";
    private static final String B_FILE = "b";
    private static final String FS_FILE = "fs";
    private static final String T_FILE = "t";
    private static final String MIC_FILE = "mic";

    private Param param;

    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    List<String> permissionsNotPass;
    private static final int PERMISSION_RequestCode = 100;//权限请求码

    private static final String TAG = "ParamConfig";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermissions();

        initView();
        initData();


    }

    private void initView(){
        fminInput = findViewById(R.id.fmin_btn);
        fmaxInput = findViewById(R.id.fmax_btn);
        BInput = findViewById(R.id.B_btn);
        TInput = findViewById(R.id.T_btn);
        fsInput = findViewById(R.id.fs_btn);
        micInput = findViewById(R.id.mic_btn);
        confirmBtn = findViewById(R.id.okBtn);

        confirmBtn.setOnClickListener(this);
    }

    private void initData(){
        String fmin = SPUtil.getString(this,FMIN_FILE,FMIN_KEY,"0");
        fminInput.setText(fmin);
        String fmax = SPUtil.getString(this,FMAX_FILE,FMAX_KEY,"0");
        fmaxInput.setText(fmax);
        String T = SPUtil.getString(this,T_FILE,T_KEY,"0.04");
        TInput.setText(T);
        String fs = SPUtil.getString(this,FS_FILE,FS_KEY,"0");
        fsInput.setText(fs);
        String B = SPUtil.getString(this,B_FILE,B_KEY,"0");
        BInput.setText(B);
        String micDist = SPUtil.getString(this,MIC_FILE,MIC_KEY,"0.18");
        micInput.setText(micDist);
    }

    private void saveData(){
        String fmin = fminInput.getText().toString();
        SPUtil.putString(this,FMIN_FILE,FMIN_KEY,fmin);

        String fmax = fmaxInput.getText().toString();
        SPUtil.putString(this,FMAX_FILE,FMAX_KEY,fmax);

        String T = TInput.getText().toString();
        SPUtil.putString(this,T_FILE,T_KEY,T);

        String fs = fsInput.getText().toString();
        SPUtil.putString(this,FS_FILE,FS_KEY,fs);

        String B = BInput.getText().toString();
        SPUtil.putString(this,B_FILE,B_KEY,B);

        String micDist = micInput.getText().toString();
        SPUtil.putString(this,MIC_FILE,MIC_KEY,micDist);

        param = new Param();

        param.setFmax(Integer.parseInt(fmax));
        param.setFmin(Integer.parseInt(fmin));
        param.setB(Integer.parseInt(B));
        param.setT(Float.parseFloat(T));
        param.setFs(Integer.parseInt(fs));
        param.setMicDist(Float.parseFloat(micDist));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.okBtn:{
                try {
                    saveData();
                }catch (Exception e){
                    Toast.makeText(this,"请输入正确格式的参数",Toast.LENGTH_SHORT).show();
                    break;
                }
                Intent intent = new Intent(MainActivity.this,RecordActivity.class);
                Gson gson = new Gson();
                String paramJson = gson.toJson(param);
                intent.putExtra("param",paramJson);
                startActivity(intent);
                finish();
                break;
            }
        }
    }

    //权限处理
    private void initPermissions(){
        permissionsNotPass = new ArrayList<>();
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                permissionsNotPass.add(permissions[i]);//添加还未授予的权限
            }
        }
        //申请权限
        if (permissionsNotPass.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_RequestCode);
        }else{
            //说明权限都已经通过，可以做你想做的事情去
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean hasPermissionDismiss = false;//有权限没有通过
        if (PERMISSION_RequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                finish();
            }
        }
    }
}
