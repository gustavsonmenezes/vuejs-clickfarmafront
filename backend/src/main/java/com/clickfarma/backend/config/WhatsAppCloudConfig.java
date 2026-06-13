package com.clickfarma.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "whatsapp.cloud")
public class WhatsAppCloudConfig {

    private boolean enabled = false;
    private String phoneNumberId = "";
    private String accessToken = "";
    private String apiUrl = "https://graph.facebook.com/v22.0";
    private String businessPhone = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getPhoneNumberId() { return phoneNumberId; }
    public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getBusinessPhone() { return businessPhone; }
    public void setBusinessPhone(String businessPhone) { this.businessPhone = businessPhone; }
}
