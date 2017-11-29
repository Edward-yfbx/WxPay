package com.usee.fmbc;

import com.softtek.fmbc.Utils.Const;
import com.softtek.fmbc.Utils.WXPayUtil;

import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    /**
     * 订单参数
     */
    private String getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("appid", Const.APP_ID);
        params.put("body", "Test Goods");
        params.put("mch_id", Const.MCH_ID);
        params.put("nonce_str", WXPayUtil.generateNonceStr());
        params.put("notify_url", Const.NOTIFY_URL);
        params.put("out_trade_no", WXPayUtil.generateUUID());
        params.put("spbill_create_ip", "36.149.13.239");
        params.put("total_fee", "1");
        params.put("trade_type", "APP");
        try {
            String sign = WXPayUtil.generateSignature(params, Const.API_KEY);
            params.put("sign", sign);
            return WXPayUtil.mapToXml(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取订单信息
     */
    @Test
    public void getOrder() {
        MediaType XML = MediaType.parse("application/xml; charset=utf-8");
        String params = getParams();
        RequestBody requestBody = RequestBody.create(XML, params);
        final Request request = new Request.Builder().url(Const.UNIFIED_ORDER).post(requestBody).build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}