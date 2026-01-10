package com.prpo.router.domain;

public record RouteResult(
    String requestId,
    ProviderId providerId,
    ModelId modelId,
    String assistantContent,
    Integer promptTokens,
    Integer completionTokens,
    Integer totalTokens,
    Long latencyMs,
    RouteDecisionInfo decision
) {}
