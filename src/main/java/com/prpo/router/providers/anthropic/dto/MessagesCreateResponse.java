package com.prpo.router.providers.anthropic.dto;

import java.util.List;

public record MessagesCreateResponse(
    String id,
    String type,
    String role,
    List<ContentBlock> content,
    String model,
    String stop_reason,
    Usage usage
) {
  public record ContentBlock(String type, String text) {}
  public record Usage(Integer input_tokens, Integer output_tokens) {}
}
