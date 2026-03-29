package com.example.ordersystem.shared.api.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name", "api", "external"})
public record RootResponse(String name, ApiLinks api, ExternalLinks external) {

  @JsonPropertyOrder({"docs", "health"})
  public record ApiLinks(String docs, String health) {}

  @JsonPropertyOrder({"kafkaUi"})
  public record ExternalLinks(String kafkaUi) {}
}
