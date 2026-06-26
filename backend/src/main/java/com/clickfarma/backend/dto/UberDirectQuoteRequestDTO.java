package com.clickfarma.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UberDirectQuoteRequestDTO {

    @NotBlank
    private String dropoffAddress;

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }
}
