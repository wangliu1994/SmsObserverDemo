package com.winnie.widget.smsobserverdemo.kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.telephony.SmsMessage
import android.util.Log
import com.winnie.widget.smsobserverdemo.java.MainActivity
import java.util.regex.Pattern

/**
 * Created by winnie on 2018/5/31.
 */
class SmsReceiver(private var context: Context, private var handler: Handler, compileLength: Int) : BroadcastReceiver() {

    private val compileValue: String
    init {
        compileValue = "\\d{$compileLength}"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        var bundle: Bundle = intent!!.extras
        var objects: Array<Any> = bundle.get("pdus") as Array<Any>
        if(objects != null){
            var phone:String? = null
            var body: StringBuilder = StringBuilder()
            for (item in objects) {
                var smsMessage = SmsMessage.createFromPdu(item as ByteArray)
                phone = smsMessage.displayOriginatingAddress
                body.append(smsMessage.displayMessageBody)
            }
            Log.e("smsbc", "phone:" + phone + "\ncontent:" + body.toString())
            checkSmsAndSend(body.toString())
        }
    }

    private fun checkSmsAndSend(body: String){
        // 正则表达式的使用,从一段字符串中取出六位连续的数字
        val pattern = Pattern.compile(compileValue)
        val matcher = pattern.matcher(body)
        if (matcher.find()) {
            val obj = matcher.group(0)
            Log.e("smsbc", "短信中找到了符合规则的验证码:" + obj)
            val msg = Message.obtain()
            msg.what = MainActivity.received_code
            msg.obj = obj
            handler.sendMessage(msg)
            Log.e("smsbc", "广播接收器接收到短信的时间:" + System.currentTimeMillis())
        } else {
            Log.e("smsbc", "短信中没有找到符合规则的验证码")
        }
    }
}