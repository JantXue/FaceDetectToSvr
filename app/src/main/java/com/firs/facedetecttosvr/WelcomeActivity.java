package com.firs.facedetecttosvr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.firs.cn.FaceNative;

public class WelcomeActivity extends Activity {
    private String name = "test", pwd = "123456";
    private boolean mISSavePassword = false;//是否有刷身份证

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // 隐藏状态栏
        setContentView(R.layout.activity_welcome);
        initDate();
    }

    private void initDate() {
        SharedPreferences userAccount = getSharedPreferences("useraccount", this.MODE_PRIVATE);
        String saveflag = userAccount.getString("saveflag", "0");
        mISSavePassword = saveflag.equals("0");

        SharedPreferences serverSettings = getSharedPreferences("serversettings", this.MODE_PRIVATE);
        String serverip = serverSettings.getString("serverip", "116.205.1.86");
        int port = serverSettings.getInt("port", 32108);
        FaceNative.SetServerIP(serverip.getBytes(), port, 0);//连接服务器

        SharedPreferences sharedPreferences3 = getSharedPreferences("setting", this.MODE_PRIVATE);
        int sorce = Integer.valueOf(sharedPreferences3.getString("score", "60"));
        FaceNative.SetScore(sorce);//链接服务器
        //感知刷身份证
        loginService();
    }

    /**
     * 测试使用登陆账号
     */
    private void loginService() {
        FaceNative.UserAuth(name.toString().getBytes(), pwd.toString().getBytes());//进行登录
        //Toast.makeText(getApplicationContext(), "====="+FaceNative.getAuth(), Toast.LENGTH_SHORT).show();
        new Thread() {
            @Override
            public void run() {
                long start_time = System.currentTimeMillis() / 1000;
                while (true) {
                    //认证成功
                    if (FaceNative.getAuth() == 1) {
                        break;
                    }
                    if (FaceNative.getAuth() == 2) {
                        mISSavePassword = false;
                        break;
                    }

                    if (System.currentTimeMillis() / 1000 - start_time >= 30) {
                        mISSavePassword = false;
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }.start();

    }

    private Handler handler = new Handler() {
        Toast toast;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if ((mISSavePassword)) {
                        SharedPreferences settings = getSharedPreferences("useraccount", WelcomeActivity.MODE_PRIVATE);
                        Editor editor = settings.edit();//获取编辑器
                        editor.putString("account", name.toString());
                        editor.putString("pwd", pwd.toString());
                        editor.putString("saveflag", "0");
                        editor.commit();//提交修改
                    }
                    if (!mISSavePassword) {
                        SharedPreferences settings = getSharedPreferences("useraccount", WelcomeActivity.MODE_PRIVATE);
                        Editor editor = settings.edit();//获取编辑器
                        editor.putString("account", name.toString());
                        editor.putString("pwd", "");
                        editor.putString("saveflag", "1");
                        editor.commit();//提交修改
                    }
                    if (FaceNative.getAuth() == 1) {
                        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                        WelcomeActivity.this.finish();
                    } else {
                        if (FaceNative.getAuth() == -1) {
                            toast = Toast.makeText(getApplicationContext(), "登录失败!用户名和密码不匹配。", Toast.LENGTH_SHORT);
                        } else if (FaceNative.getAuth() == 2) {
                            toast = Toast.makeText(getApplicationContext(), "登录超时失败!请重新登录。", Toast.LENGTH_SHORT);
                        } else {
                            toast = Toast.makeText(getApplicationContext(), "服务器连接失败!请重新登录。", Toast.LENGTH_SHORT);
                        }
                    }
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        FaceNative.initTcp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (false == mISSavePassword) {
            FaceNative.setThreadExit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
