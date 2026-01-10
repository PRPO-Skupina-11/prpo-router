package com.prpo.router.providers.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponsesCreateRequest(
    String model,
    List<InputItem> input,
    Boolean stream,
    Boolean store,
    Integer max_output_tokens
) {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record InputItem(
      String type,
      String role,
      List<ContentItem> content
  ) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ContentItem(
      String type,
      String text
  ) {}
}
