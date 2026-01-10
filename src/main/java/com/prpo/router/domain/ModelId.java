package com.prpo.router.domain;

public record ModelId(String value) {
  public static ModelId of(String value) {
    return new ModelId(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
