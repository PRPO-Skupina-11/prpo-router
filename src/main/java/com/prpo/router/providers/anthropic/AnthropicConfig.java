package com.prpo.router.providers.anthropic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AnthropicConfig {

  @Bean(name = "anthropicWebClient")
  WebClient anthropicWebClient(
      WebClient.Builder builder,
      @Value("${anthropic.base-url}") String baseUrl,
      @Value("${anthropic.api-key}") String apiKey,
      @Value("${anthropic.version:2023-06-01}") String anthropicVersion
  ) {
    return builder
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("x-api-key", apiKey)
        .defaultHeader("anthropic-version", anthropicVersion)
        .build();
  }
}
