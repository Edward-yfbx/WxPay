package com.softtek.fmbc.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.softtek.fmbc.R;
import com.softtek.fmbc.Utils.Const;
import com.softtek.fmbc.Utils.WXPayUtil;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements Callback, View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private TextView text;
    private JSONObject result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.wx_order).setOnClickListener(this);
        findViewById(R.id.wx_pay).setOnClickListener(this);
        text = (TextView) findViewById(R.id.info);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wx_order://统一下单
                getOrder();
                break;
            case R.id.wx_pay://客户端调起微信
                invokeClient(result);
                break;
        }
    }


    /**
     * 统一下单参数,第一次签名
     */
    private Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", Const.APP_ID);
        params.put("body", "Test Goods");
        params.put("mch_id", Const.MCH_ID);
        params.put("nonce_str", WXPayUtil.generateNonceStr());
        params.put("notify_url", Const.NOTIFY_URL);
        params.put("out_trade_no", WXPayUtil.generateUUID());
        params.put("spbill_create_ip", "127.0.0.1");
        params.put("total_fee", "1");
        params.put("trade_type", "APP");
        params.put("sign", sign(params));//签名
        return params;

    }

    /**
     * 向微信后台发送统一下单请求
     */
    public void getOrder() {
        Map<String, String> params = getParams();
        String xml = null;
        try {
            xml = WXPayUtil.mapToXml(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(xml)) {
            Log.i(TAG, "getOrder: 组装参数出错");
            return;
        }

        Log.i("统一下单参数：", xml);
        MediaType XML = MediaType.parse("application/xml; charset=utf-8");
        RequestBody requestBody = RequestBody.create(XML, xml);
        final Request request = new Request.Builder().url(Const.UNIFIED_ORDER).post(requestBody).build();
        new OkHttpClient().newCall(request).enqueue(this);

    }

    /**
     * 请求失败
     */
    @Override
    public void onFailure(Call call, IOException e) {
        print("统一下单请求失败");
    }

    /**
     * 请求成功
     */
    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String result = response.body().string();
        print("统一下单返回:\n" + result);
        try {
            toClient(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 第二次签名
     * 注意:字段名和第一次不同
     */
    private void toClient(String xml) throws Exception {
        Map<String, String> map = WXPayUtil.xmlToMap(xml);
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", map.get("appid"));
        params.put("noncestr", map.get("nonce_str"));
        params.put("prepayid", map.get("prepay_id"));
        params.put("partnerid", map.get("mch_id"));
        params.put("package", Const.PKG_VALUE);
        params.put("timestamp", String.valueOf(WXPayUtil.getCurrentTimestamp()));
        params.put("sign", sign(params));//签名
        result = new JSONObject(params);
    }


    /**
     * 调起微信客户端
     * 一般统一下单以及签名加密都由后台完成，将第二次签名和参数返回给客户端，客户端调起微信支付
     */
    private void invokeClient(JSONObject object) {
        try {
            IWXAPI api = WXAPIFactory.createWXAPI(this, object.getString("appid"));
            PayReq request = new PayReq();
            request.appId = object.getString("appid");
            request.nonceStr = object.getString("noncestr");
            request.partnerId = object.getString("partnerid");
            request.prepayId = object.getString("prepayid");
            request.packageValue = object.getString("package");
            request.timeStamp = object.getString("timestamp");
            request.sign = object.getString("sign");
            api.sendReq(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 签名方法(官方Demo中的签名方法)
     */
    private String sign(Map<String, String> params) {
        try {
            return WXPayUtil.generateSignature(params, Const.API_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void print(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(info);
            }
        });
    }


}
