package com.prpo.router.domain;

public record RouteDecisionInfo(
    ProviderId providerId,
    ModelId modelId,
    String reason
) {}
