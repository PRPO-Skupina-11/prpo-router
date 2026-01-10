package com.prpo.router.providers;

import com.prpo.router.domain.ProviderId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProviderRegistry {

  private final Map<ProviderId, ProviderClient> clients;

  public ProviderRegistry(List<ProviderClient> clients) {
    this.clients = new EnumMap<>(ProviderId.class);
    for (ProviderClient client : clients) {
      this.clients.put(client.providerId(), client);
    }
  }

  public ProviderClient get(ProviderId providerId) {
    return clients.get(providerId);
  }
}
