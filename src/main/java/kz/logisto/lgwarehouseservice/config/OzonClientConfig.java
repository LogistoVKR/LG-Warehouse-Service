package kz.logisto.lgwarehouseservice.config;

import kz.logisto.lgwarehouseservice.config.property.RestProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class OzonClientConfig {

  @Bean
  public RestClient ozonRestClient(RestProperty restProperty) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(restProperty.getOzonApi().getConnectTimeout());
    factory.setReadTimeout(restProperty.getOzonApi().getReadTimeout());
    return RestClient.builder()
        .baseUrl(restProperty.getOzonApi().getUrl())
        .requestFactory(factory)
        .build();
  }
}
