package com.prpo.router.providers.openai;

import com.prpo.router.domain.ChatTurn;
import com.prpo.router.domain.ModelId;
import com.prpo.router.domain.ProviderId;
import com.prpo.router.providers.ProviderClient;
import com.prpo.router.providers.openai.dto.ResponsesCreateRequest;
import com.prpo.router.providers.openai.dto.ResponsesCreateResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAIClient implements ProviderClient {

  private final WebClient webClient;
  private final String defaultModel;

  public OpenAIClient(
      @Qualifier("openaiWebClient") WebClient openaiWebClient,
      @Value("${openai.model}") String defaultModel
  ) {
    this.webClient = openaiWebClient;
    this.defaultModel = defaultModel;
  }

  @Override
  public ProviderId providerId() {
    return ProviderId.OPENAI;
  }

  @Override
  public Result generate(List<ChatTurn> context, String userMessage, ModelId modelId) {
    String modelToUse = modelId != null && modelId.value() != null && !modelId.value().isBlank()
        ? modelId.value()
        : defaultModel;

    var input = new ArrayList<ResponsesCreateRequest.InputItem>();

    for (ChatTurn turn : context) {
      input.add(toMessageItem(turn.role(), turn.content()));
    }
    input.add(toMessageItem("user", userMessage));

    var req = new ResponsesCreateRequest(
        modelToUse,
        input,
        false,
        false,
        null
    );

    ResponsesCreateResponse resp;
    try {
      resp = webClient.post()
          .uri("/responses")
          .bodyValue(req)
          .retrieve()
          .bodyToMono(ResponsesCreateResponse.class)
          .block();
    } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
      System.err.println("=== OPENAI ERROR ===");
      System.err.println("status: " + e.getStatusCode());
      System.err.println("body: " + e.getResponseBodyAsString());
      System.err.println("====================");
      throw e;
    }



    String assistantText = extractAssistantText(resp);

    Integer promptTokens = resp != null && resp.usage() != null ? resp.usage().input_tokens() : null;
    Integer completionTokens = resp != null && resp.usage() != null ? resp.usage().output_tokens() : null;
    Integer totalTokens = resp != null && resp.usage() != null ? resp.usage().total_tokens() : null;

    return new Result(assistantText, promptTokens, completionTokens, totalTokens);
  }

  private ResponsesCreateRequest.InputItem toMessageItem(String role, String text) {
    String contentType = "assistant".equals(role) ? "output_text" : "input_text";
    var content = List.of(new ResponsesCreateRequest.ContentItem(contentType, text));
    return new ResponsesCreateRequest.InputItem("message", role, content);
  }

  private String extractAssistantText(ResponsesCreateResponse resp) {
    if (resp == null || resp.output() == null) return "";

    for (var item : resp.output()) {
      if (!"message".equals(item.type())) continue;
      if (!"assistant".equals(item.role())) continue;
      if (item.content() == null) continue;

      for (var c : item.content()) {
        if ("output_text".equals(c.type()) && c.text() != null) {
          return c.text();
        }
      }
    }
    return "";
  }
}
