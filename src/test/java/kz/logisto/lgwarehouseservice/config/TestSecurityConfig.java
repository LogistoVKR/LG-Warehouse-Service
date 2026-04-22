package kz.logisto.lgwarehouseservice.config;

import kz.logisto.lgwarehouseservice.data.model.MovementAnalyticsModel;
import kz.logisto.lgwarehouseservice.service.ItemSearchService;
import kz.logisto.lgwarehouseservice.service.MovementAnalyticsService;
import kz.logisto.lgwarehouseservice.service.UserService;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestSecurityConfig {

  @Bean
  @Primary
  public UserService testUserService() {
    UserService mock = Mockito.mock(UserService.class);
    Mockito.when(mock.isMember(Mockito.anyString(), Mockito.any())).thenReturn(true);
    Mockito.when(mock.canManageWarehouse(Mockito.anyString(), Mockito.any())).thenReturn(true);
    return mock;
  }

  @Bean
  @Primary
  public ItemSearchService testItemSearchService() {
    return Mockito.mock(ItemSearchService.class);
  }

  @Bean
  @Primary
  public MovementAnalyticsService testMovementAnalyticsService() {
    return Mockito.mock(MovementAnalyticsService.class);
  }

  @Bean
  @Primary
  public VectorStore testVectorStore() {
    return Mockito.mock(VectorStore.class);
  }

  @Bean
  @Primary
  public ChatClient testChatClient() {
    return Mockito.mock(ChatClient.class);
  }

  @Bean
  public ChatClient.Builder testChatClientBuilder() {
    return Mockito.mock(ChatClient.Builder.class, Mockito.RETURNS_DEEP_STUBS);
  }

  @Bean
  @Primary
  @SuppressWarnings("unchecked")
  public BeanOutputConverter<MovementAnalyticsModel> testMovementAnalyticsConverter() {
    return Mockito.mock(BeanOutputConverter.class);
  }

  @Bean
  @Primary
  public OpenAiChatOptions testAnalyticsResponseOptions() {
    return OpenAiChatOptions.builder().build();
  }

  @Bean
  @Primary
  public ClientHttpRequestInterceptor testHttpSecurityInterceptor() {
    return Mockito.mock(ClientHttpRequestInterceptor.class);
  }

  @Bean
  @Primary
  public ClientRegistrationRepository testClientRegistrationRepository() {
    return Mockito.mock(ClientRegistrationRepository.class);
  }

  @Bean
  @Primary
  public OAuth2AuthorizedClientRepository testAuthorizedClientRepository() {
    return Mockito.mock(OAuth2AuthorizedClientRepository.class);
  }

  @Bean
  @Primary
  public JwtDecoder testJwtDecoder() {
    return Mockito.mock(JwtDecoder.class);
  }
}
