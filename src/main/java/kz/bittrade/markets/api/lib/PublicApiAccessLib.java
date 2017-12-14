package kz.bittrade.markets.api.lib;

import com.google.gson.JsonObject;
import kz.bittrade.markets.api.lib.util.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

public final class PublicApiAccessLib extends ApiAccessLib {

    public static JsonObject performBasicRequest(String method, String optionalPair) {
        JsonObject result;
        String URL = PUBLIC_API_URL.concat(method);

        URL = URL.concat(optionalPair);

        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(URL);

        get.setHeader("Accept", "application/json");
        get.setHeader("Content-type", "application/json");

//        for (Header header : getHeaders()) {
//            get.addHeader(header);
//        }

        try {
            HttpResponse response = client.execute(get);
            HttpEntity entity = response.getEntity();

            if (entity == null)
                throw new NullPointerException();

            result = JsonHelper.getAsJson(IOUtils.toString(entity.getContent(), "UTF-8"));
        } catch (Exception e) {
            log("Failed to perform GET request");
            e.printStackTrace();
            return null;
        }
        return result;
    }
}
