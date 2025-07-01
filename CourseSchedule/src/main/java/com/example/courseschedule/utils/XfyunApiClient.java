//好像没调用成功
/*package com.example.courseschedule.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import jakarta.annotation.Nullable;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class XfyunApiClient {

    // 讯飞星火大模型配置
    public static final String hostUrl = "https://spark-api.xf-yun.com/v1/x1";
    public static final String domain = "x1";
    public static final String appid = "efae899f";
    public static final String apiSecret = "ZTMxZWRkZjVmYTM3MDE5MTNjMDQwMDY3";
    public static final String apiKey = "274d016ed53b8a61a4242b670ab429c1";

    // 构建鉴权 URL（保持不变）
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "GET " + url.getPath() + " HTTP/1.1";
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", sha);

        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath())
                .newBuilder()
                .addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date)
                .addQueryParameter("host", url.getHost())
                .build();

        return httpUrl.toString();
    }

    /**
     * 新方法：专门为智能排课设计的API调用
     * @param prompt 排课相关的提示词
     * @return 大模型的完整响应
     */
/*
    public static String callForScheduling(String prompt) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().build();
        String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");

        final String[] responseHolder = new String[1]; // 用于保存响应

        Request request = new Request.Builder().url(url).build();
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                JSONObject requestJson = new JSONObject();
                
                // Header
                JSONObject header = new JSONObject();
                header.put("app_id", appid);
                header.put("uid", UUID.randomUUID().toString().substring(0, 10));
                
                // Parameter
                JSONObject parameter = new JSONObject();
                JSONObject chat = new JSONObject();
                chat.put("domain", domain);
                chat.put("temperature", 0.3); // 更低的随机性，适合排课
                chat.put("max_tokens", 2048); // 适当限制响应长度
                parameter.put("chat", chat);
                
                // Payload
                JSONObject payload = new JSONObject();
                JSONObject message = new JSONObject();
                message.put("text", new JSONArray().fluentAdd(
                    new JSONObject()
                        .fluentPut("role", "user")
                        .fluentPut("content", prompt)
                ));
                payload.put("message", message);
                
                requestJson.put("header", header);
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);
                
                webSocket.send(requestJson.toString());
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                // 保存响应而不是直接打印
                responseHolder[0] = text;
                webSocket.close(1000, "Normal closure");
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                webSocket.close(1000, null);
                synchronized (webSocket) {
                    webSocket.notify();
                }
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                t.printStackTrace();
                synchronized (webSocket) {
                    webSocket.notify();
                }
            }
        });

        // 等待 WebSocket 连接关闭
        synchronized (webSocket) {
            try {
                webSocket.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return responseHolder[0]; // 返回完整的响应
    }

    /**
     * 原始方法保留（兼容旧代码）
     */

   /* public static String callXfyunAPI(Map<String, Object> requestData) throws Exception {
        // 这里可以调用新的callForScheduling方法或保持原有实现
        return callForScheduling("介绍一下你自己"); // 默认行为
    }
}*/


