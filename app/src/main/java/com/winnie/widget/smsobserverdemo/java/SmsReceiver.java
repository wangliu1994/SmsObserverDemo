package com.winnie.widget.smsobserverdemo.java;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by winnie on 2018/5/31.
 */

public class SmsReceiver extends BroadcastReceiver {

    private Context context;
    private Handler handler;
    private String compileValue;

    public SmsReceiver(Context context, Handler handler, int compileLength) {
        this.context = context;
        this.handler = handler;
        this.compileValue = "\\d{" + compileLength + "}";
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        Object[] objects = (Object[]) bundle.get("pdus");
        if(objects != null){
            String phone = null;
            StringBuilder body = new StringBuilder();
            for(int i = 0; i< objects.length; i++){
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) objects[i]);
                phone = smsMessage.getDisplayOriginatingAddress();
                body.append(smsMessage.getDisplayMessageBody());
            }
            Log.e("smsbc", "phone:" + phone + "\ncontent:" + body.toString());
            checkSmsAndSend(body.toString());
        }
    }

    private void checkSmsAndSend(String body){

        // 正则表达式的使用,从一段字符串中取出六位连续的数字
        Pattern pattern = Pattern.compile(compileValue);
        Matcher matcher = pattern.matcher(body);
        if (matcher.find()) {
            String obj = matcher.group(0);
            Log.e("smsbc", "短信中找到了符合规则的验证码:" + obj);
            Message msg = Message.obtain();
            msg.what = MainActivity.received_code;
            msg.obj = obj;
            handler.sendMessage(msg);
            Log.e("smsbc", "广播接收器接收到短信的时间:" + System.currentTimeMillis());
        } else {
            Log.e("smsbc", "短信中没有找到符合规则的验证码");
        }
    }
}
