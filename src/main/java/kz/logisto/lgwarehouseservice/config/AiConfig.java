package kz.logisto.lgwarehouseservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kz.logisto.lgwarehouseservice.data.model.MovementAnalyticsModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.openai.api.ResponseFormat.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

  @Bean
  public ChatClient chatClient(ChatClient.Builder builder) {
    return builder.build();
  }

  @Bean
  public BeanOutputConverter<MovementAnalyticsModel> movementAnalyticsConverter() {
    return new BeanOutputConverter<>(MovementAnalyticsModel.class);
  }

  @Bean
  public OpenAiChatOptions analyticsResponseOptions(
      BeanOutputConverter<MovementAnalyticsModel> converter,
      ObjectMapper objectMapper) throws JsonProcessingException {
    Map<String, Object> schema = converter.getJsonSchemaMap();
    addRequiredFields(schema);
    String schemaJson = objectMapper.writeValueAsString(schema);

    return OpenAiChatOptions.builder()
        .responseFormat(ResponseFormat.builder()
            .type(Type.JSON_SCHEMA)
            .jsonSchema(ResponseFormat.JsonSchema.builder()
                .name("MovementAnalytics")
                .schema(schemaJson)
                .strict(true)
                .build())
            .build())
        .build();
  }

  @SuppressWarnings("unchecked")
  private void addRequiredFields(Map<String, Object> schema) {
    Object properties = schema.get("properties");
    if (properties instanceof Map<?, ?> props) {
      schema.put("required", new ArrayList<>(props.keySet()));
      for (Object value : props.values()) {
        if (value instanceof Map<?, ?> fieldSchema) {
          addRequiredFields((Map<String, Object>) fieldSchema);
        }
      }
    }

    Object items = schema.get("items");
    if (items instanceof Map<?, ?>) {
      addRequiredFields((Map<String, Object>) items);
    }
  }
}
