package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
public class ProxyCustomiser implements RestTemplateCustomizer {

    private final String hostToProxy;
    private final String proxyHost;
    private final int proxyPort;

    public ProxyCustomiser(String baseURL, String proxyHost, int proxyPort) {
        this.hostToProxy = URI.create(baseURL).getHost();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public void customize(RestTemplate restTemplate)  {

        log.info("Using proxy {}:{} for {}", proxyHost, proxyPort, hostToProxy);

        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "https");
        HttpClient httpClient = HttpClientBuilder.create().setRoutePlanner(new DefaultProxyRoutePlanner(proxy) {
            @Override
            protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context)
                    throws HttpException {

                if (target.getHostName().equals(hostToProxy)) {
                    log.info("proxying {} to {}", target.getHostName(), hostToProxy);
                    return super.determineProxy(target, request, context);
                }
                return null;
            }
        }).build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

    }

}
