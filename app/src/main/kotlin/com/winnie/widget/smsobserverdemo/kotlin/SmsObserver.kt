package com.winnie.widget.smsobserverdemo.kotlin

import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Log
import java.util.regex.Pattern

/**
 * Created by winnie on 2018/5/25.
 * 观察者模式
 */

class SmsObserver(private val handler: Handler, private val context: Context, compileValueLength: Int) : ContentObserver(handler) {
    private val compileValue: String

    init {
        this.compileValue = "\\d{$compileValueLength}"
    }

    // falseUri:content://sms/1
    // falseUri:content://sms/raw
    // 收到短信一般来说都是执行了两次onchange方法.第一次一般都是raw的这个.这个时候虽然收到了短信.但是短信还没有写入到收件箱里面
    // 然后才是另外一个,后面的数字是该短信在收件箱中的位置
    override fun onChange(selfChange: Boolean, uri: Uri) {
        super.onChange(selfChange, uri)
        Log.e("smsobserver", "selfChange:" + selfChange + "Uri:" + uri.toString())

        if (uri.toString() == "content://sms/raw") {
            return
        }

        var cursor: Cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                var address: String = cursor.getString(cursor.getColumnIndex("address"))
                var body: String = cursor.getString(cursor.getColumnIndex("body"))
                cursor.close()


                // 在这里我们的短信提供商的号码如果是固定的话.我们可以再加一个判断,这样就不会受到别的短信应用的验证码的影响了
                // 不然的话就在我们的正则表达式中,加一些自己的判断,例如短信中含有自己应用名字啊什么的...
                if (!body.contains("猪八戒网")) {
                    return;
                }

                // 正则表达式的使用,从一段字符串中取出六位连续的数字
                var pattern = Pattern.compile(compileValue)
                var matcher = pattern.matcher(body)
                if(matcher.find()){
                    // String
                    Log.e("smsobserver", "code:" + matcher.group(0))
                    Log.e("smsobserver", "contentObserver get code time:" + System.currentTimeMillis())

                    // 利用handler将得到的验证码发送给主界面
                    var msg = Message.obtain()
                    msg.what = MainActivity.received_code
                    msg.obj = matcher.group(0)
                    handler.sendMessage(msg)
                }

            } else {
                Log.e("smsobserver", "movetofirst为false了")
            }

        } else {
            Log.e("smsobserver", "cursor为null了")
        }
    }

}
