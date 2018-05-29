package com.winnie.widget.smsobserverdemo;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by winnie on 2018/5/25.
 * 观察者模式
 */

public class SmsObserver extends ContentObserver {

    private Context context;
    private Handler handler;
    private String compileValue;

    public SmsObserver(Handler handler, Context context, int compileValueLength) {
        super(handler);
        this.handler= handler;
        this.context = context;
        this.compileValue = "\\d{" + compileValueLength + "}";
    }

    // falseUri:content://sms/1
    // falseUri:content://sms/raw
    // 收到短信一般来说都是执行了两次onchange方法.第一次一般都是raw的这个.这个时候虽然收到了短信.但是短信还没有写入到收件箱里面
    // 然后才是另外一个,后面的数字是该短信在收件箱中的位置
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);

        if(uri.toString().equals("content://sms/raw")){
            return;
        }
    }
}
