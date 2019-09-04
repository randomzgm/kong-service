package com.block90.wallet;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class KongService {
    private static final String DATE_FIELD = "x-date";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Log log = LogFactory.getLog(KongService.class.getName());

    public enum HttpMethod {
        GET("GET"), POST("POST");

        private String name;

        private HttpMethod(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String host;

    public KongService(String host) {
        this.host = host;
    }

    public void callRpc(String username, String secret, String body) throws InvalidKeyException, NoSuchAlgorithmException {
        log.info("--------------- call rpc with hmac-auth ---------------");
        postService(host + "rpc", body, username, secret);
    }

    public void callPlDifficulty() throws NoSuchAlgorithmException, InvalidKeyException {
        log.info("--------------- call get difficulty by get ---------------");
        getService(host + "ben-pl/api/getdifficulty", StringUtils.EMPTY, null, null);
    }

    public void getService(String url, String body, String username, String secret)
            throws InvalidKeyException, NoSuchAlgorithmException {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(secret)) {
            headers = createHmacHeaders(body, username, secret);
        }
        callService(url, headers, body, HttpMethod.GET);
    }

    public void postService(String url, String body, String username, String secret)
            throws InvalidKeyException, NoSuchAlgorithmException {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(secret)) {
            headers = createHmacHeaders(body, username, secret);
        }
        callService(url, headers, body);
    }

    private void callService(String url, Map<String, String> headers, String body) {
        callService(url, headers, body, HttpMethod.POST);
    }

    private void callService(String url, Map<String, String> headers, String body, HttpMethod method) {
        log.info(String.format("call service at: %s by %s", url, method.getName()));
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpRequestBase request;
        if (HttpMethod.GET.equals(method)) {
            request = new HttpGet(url);
        } else {
            request = new HttpPost(url);
        }
        try {
            Map<String, String> reqHeaders = new HashMap<>();
            reqHeaders.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)" +
                    " snap IntelliJ IDEA 2019.2.1 (Community Edition) Build #IC-192.6262.58, built on August 20, 2019");
            reqHeaders.putAll(headers);
            log.debug("reqHeaders = " + JSON.toJSONString(reqHeaders, true));
            for (Map.Entry<String, String> entry : reqHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            if (request instanceof HttpPost) {
                ((HttpPost) request).setEntity(new StringEntity(body, Charsets.UTF_8));
            }
            CloseableHttpResponse response = client.execute(request);
            String result = EntityUtils.toString(response.getEntity());
            log.info("response = " + JSON.toJSONString(JSON.parse(result), true));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Map<String, String> createHmacHeaders(String body, String username, String hmacSecret) throws NoSuchAlgorithmException, InvalidKeyException {
        Map<String, String> headers = new HashMap<>();
        // 用sha256算法计算请求body的摘要
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(body.getBytes(StandardCharsets.UTF_8));
        String bodyDigest = String.format("SHA-256=%s", BASE64_ENCODER.encodeToString(md.digest()));

        // 生成当前GMT时间，注意格式不能改变，必须形如：Wed, 14 Aug 2019 09:09:28 GMT
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String gmTime = sdf.format(new Date());

        // 拼装待签名的数据
        String strToSign = String.format("%s: %s\ndigest: %s", DATE_FIELD, gmTime, bodyDigest);

        // 生成签名
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secret);
        String signature = BASE64_ENCODER.encodeToString(hmac.doFinal(strToSign.getBytes(StandardCharsets.UTF_8)));
        String Authorization =
                String.format("hmac username=\"%s\", algorithm=\"hmac-sha256\", headers=\"%s digest\", signature=\"%s\"",
                        username, DATE_FIELD, signature);

        // 拼装headers
        headers.put(DATE_FIELD, gmTime);
        headers.put("Digest", bodyDigest);
        headers.put("Authorization", Authorization);
        return headers;
    }
}
