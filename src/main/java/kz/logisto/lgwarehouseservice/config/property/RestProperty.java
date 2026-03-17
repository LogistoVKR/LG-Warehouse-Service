package kz.logisto.lgwarehouseservice.config.property;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("application.rest")
public class RestProperty {

  private RestServiceProperty mcUserService;

  @Getter
  @Setter
  public static class RestServiceProperty {

    private String url;
    private String contextPath;
    private Duration connectTimeout;
    private Duration readTimeout;
  }
}
