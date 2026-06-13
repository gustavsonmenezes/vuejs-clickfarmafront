package com.clickfarma.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    public GoogleUserInfo verifyToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Token Google inválido ou expirado");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setGoogleId(payload.getSubject());
            userInfo.setEmail(payload.getEmail());
            userInfo.setName((String) payload.get("name"));
            userInfo.setAvatarUrl((String) payload.get("picture"));

            return userInfo;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao verificar token Google: " + e.getMessage(), e);
        }
    }

    public static class GoogleUserInfo {
        private String googleId;
        private String email;
        private String name;
        private String avatarUrl;

        public String getGoogleId() { return googleId; }
        public void setGoogleId(String googleId) { this.googleId = googleId; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}
