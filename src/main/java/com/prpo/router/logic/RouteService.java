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
import com.prpo.router.logic.PricingService.PricePer1k;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

  private static final ModelId OPENAI_DEFAULT = ModelId.of("gpt-5-mini");
  private static final ModelId ANTHROPIC_DEFAULT = ModelId.of("claude-sonnet-4-5");

  private final ProviderRegistry providerRegistry;
  private final RoutingHeuristic heuristic;
  private final PricingService pricingService;

  public RouteService(
      ProviderRegistry providerRegistry,
      RoutingHeuristic heuristic,
      PricingService pricingService
  ) {
    this.providerRegistry = providerRegistry;
    this.heuristic = heuristic;
    this.pricingService = pricingService;
  }

  public RouteResponse routeAndCall(RouteRequest request) {
    long t0 = System.currentTimeMillis();

    System.err.println("ROUTE requestId=" + request.getRequestId() + " messageLen=" +
    (request.getMessage() == null ? 0 : request.getMessage().length()));

    List<ChatTurn> context = mapContext(request);

    ProviderId providerId = null;
    ModelId modelId = null;

    var overrides = request.getModelOverrides();
    String forceProviderId = overrides != null ? overrides.getForceProviderId() : null;
    String forceModelId = overrides != null ? overrides.getForceModelId() : null;

    if (forceProviderId != null && !forceProviderId.isBlank()) {
      providerId = ProviderId.valueOf(forceProviderId.trim().toUpperCase());
    }

    if (forceModelId != null && !forceModelId.isBlank()) {
      modelId = ModelId.of(forceModelId.trim());
    }

    RoutingHeuristic.Selection selection = null;
    if (providerId == null) {
      selection = heuristic.choose(request.getMessage(), modelId);
      providerId = selection.providerId();
      modelId = selection.modelId();
    }

    if (providerId != null && modelId == null) {
      if (providerId == ProviderId.OPENAI) {
        selection = heuristic.choose(request.getMessage(), modelId);
        modelId = selection.modelId();
      } else {
        modelId = null;
      }
    }

    if (providerId == ProviderId.OPENAI && modelId == null) {
      modelId = OPENAI_DEFAULT;
    }

    if (providerId == ProviderId.ANTHROPIC && modelId == null) {
      modelId = ANTHROPIC_DEFAULT;
    }

    ProviderClient client = providerRegistry.get(providerId);
    if (client == null) {
      providerId = ProviderId.OPENAI;
      client = providerRegistry.get(providerId);
    }

    ProviderClient.Result result = client.generate(context, request.getMessage(), modelId);

    long latencyMs = System.currentTimeMillis() - t0;

    ProviderUsage usage = new ProviderUsage()
        .promptTokens(nullToZero(result.promptTokens()))
        .completionTokens(nullToZero(result.completionTokens()))
        .totalTokens(nullToZero(result.totalTokens()));

    String providerIdStr = providerId.name().toLowerCase();
    String modelIdStr = modelId != null ? modelId.value() : null;

    Double cost = computeCostEur(providerIdStr, modelIdStr, result.promptTokens(), result.completionTokens());

    RouteDecision decision = new RouteDecision()
        .providerId(providerIdStr)
        .modelId(modelId != null ? modelId.value() : null)
        .reason("v1 heuristic: chars=" + selection.approxChars())
        .candidates(null);

    RouteResponse response = new RouteResponse()
        .requestId(request.getRequestId())
        .providerId(providerIdStr)
        .modelId(modelId != null ? modelId.value() : null)
        .assistantContent(result.assistantText())
        .latencyMs((int) latencyMs)
        .usage(usage)
        .estimatedCost(cost)
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

  private Double computeCostEur(String providerId, String modelId, Integer promptTokens, Integer completionTokens) {
    if (providerId == null || modelId == null) return null;
    if (promptTokens == null || completionTokens == null) return null;

    PricePer1k price = pricingService.getPrice(providerId, modelId);
    if (price == null) return null;
    if (price.prompt() == null || price.completion() == null) return null;

    double promptCost = (promptTokens / 1000.0) * price.prompt().doubleValue();
    double completionCost = (completionTokens / 1000.0) * price.completion().doubleValue();
    return promptCost + completionCost;
  }

}
