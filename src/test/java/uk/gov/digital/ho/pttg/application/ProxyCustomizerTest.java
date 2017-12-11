package uk.gov.digital.ho.pttg.application;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProxyCustomizerTest {

    @Mock RestTemplate template;

    @Test
    public void shouldSetRequestFactory() {
        ProxyCustomizer customizer = new ProxyCustomizer(
                "test.hmrc.gov.uk",
                "a.proxy.server",
                1234);

        customizer.customize(template);
        verify(template).setRequestFactory(any(HttpComponentsClientHttpRequestFactory.class));
    }
}