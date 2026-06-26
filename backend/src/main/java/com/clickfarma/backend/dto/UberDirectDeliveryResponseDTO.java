package com.clickfarma.backend.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UberDirectDeliveryResponseDTO {

    private String id;
    private String quoteId;
    private String status;
    private String trackingUrl;
    private String externalOrderId;
    private UberDirectContactDTO pickup;
    private UberDirectContactDTO dropoff;
    private UberDirectCourierDTO courier;
    private List<UberDirectManifestItemDTO> manifestItems;
    private String estimatedDelivery;
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTrackingUrl() { return trackingUrl; }
    public void setTrackingUrl(String trackingUrl) { this.trackingUrl = trackingUrl; }

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }

    public UberDirectContactDTO getPickup() { return pickup; }
    public void setPickup(UberDirectContactDTO pickup) { this.pickup = pickup; }

    public UberDirectContactDTO getDropoff() { return dropoff; }
    public void setDropoff(UberDirectContactDTO dropoff) { this.dropoff = dropoff; }

    public UberDirectCourierDTO getCourier() { return courier; }
    public void setCourier(UberDirectCourierDTO courier) { this.courier = courier; }

    public List<UberDirectManifestItemDTO> getManifestItems() { return manifestItems; }
    public void setManifestItems(List<UberDirectManifestItemDTO> manifestItems) { this.manifestItems = manifestItems; }

    public String getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(String estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
