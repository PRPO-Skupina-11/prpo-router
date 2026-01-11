package com.prpo.router.providers.anthropic;

import com.prpo.router.domain.ChatTurn;
import com.prpo.router.domain.ModelId;
import com.prpo.router.domain.ProviderId;
import com.prpo.router.providers.ProviderClient;
import com.prpo.router.providers.anthropic.dto.MessagesCreateRequest;
import com.prpo.router.providers.anthropic.dto.MessagesCreateResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AnthropicClient implements ProviderClient {

  private final WebClient webClient;
  private final String defaultModel;
  private final Integer maxTokens;

  public AnthropicClient(
      @Qualifier("anthropicWebClient") WebClient anthropicWebClient,
      @Value("${anthropic.model}") String defaultModel,
      @Value("${anthropic.maxTokens}") Integer maxTokens
  ) {
    this.webClient = anthropicWebClient;
    this.defaultModel = defaultModel;
    this.maxTokens = maxTokens;
  }

  @Override
  public ProviderId providerId() {
    return ProviderId.ANTHROPIC;
  }

  @Override
  public Result generate(List<ChatTurn> context, String userMessage, ModelId modelId) {
    String modelToUse = modelId != null && modelId.value() != null && !modelId.value().isBlank()
        ? modelId.value()
        : defaultModel;

    var messages = new ArrayList<MessagesCreateRequest.Message>();

    for (ChatTurn turn : context) {
      messages.add(new MessagesCreateRequest.Message(turn.role(), turn.content()));
    }
    messages.add(new MessagesCreateRequest.Message("user", userMessage));

    var req = new MessagesCreateRequest(modelToUse, maxTokens, messages);

    MessagesCreateResponse resp = webClient.post()
        .uri("/v1/messages")
        .bodyValue(req)
        .retrieve()
        .bodyToMono(MessagesCreateResponse.class)
        .block();

    String assistantText = extractAssistantText(resp);

    Integer promptTokens = resp != null && resp.usage() != null ? resp.usage().input_tokens() : null;
    Integer completionTokens = resp != null && resp.usage() != null ? resp.usage().output_tokens() : null;
    Integer totalTokens = (promptTokens != null && completionTokens != null) ? (promptTokens + completionTokens) : null;

    return new Result(assistantText, promptTokens, completionTokens, totalTokens);
  }

  private String extractAssistantText(MessagesCreateResponse resp) {
    if (resp == null || resp.content() == null) return "";

    StringBuilder sb = new StringBuilder();
    for (var block : resp.content()) {
      if (block == null) continue;
      if (!"text".equals(block.type())) continue;
      if (block.text() == null) continue;

      if (!sb.isEmpty()) sb.append("\n");
      sb.append(block.text());
    }
    return sb.toString();
  }
}
