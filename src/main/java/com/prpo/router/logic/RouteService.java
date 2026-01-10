package com.prpo.router.logic;

import com.prpo.router.domain.ChatTurn;
import com.prpo.router.domain.ModelId;
import com.prpo.router.domain.ProviderId;
import com.prpo.router.model.ProviderUsage;
import com.prpo.router.model.RouteDecision;
import com.prpo.router.model.RouteRequest;
import com.prpo.router.model.RouteResponse;
import com.prpo.router.providers.ProviderClient;
import com.prpo.router.providers.ProviderRegistry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

  private final ProviderRegistry providerRegistry;
  private final String defaultOpenAiModel;

  public RouteService(
      ProviderRegistry providerRegistry,
      @Value("${openai.model}") String defaultOpenAiModel
  ) {
    this.providerRegistry = providerRegistry;
    this.defaultOpenAiModel = defaultOpenAiModel;
  }

  public RouteResponse routeAndCall(RouteRequest request) {
    long t0 = System.currentTimeMillis();

    List<ChatTurn> context = mapContext(request);

    ProviderId providerId = ProviderId.OPENAI;
    ModelId modelId = ModelId.of(defaultOpenAiModel);

    ProviderClient client = providerRegistry.get(providerId);
    ProviderClient.Result result = client.generate(context, request.getMessage(), modelId);

    long latencyMs = System.currentTimeMillis() - t0;

    ProviderUsage usage = new ProviderUsage()
        .promptTokens(nullToZero(result.promptTokens()))
        .completionTokens(nullToZero(result.completionTokens()))
        .totalTokens(nullToZero(result.totalTokens()));

    RouteDecision decision = new RouteDecision()
        .providerId(providerId.name().toLowerCase())
        .modelId(modelId.value())
        .reason("v1 default route");

    RouteResponse response = new RouteResponse()
        .requestId(request.getRequestId())
        .providerId(providerId.name().toLowerCase())
        .modelId(modelId.value())
        .assistantContent(result.assistantText())
        .latencyMs((int) latencyMs)
        .usage(usage)
        .estimatedCost(0.0)
        .currency("EUR")
        .decision(decision);

    return response;
  }

  private List<ChatTurn> mapContext(RouteRequest request) {
    var out = new ArrayList<ChatTurn>();
    if (request.getContext() == null) return out;

    for (var msg : request.getContext()) {
      if (msg == null) continue;

      String role = msg.getRole() != null
          ? msg.getRole().toString().toLowerCase()
          : "user";

      String content = msg.getContent() != null ? msg.getContent() : "";

      out.add(new ChatTurn(role, content));
    }

    return out;
  }

  private int nullToZero(Integer v) {
    return v == null ? 0 : v;
  }
}
