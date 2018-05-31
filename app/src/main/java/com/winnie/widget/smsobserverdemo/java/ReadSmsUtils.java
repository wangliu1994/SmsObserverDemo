package com.winnie.widget.smsobserverdemo.java;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by winnie on 2018/5/31.
 */

public class ReadSmsUtils {

    public static void readSms(Context context, Uri uri, String compileValue, Handler handler){
        Cursor cursor  = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                cursor.close();


                // 在这里我们的短信提供商的号码如果是固定的话.我们可以再加一个判断,这样就不会受到别的短信应用的验证码的影响了
                // 不然的话就在我们的正则表达式中,加一些自己的判断,例如短信中含有自己应用名字啊什么的...
                if (!body.contains("猪八戒网")) {
                    return;
                }

                // 正则表达式的使用,从一段字符串中取出六位连续的数字
                Pattern pattern = Pattern.compile(compileValue);
                Matcher matcher = pattern.matcher(body);
                if(matcher.find()){
                    // String
                    Log.e("smsobserver", "code:" + matcher.group(0));
                    Log.e("smsobserver", "contentObserver get code time:" + System.currentTimeMillis());

                    // 利用handler将得到的验证码发送给主界面
                    Message msg = Message.obtain();
                    msg.what = MainActivity.received_code;
                    msg.obj = matcher.group(0);
                    handler.sendMessage(msg);
                }

            } else {
                Log.e("smsobserver", "movetofirst为false了");
            }

        } else {
            Log.e("smsobserver", "cursor为null了");
        }
    }
}
