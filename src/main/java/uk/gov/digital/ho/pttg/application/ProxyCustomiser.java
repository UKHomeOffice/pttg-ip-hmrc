package uk.gov.digital.ho.pttg.application;

import com.google.common.net.InternetDomainName;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


public class ProxyCustomiser implements RestTemplateCustomizer {

    @Value("${hmrc.endpoint}") private String hmrcBaseUrl;
    @Value("${https.proxyHost}") private String proxyHost;
    @Value("${https.proxyPort}") private String proxyPort;

    @Override
    public void customize(RestTemplate restTemplate)  {
        final String proxyUrl = proxyHost;
        final int port = Integer.parseInt(proxyPort);

        HttpHost proxy = new HttpHost(proxyUrl, port, "https");
        HttpClient httpClient = HttpClientBuilder.create().setRoutePlanner(new DefaultProxyRoutePlanner(proxy) {
            @Override
            protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context)
                    throws HttpException {

                if (target.getHostName().equals(InternetDomainName.from(hmrcBaseUrl).topPrivateDomain().toString())) {
                    return super.determineProxy(target, request, context);
                }
                return null;
            }
        }).build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

    }

}
