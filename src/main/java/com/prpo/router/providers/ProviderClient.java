package com.prpo.router.providers;

import com.prpo.router.domain.ChatTurn;
import com.prpo.router.domain.ModelId;
import com.prpo.router.domain.ProviderId;
import java.util.List;

public interface ProviderClient {

  ProviderId providerId();

  Result generate(List<ChatTurn> context, String userMessage, ModelId modelId);

  record Result(
      String assistantText,
      Integer promptTokens,
      Integer completionTokens,
      Integer totalTokens
  ) {}
}
