package kz.bittrade.markets.api.lib;

import kz.bittrade.com.BFConstants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;


public final class BitfinexPrivateApiAccessLib extends ApiAccessLib {

    private static final String TAG = BitfinexPrivateApiAccessLib.class.getSimpleName();

    private String apiKey = "";
    private String apiKeySecret = "";
    private long nonce = System.currentTimeMillis();


    public BitfinexPrivateApiAccessLib(String apiKey, String apiKeySecret) {
        this.apiKey = apiKey;
        this.apiKeySecret = apiKeySecret;
    }

    /**
     * Creates an authenticated request WITHOUT request parameters. Send a request for Balances.
     *
     * @return Response string if request successfull
     * @throws IOException
     */
    public String sendRequestV1Balances() throws IOException {
        String sResponse;

        HttpURLConnection conn = null;

        String urlPath = "balances";
        String method = "POST";

        try {
            URL url = new URL(BFConstants.BIT_API_BASIC_URL + urlPath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);

            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jo = new JSONObject();
            jo.put("request", "/v1/" + urlPath);
            jo.put("nonce", Long.toString(getNonce()));

            // API v1
            String payload = jo.toString();
            String payload_base64 = Base64.getMimeEncoder().encodeToString(payload.getBytes());

//            String payload_sha384hmac = hmacDigest(payload_base64, apiKeySecret, BFConstants.ALGORITHM_HMACSHA384);
            String payload_sha384hmac = HashHelper.getHmacSHA(payload_base64, apiKeySecret, BFConstants.ALGORITHM_HMACSHA384);

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.addRequestProperty("X-BFX-APIKEY", apiKey);
            conn.addRequestProperty("X-BFX-PAYLOAD", payload_base64);
            conn.addRequestProperty("X-BFX-SIGNATURE", payload_sha384hmac);

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            return convertStreamToString(in);

        } catch (MalformedURLException e) {
            throw new IOException(e.getClass().getName(), e);
        } catch (IOException e) {
            String errMsg = e.toString();

            if (conn != null) {
                try {
                    InputStream errorStream = conn.getErrorStream();
                    if (errorStream != null) sResponse = convertStreamToString(errorStream);
                    else sResponse = "Network error";
                    errMsg += " -> " + sResponse;
                    log(TAG + " " + errMsg);
                    return sResponse;
                } catch (IOException e1) {
                    errMsg += " Error on reading error-stream. -> " + e1.getLocalizedMessage();
                    log(TAG + " " + errMsg);
                    throw new IOException(e.getClass().getName(), e1);
                }
            } else {
                throw new IOException(e.getClass().getName(), e);
            }
        } catch (JSONException e) {
            String msg = "Error on setting up the connection to server";
            throw new IOException(msg, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public long getNonce() {
        return ++nonce;
    }


//    public static String hmacDigest(String msg, String keyString, String algo) {
//        String digest = null;
//        try {
//            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), algo);
//            Mac mac = Mac.getInstance(algo);
//            mac.init(key);
//
//            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));
//
//            StringBuffer hash = new StringBuffer();
//            for (int i = 0; i < bytes.length; i++) {
//                String hex = Integer.toHexString(0xFF & bytes[i]);
//                if (hex.length() == 1) {
//                    hash.append('0');
//                }
//                hash.append(hex);
//            }
//            digest = hash.toString();
//        } catch (UnsupportedEncodingException e) {
//            log(TAG + " Exception: " + e.getMessage());
//        } catch (InvalidKeyException e) {
//            log(TAG + " Exception: " + e.getMessage());
//        } catch (NoSuchAlgorithmException e) {
//            log(TAG + " Exception: " + e.getMessage());
//        }
//        return digest;
//    }
}
