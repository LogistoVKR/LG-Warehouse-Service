package kz.logisto.lgwarehouseservice.util;

import kz.logisto.lgwarehouseservice.config.property.RestProperty.RestServiceProperty;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public final class RestClientUtil {

  private RestClientUtil() {
  }

  public static RestClient build(RestServiceProperty property,
                                 ClientHttpRequestInterceptor httpSecurityInterceptor) {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(property.getConnectTimeout());
    factory.setReadTimeout(property.getReadTimeout());
    return RestClient.builder()
        .baseUrl(property.getUrl())
        .requestInterceptor(httpSecurityInterceptor)
        .build();
  }
}
