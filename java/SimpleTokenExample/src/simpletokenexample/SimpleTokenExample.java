/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpletokenexample;

import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author amir
 */
public class SimpleTokenExample {
    public static String FC_API_KEY="<Your FC API key here>";
    public static String FC_API_SECRET="<Your FC API secret here>";
    public static String API_ENTRY_POINT = "https://api.fortycloud.net";
    
    public static final int TIMEOUT = 120000;
    
    
    public static String getSignatureDate() {
        String DATEFORMAT = "E, dd MMM yyyy HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date()) + " GMT";
    }
    public static  void setSignature(String path, HttpRequestBase httpreq, String httpVerb, String body) throws Exception {
        String date = getSignatureDate();
        httpreq.setHeader(HTTP.DATE_HEADER, date);
        Header[] h = httpreq.getHeaders(HTTP.CONTENT_TYPE);
        String contentType = "";
        if (h != null && h.length > 0) {
            contentType = h[0].getValue();
        }
        String signature = calculateSignature(httpVerb, contentType, path, date, body == null ? "" : body);
        httpreq.setHeader("Authorization", "FCRestAPI AccessKey=" + FC_API_KEY + " SignatureType=HmacSHA256 Signature=" + signature);
}    
    private static  String calculateSignature(String restVerb, String contentType, String url, String date, String body) throws Exception {
 
        StringBuilder stringToSign = new StringBuilder();
        stringToSign.append(restVerb).append("\n");
        stringToSign.append(contentType).append("\n");
        stringToSign.append(date).append("\n");
        stringToSign.append(url).append("\n");
        stringToSign.append(body).append("\n");
        byte[] toSign = stringToSign.toString().getBytes("US-ASCII");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(FC_API_SECRET.getBytes(), "HmacSHA256"));
        byte[] rawHmac = mac.doFinal(toSign);
 
        return new String(Base64.encodeBase64(rawHmac));
 
    }  
    public static CloseableHttpClient build()throws Exception {

        SSLContext sslcontext = SSLContexts.custom().build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,null);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();        
        return httpclient;        
        
        
    }    
    
    public static String FC_SUBNET_RESOURCE = "/restapi/v0.4/subnets";
    
    public static void main(String[] args) throws Exception{

        String url = API_ENTRY_POINT+FC_SUBNET_RESOURCE;
        HttpGet httpAction = new HttpGet(url);
        setSignature(FC_SUBNET_RESOURCE, httpAction, httpAction.getMethod(), null);
        
        CloseableHttpClient localClient = build();
        CloseableHttpResponse httpResponse = localClient.execute(httpAction);
        HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            String responseBody = EntityUtils.toString(entity);
            System.out.println(responseBody);
        }
    }
    
}
