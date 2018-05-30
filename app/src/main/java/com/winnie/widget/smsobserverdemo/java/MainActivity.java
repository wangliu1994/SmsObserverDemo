package com.winnie.widget.smsobserverdemo.java;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.winnie.widget.smsobserverdemo.R;
import com.winnie.widget.smsobserverdemo.kotlin.SmsObserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    public static final int received_code = 1001;
    private static final int permission_code = 1002;

    private TextView codeView;
    private SmsObserver smsObserver;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            codeView.setText((String) msg.obj);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == permission_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
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
            init();
        }
    }

    private void init() {
        smsObserver = new SmsObserver(handler, this, 6);
        Uri uri = Uri.parse("content://sms");

        /**
         * notifyForDescendents：如果为true表示以这个Uri为开头的所有Uri都会被匹配到，
         * 如果为false表示精确匹配，即只会匹配这个给定的Uri。
         */
        getContentResolver().registerContentObserver(uri, true, smsObserver);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = getContentResolver().query(Uri.parse("content://sms/2687"), null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    if (!cursor.isAfterLast()) {
                        String address = cursor.getString(cursor.getColumnIndex("address"));
                        String body = cursor.getString(cursor.getColumnIndex("body"));
                        Log.e("smsobserver", "get sms:address:" + address + "body:" + body);
                        cursor.close();// 最后用完游标千万记得关闭

                        // 在这里我们的短信提供商的号码如果是固定的话.我们可以再加一个判断,这样就不会受到别的短信应用的验证码的影响了
                        // 不然的话就在我们的正则表达式中,加一些自己的判断,例如短信中含有自己应用名字啊什么的...
                        if (!body.contains("猪八戒网")) {
                            return;
                        }

                        // 正则表达式的使用,从一段字符串中取出六位连续的数字
                        Pattern pattern = Pattern.compile("\\d6}");
                        Matcher matcher = pattern.matcher(body);
                        if (matcher.find()) {
                            // String
                            Log.e("smsobserver", "code:" + matcher.group(0));
                            Log.e("smsobserver", "contentObserver get code time:" + System.currentTimeMillis());

                            // 利用handler将得到的验证码发送给主界面
                            Message msg = Message.obtain();
                            msg.what = MainActivity.received_code;
                            msg.obj = matcher.group(0);
                            handler.sendMessage(msg);
                        } else {
                            Log.e("smsobserver", "没有在短信中获取到合格的验证码");
                        }
                    } else {
                        Log.e("smsobserver", "movetofirst为false了");
                    }
                }
            }
        }).run();
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
    }
}
