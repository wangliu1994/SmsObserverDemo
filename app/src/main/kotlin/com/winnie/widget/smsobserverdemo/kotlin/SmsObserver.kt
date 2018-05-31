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

        ReadSmsUtils.readSms(context = context, uri = uri, compileValue = compileValue, handler = handler)
    }

}
