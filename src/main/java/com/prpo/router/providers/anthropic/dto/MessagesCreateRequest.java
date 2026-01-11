package com.prpo.router.providers.anthropic.dto;

import java.util.List;

public record MessagesCreateRequest(
    String model,
    Integer max_tokens,
    List<Message> messages
) {
  public record Message(String role, String content) {}
}