//正确的测试连接
/*
package com.example.courseschedule.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import jakarta.annotation.Nullable;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class XfyunApiClient {

    // 讯飞星火大模型配置
    public static final String hostUrl = "https://spark-api.xf-yun.com/v1/x1";
    public static final String domain = "x1";
    public static final String appid = "efae899f";
    public static final String apiSecret = "ZTMxZWRkZjVmYTM3MDE5MTNjMDQwMDY3";
    public static final String apiKey = "274d016ed53b8a61a4242b670ab429c1";

    // 构建鉴权 URL
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "GET " + url.getPath() + " HTTP/1.1";
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", sha);

        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath())
                .newBuilder()
                .addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date)
                .addQueryParameter("host", url.getHost())
                .build();

        return httpUrl.toString();
    }

    // 调用讯飞星火大模型 API
    public static String callXfyunAPI(Map<String, Object> requestData) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder().build();

        // 构建鉴权 URL
        String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");

        // 构建 WebSocket 请求
        Request request = new Request.Builder().url(url).build();
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                // 发送请求数据
                JSONObject requestJson = new JSONObject();
                JSONObject header = new JSONObject();
                header.put("app_id", appid);
                header.put("uid", UUID.randomUUID().toString().substring(0, 10));

                JSONObject parameter = new JSONObject();
                JSONObject chat = new JSONObject();
                chat.put("domain", domain);
                chat.put("temperature", 0.5);
                chat.put("max_tokens", 4096);
                parameter.put("chat", chat);

                JSONObject payload = new JSONObject();
                JSONObject message = new JSONObject();  // Changed from JSONArray to JSONObject

                // Add text content directly to the message object
                message.put("text", new JSONArray().fluentAdd(
                    new JSONObject()
                        .fluentPut("role", "user")
                        .fluentPut("content", "介绍一下你自己")
                ));

                payload.put("message", message);  // Now passing a JSONObject instead of JSONArray
                requestJson.put("header", header);
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);

                webSocket.send(requestJson.toString());
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                System.out.println(text);
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                webSocket.close(1000, null);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                t.printStackTrace();
            }
        });

        // 等待 WebSocket 连接关闭
        synchronized (webSocket) {
            try {
                webSocket.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return "Success";
    }
}
*/
package com.example.courseschedule.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class XfyunApiClient {

    // 讯飞星火大模型配置
    public static final String hostUrl = "https://spark-api.xf-yun.com/v1/x1";
    public static final String domain = "x1";
    public static final String appid = "efae899f";
    public static final String apiSecret = "ZTMxZWRkZjVmYTM3MDE5MTNjMDQwMDY3";
    public static final String apiKey = "274d016ed53b8a61a4242b670ab429c1";

    // 构建鉴权 URL
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "GET " + url.getPath() + " HTTP/1.1";
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                apiKey, "hmac-sha256", "host date request-line", sha);

        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath())
                .newBuilder()
                .addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)))
                .addQueryParameter("date", date)
                .addQueryParameter("host", url.getHost())
                .build();

        return httpUrl.toString();
    }

    /**
     * 调用讯飞星火API
     * @param prompt 提示词内容
     * @return 大模型响应结果
     */
    public static String callXfyunAPI(String prompt) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        String authUrl = getAuthUrl(hostUrl, apiKey, apiSecret);
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");

        final CountDownLatch latch = new CountDownLatch(1);
        final StringBuilder responseBuilder = new StringBuilder();

        Request request = new Request.Builder().url(url).build();
        WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                JSONObject requestJson = new JSONObject();
                
                // Header
                JSONObject header = new JSONObject();
                header.put("app_id", appid);
                header.put("uid", UUID.randomUUID().toString().substring(0, 10));
                
                // Parameter
                JSONObject parameter = new JSONObject();
                JSONObject chat = new JSONObject();
                chat.put("domain", domain);
                chat.put("temperature", 0.3);
                chat.put("max_tokens", 2048);
                parameter.put("chat", chat);
                
                // Payload
                JSONObject payload = new JSONObject();
                JSONObject message = new JSONObject();
                message.put("text", new JSONArray().fluentAdd(
                    new JSONObject()
                        .fluentPut("role", "user")
                        .fluentPut("content", prompt)
                ));
                payload.put("message", message);
                
                requestJson.put("header", header);
                requestJson.put("parameter", parameter);
                requestJson.put("payload", payload);
                
                webSocket.send(requestJson.toString());
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                responseBuilder.append(text);
                System.out.println(text);
                webSocket.close(1000, "Normal closure");
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                latch.countDown();
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                responseBuilder.append("Error: ").append(t.getMessage());
                latch.countDown();
            }
        });

        latch.await(60, TimeUnit.SECONDS);
        return responseBuilder.toString();
    }
}