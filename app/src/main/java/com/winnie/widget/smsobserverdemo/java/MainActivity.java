package com.winnie.widget.smsobserverdemo.java;

import android.Manifest;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.winnie.widget.smsobserverdemo.R;
import com.winnie.widget.smsobserverdemo.kotlin.ReadSmsUtils;
import com.winnie.widget.smsobserverdemo.kotlin.SmsObserver;


public class MainActivity extends AppCompatActivity {

    public static final int received_code = 1001;
    private static final int permission_code = 1002;

    private TextView codeView;
    private SmsObserver smsObserver;

    private SmsReceiver smsReceiver;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            codeView.setText((String) msg.obj);
            Log.e("mainActivity", "activity get code time:" + System.currentTimeMillis());
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permission_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSmsObserver();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                    showPermissionDialog();
                } else {
                    Toast.makeText(this, "必须开启权限才可使用该功能", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        codeView = findViewById(R.id.verify_code);

        requestPermissions();
    }

    /**
     * 动态监测权限
     */
    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                showPermissionDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, permission_code);
            }
        } else {
            initSmsObserver();
        }
    }

    private void initSmsObserver() {
        smsObserver = new SmsObserver(handler, this, 6);
        Uri uri = Uri.parse("content://sms");

        /**
         * notifyForDescendents：如果为true表示以这个Uri为开头的所有Uri都会被匹配到，
         * 如果为false表示精确匹配，即只会匹配这个给定的Uri。
         */
        getContentResolver().registerContentObserver(uri, true, smsObserver);

        testReadSms();
    }

    private void testReadSms(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReadSmsUtils.Utils.readSms(MainActivity.this, Uri.parse("content://sms/1"), "\\d{6}", handler);
            }
        }).run();
    }


    private void initSmsReceiver(){
        smsReceiver = new SmsReceiver(this, handler, 6);
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }

    /**
     * 权限引导弹窗
     */

    private void showPermissionDialog() {
        new AlertDialog
                .Builder(this)
                .setTitle("获取短信权")
                .setMessage("我们需要获取短信权限，才能读取短信验证码")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, permission_code);
            }
        }).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(smsObserver);
        unregisterReceiver(smsReceiver);
    }
}
