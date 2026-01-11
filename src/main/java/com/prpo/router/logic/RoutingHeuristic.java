package com.prpo.router.logic;

import com.prpo.router.domain.ModelId;
import com.prpo.router.domain.ProviderId;
import org.springframework.stereotype.Component;
@Component
public class RoutingHeuristic {

  public Selection choose(String userMessage, ModelId requestedModelId) {
    int approxChars = userMessage != null ? userMessage.length() : 0;

    ProviderId providerId =
        approxChars >= 500 ? ProviderId.ANTHROPIC : ProviderId.OPENAI;

    ModelId modelId = requestedModelId;

    if (providerId == ProviderId.OPENAI) {
      modelId = chooseOpenAiModel(requestedModelId, approxChars);
    }

    return new Selection(providerId, modelId, approxChars);
  }

  private ModelId chooseOpenAiModel(ModelId requestedModelId, int approxChars) {
    if (requestedModelId != null && requestedModelId.value() != null && !requestedModelId.value().isBlank()) {
      return requestedModelId;
    }
    if (approxChars >= 100) return ModelId.of("gpt-5.2");
    return ModelId.of("gpt-5-mini");
  }

  public record Selection(ProviderId providerId, ModelId modelId, int approxChars) {}
}
