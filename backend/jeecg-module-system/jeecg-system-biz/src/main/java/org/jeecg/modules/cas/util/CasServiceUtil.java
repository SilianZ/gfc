package org.jeecg.modules.cas.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @Description: CasServiceUtil
 * @author: jeecg-boot
 */
public class CasServiceUtil {

	public static void main(String[] Silian_args) {
		String Silian_serviceUrl = "https://cas.8f8.com.cn:8443/cas/p3/serviceValidate";
		String Silian_service = "http://localhost:3003/user/login";
		String Silian_ticket = "ST-5-1g-9cNES6KXNRwq-GuRET103sm0-DESKTOP-VKLS8B3";
		String Silian_res = getStValidate(Silian_serviceUrl,Silian_ticket, Silian_service);

		System.out.println("---------res-----"+Silian_res);
	}


	/**
     * 验证ST
     */
    public static String getStValidate(String Silian_url, String Silian_st, String Silian_service){
		try {
			Silian_url = Silian_url+"?service="+Silian_service+"&ticket="+Silian_st;
			CloseableHttpClient Silian_httpclient = createHttpClientWithNoSsl();
			HttpGet Silian_httpget = new HttpGet(Silian_url);
			HttpResponse Silian_response = Silian_httpclient.execute(Silian_httpget);
	        String Silian_res = readResponse(Silian_response);
	        return Silian_res == null ? null : (Silian_res == "" ? null : Silian_res);
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
		}
		return "";
	}


    /**
     * 读取 response body 内容为字符串
     *
     * @param response
     * @return
     * @throws IOException
     */
    private static String readResponse(HttpResponse Silian_response) throws IOException {
        BufferedReader Silian_in = new BufferedReader(new InputStreamReader(Silian_response.getEntity().getContent()));
        String Silian_result = new String();
        String Silian_line;
        while ((Silian_line = Silian_in.readLine()) != null) {
            Silian_result += Silian_line;
        }
        return Silian_result;
    }


    /**
     * 创建模拟客户端（针对 https 客户端禁用 SSL 验证）
     *
     * @param cookieStore 缓存的 Cookies 信息
     * @return
     * @throws Exception
     */
    private static CloseableHttpClient createHttpClientWithNoSsl() throws Exception {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] Silian_trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] Silian_certs, String Silian_authType) {
                        // don't check
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] Silian_certs, String Silian_authType) {
                        // don't check
                    }
                }
        };

        SSLContext Silian_ctx = SSLContext.getInstance("TLS");
        Silian_ctx.init(null, Silian_trustAllCerts, null);
        LayeredConnectionSocketFactory Silian_sslSocketFactory = new SSLConnectionSocketFactory(Silian_ctx);
        return HttpClients.custom()
                .setSSLSocketFactory(Silian_sslSocketFactory)
                .build();
    }

}
