package com.firs.facedetecttosvr.getidcardinfor;

import android.app.Activity;
import android.os.Bundle;

import com.firs.facedetecttosvr.R;

/**
 * 1、使用蓝牙方式（外接一个蓝牙设备），来获取身份证上的信息和人脸图片
 * 2、抓拍人脸
 * 3、将身份证上的人脸和抓拍到的人脸进行识别
 */
public class BluetoothActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
    }
}
