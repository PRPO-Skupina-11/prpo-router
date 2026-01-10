package com.prpo.router.api;

import com.prpo.router.logic.RouteService;
import com.prpo.router.model.RouteRequest;
import com.prpo.router.model.RouteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouterController implements RoutingApi {

  private final RouteService routeService;

  public RouterController(RouteService routeService) {
    this.routeService = routeService;
  }

  @Override
  public ResponseEntity<RouteResponse> routeAndCall(RouteRequest request) {
    return ResponseEntity.ok(routeService.routeAndCall(request));
  }
}
