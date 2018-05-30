package com.winnie.widget.smsobserverdemo.java;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // 降序查询我们的数据库,原作者代码竟然uri是"content://sms/inbox",而且还加了个查询条件(时间降序..)..感觉有点多此一举..
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
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
                Pattern pattern = Pattern.compile(compileValue);
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
        } else {
            Log.e("smsobserver", "cursor为null了");
        }
    }

}
