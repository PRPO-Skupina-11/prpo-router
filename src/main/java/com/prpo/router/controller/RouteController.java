package com.prpo.router.controller;

import com.prpo.router.api.RoutingApi;
import com.prpo.router.model.ProviderUsage;
import com.prpo.router.model.RouteDecision;
import com.prpo.router.model.RouteRequest;
import com.prpo.router.model.RouteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteController implements RoutingApi {

  @Override
  public ResponseEntity<RouteResponse> routeAndCall(RouteRequest request) {

    ProviderUsage usage = new ProviderUsage()
        .promptTokens(0)
        .completionTokens(0)
        .totalTokens(0);

    RouteDecision decision = new RouteDecision()
        .providerId("stub")
        .modelId("stub-v1")
        .reason("stub routing");

    RouteResponse response = new RouteResponse()
        .requestId(request.getRequestId())
        .providerId("stub")
        .modelId("stub-v1")
        .assistantContent("You said: " + request.getMessage())
        .latencyMs(1)
        .usage(usage)
        .estimatedCost(0.0)
        .currency("EUR")
        .decision(decision);

    return ResponseEntity.ok(response);
  }
}
