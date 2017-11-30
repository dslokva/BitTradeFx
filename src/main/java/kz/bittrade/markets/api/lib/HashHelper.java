package kz.bittrade.markets.api.lib;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HashHelper {


    public static String getHmacSHA(String str, String secret, String algo) {
        Mac macInst = null;
        String result = "";

        try {
            macInst = Mac.getInstance(algo);
            macInst.init(new SecretKeySpec(secret.getBytes("UTF-8"), algo));

            result = DatatypeConverter.printHexBinary((macInst.doFinal(str.getBytes("UTF-8")))).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        return result;
    }
}