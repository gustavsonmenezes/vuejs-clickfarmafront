package com.clickfarma.backend.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UberDirectQuoteResponseDTO {

    private String quoteId;
    private double deliveryFee;
    private String currency;
    private String estimatedTime;
    private double estimatedDistanceKm;
    private String expiresAt;

    public UberDirectQuoteResponseDTO() {}

    public UberDirectQuoteResponseDTO(String quoteId, double deliveryFee, String currency, String estimatedTime) {
        this.quoteId = quoteId;
        this.deliveryFee = deliveryFee;
        this.currency = currency;
        this.estimatedTime = estimatedTime;
    }

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public double getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(double estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}
