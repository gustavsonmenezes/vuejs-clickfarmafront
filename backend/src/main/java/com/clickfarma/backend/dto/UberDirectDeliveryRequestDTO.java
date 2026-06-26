package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class UberDirectDeliveryRequestDTO {

    @NotBlank
    private String quoteId;

    private UberDirectContactDTO dropoff;

    @NotEmpty
    private List<UberDirectManifestItemDTO> manifestItems;

    private String externalOrderId;

    public String getQuoteId() { return quoteId; }
    public void setQuoteId(String quoteId) { this.quoteId = quoteId; }

    public UberDirectContactDTO getDropoff() { return dropoff; }
    public void setDropoff(UberDirectContactDTO dropoff) { this.dropoff = dropoff; }

    public List<UberDirectManifestItemDTO> getManifestItems() { return manifestItems; }
    public void setManifestItems(List<UberDirectManifestItemDTO> manifestItems) { this.manifestItems = manifestItems; }

    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
}
