package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.net.URI;

@Slf4j
public class ProxyCustomizer implements RestTemplateCustomizer {

    private final String hostToProxy;
    private final String proxyHost;
    private final int proxyPort;

    public ProxyCustomizer(String baseURL, String proxyHost, int proxyPort) {
        this.hostToProxy = URI.create(baseURL).getHost();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public void customize(RestTemplate restTemplate)  {

        log.info("Using proxy {}:{} for {}", proxyHost, proxyPort, hostToProxy);

        HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
        HttpClient httpClient = HttpClientBuilder.create().setRoutePlanner(new HMRCProxyRoutePlanner(proxy, hostToProxy)).build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

    }


}
