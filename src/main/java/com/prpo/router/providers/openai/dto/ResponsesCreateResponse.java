package com.prpo.router.providers.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponsesCreateResponse(
    String id,
    String model,
    List<OutputItem> output,
    Usage usage
) {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record OutputItem(
      String type,
      String role,
      List<ContentItem> content
  ) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ContentItem(
      String type,
      String text
  ) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record Usage(
      Integer input_tokens,
      Integer output_tokens,
      Integer total_tokens
  ) {}
}
