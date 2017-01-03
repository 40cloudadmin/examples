/*

* THE PROGRAM IS DISTRIBUTED IN THE HOPE THAT IT WILL BE USEFUL, BUT WITHOUT ANY WARRANTY. 
* IT IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, 
* EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, 
* THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. 
* THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. 
* SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL 
* NECESSARY SERVICING, REPAIR OR CORRECTION.


* IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW THE AUTHOR WILL BE LIABLE TO YOU FOR DAMAGES, 
* INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR INABILITY 
* TO USE THE PROGRAM (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED 
* INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE OF THE PROGRAM TO OPERATE WITH ANY OTHER PROGRAMS), 
* EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 

* Example for 40Cloud API Key and Secret
*
* Usage: 
* Change the values FC_API_KEY and FC_API_SECRET to your own key and secret keys as provided from the management console
* run the program
*
* Dependencies:
* Apache http client - httpclient-4.3.6.jar and httpcore-4.3.3.jar
* Apache based 64 Encoding - commons-codec-1.9.jar
* Apache Common logging - commons-logging-1.2.jar
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
