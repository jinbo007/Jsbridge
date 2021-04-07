package com.peiqi.jsbridge;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.peiqi.jsbridge.bridge.BridgeUtil;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author houjinbo
 * @date 3/22/21
 * 参考jsbridge:https://github.com/lzyzsd/JsBridge
 */
public class WebViewActivity extends AppCompatActivity {
    WebView webView;

    private String METHOD_AUTH = "getAuthInfo";
    private String METHOD_LOCATION = "getLocation";
    private String METHOD_SCAN = "startQrCode";
    private String METHOD_PERMISSION = "checkPermisssion";


    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/demo.html");
//        webView.loadUrl("http://10.150.123.123:8080");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                BridgeUtil.webViewLoadLocalJs(view, "jsbridge.js");
                showMsg("注入完成");
            }
        });

        webView.addJavascriptInterface(new JsInterace(), "jsbridge");

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public class JsInterace {
        @JavascriptInterface
        public void send(String method, String msg, String callbackId) {
            showMsg(" 客户端收到消息: 方法名:" + method + "消息体:" + msg + "\n callbackId:" + callbackId);
            if (METHOD_LOCATION.equals(method)) {
                getLocation(msg, callbackId);
            }
        }
    }

    /**
     * 定位
     */
    private void getLocation(String msg, String callbackId) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("ret_code", "00");
            obj.put("longitude", 35.24324324);
            obj.put("latitude", 135.24324324);
            obj.put("ret_msg", "定位成功");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callbackJs(callbackId, obj.toString());
    }

    /**
     * 将原生处理的结果回调回H5端
     *
     * @param callbackId
     * @param data
     */
    private void callbackJs(String callbackId, String data) {

        //检查是否需要回调
        if (TextUtils.isEmpty(callbackId)) {
            Log.d("tag", "不需要回调给js---> callbackId 为空 ");
            return;
        }
        Log.d("tag", "回调给js--->" + callbackId + data);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject object = new JSONObject();
                try {
                    object.put("callbackId", callbackId);
                    object.put("data", BridgeUtil.formatJson(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String js_content = String.format(BridgeUtil.JS_HANDLE_MESSAGE_FROM_JAVA, object.toString());
                webView.loadUrl(js_content);


            }
        }, 1000);

    }

    private void showMsg(String msg) {
        Log.d("tag", " 原生收到的消息" + msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WebViewActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
