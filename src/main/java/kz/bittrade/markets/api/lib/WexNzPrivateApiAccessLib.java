package kz.bittrade.markets.api.lib;

import com.google.gson.JsonObject;
import kz.bittrade.com.AppSettingsHolder;
import kz.bittrade.com.BFConstants;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public final class WexNzPrivateApiAccessLib extends ApiAccessLib {
    private String key = null;
    private String secret = null;
    private int nonce = -1;

    public WexNzPrivateApiAccessLib(String key, String secret, int nonce) {
        this.key = key;
        this.secret = secret;
        this.nonce = nonce;
    }

    public static boolean isValidAPIKey(String str) {
        return str.matches(BFConstants.WEX_API_KEY_PATTERN);
    }

    public static boolean isValidSecret(String str) {
        return str.matches(BFConstants.WEX_API_SECRET_PATTERN);
    }

    private int getNonce() {
        int newNonce;
        if (nonce == -1) {
            newNonce = (new Random()).nextInt(Integer.MAX_VALUE / 10);
        } else {
            newNonce = ++nonce;
        }

        AppSettingsHolder.getInstance().setProperty(BFConstants.WEX_API_NONCE, String.valueOf(newNonce));
        return newNonce;
    }

    public JsonObject performAuthorizedRequest(ArrayList<NameValuePair> postData) {
        //remove previous request nonce value
        for(NameValuePair pair : postData) {
            if(pair.getName().equals("nonce")) {
                postData.remove(pair);
                break;
            }
        }
        //add new nonce
        postData.add(new BasicNameValuePair("nonce", String.valueOf(getNonce())));
        LinkedHashMap<String, String> headers = createHeaders(postData);

        return sendRequest(PRIVATE_API_URL, postData, headers);
    }

    private JsonObject sendRequest(String URL, ArrayList<NameValuePair> postData, LinkedHashMap<String, String> headers) {
        JsonObject result;
        if (headers == null) {
            log("Mandatory \"Sign\" header is absent");
            return null;
        }

        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(URL);

        try {
            post.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));

            for(Map.Entry<String, String> entry : headers.entrySet())
                post.addHeader(entry.getKey(), entry.getValue());

            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();

            if(entity == null)
                throw new NullPointerException();

            result = JsonHelper.getAsJson(IOUtils.toString(entity.getContent(), "UTF-8"));
        } catch (Exception e) {
            log("Failed to perform POST request");
            e.printStackTrace();
            return null;
        }

        return result;
    }

    private String getPostDataAsString(ArrayList<NameValuePair> postData) {
        String postDataStr = "";
        for(NameValuePair pair : postData) {
            postDataStr = postDataStr.concat(pair.getName());
            postDataStr = postDataStr.concat("=");
            postDataStr = postDataStr.concat(pair.getValue());
            postDataStr = postDataStr.concat("&");
        }

        return postDataStr.substring(0, postDataStr.length() - 1);
    }

    private LinkedHashMap<String, String> createHeaders(ArrayList<NameValuePair> postData) {
        LinkedHashMap<String, String> headers = new LinkedHashMap<>(2);
        headers.put("Key", this.key);

        String postDataStr = getPostDataAsString(postData);
        String sign = HashHelper.getHmacSHA512(postDataStr, this.secret);

        if(sign == null) {
            log("Failed to calculate Hmac-SHA512 of post data: \"".concat(postDataStr).concat("\""));
            return null;
        }

        log("Calculated Hmac-SHA512: \"".concat(sign).concat("\" of data: \"").concat(postDataStr).concat("\""));
        headers.put("Sign", sign);
        return headers;
    }
}