package com.winnie.widget.smsobserverdemo.kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.winnie.widget.smsobserverdemo.R
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private var codeView: TextView? = null
    private var smsObserver: SmsObserver? = null

    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            codeView!!.text = msg.obj as String
            Log.e("mainactivity", "activity get code time:" + System.currentTimeMillis())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permission_code) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                    showPermissionDialog()
                } else {
                    Toast.makeText(this, "必须开启权限才可使用该功能", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        codeView = findViewById(R.id.verify_code)

        requestPermissions()
    }

    /**
     * 动态监测权限
     */
    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                showPermissionDialog()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), permission_code)
            }
        } else {
            init()
        }
    }

    private fun init() {
        smsObserver = SmsObserver(handler, this, 6)
        val uri = Uri.parse("content://sms")

        /**
         * notifyForDescendents：如果为true表示以这个Uri为开头的所有Uri都会被匹配到，
         * 如果为false表示精确匹配，即只会匹配这个给定的Uri。
         */
        contentResolver.registerContentObserver(uri, true, smsObserver!!)

        testReadSms()
    }

    private fun testReadSms() {
        Thread(Runnable {
            ReadSmsUtils.readSms(this, Uri.parse("content://sms/1"), "\\d{6}", handler)
        }).run()
    }

    /**
     * 权限引导弹窗
     */
    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
                .setTitle("获取短信权")
                .setMessage("我们需要获取短信权限，才能读取短信验证码")
                .setNegativeButton("取消") { dialog, which -> finish() }.setPositiveButton("确定") { dialog, which -> ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_SMS), permission_code) }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(smsObserver!!)
    }

    companion object {

        val received_code = 1001
        private val permission_code = 1002
    }
}