package com.lms.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.lms.dto.GoogleUserDTO;

/**
 * Thin transport layer for Google's OAuth 2.0 authorization-code flow.
 * Talks to Google only - no DB access, no session handling. The servlet
 * hands the resulting GoogleUserDTO to UserService, which owns all the
 * account-provisioning / linking logic.
 *
 * Any network or protocol failure surfaces as a plain IOException so the
 * servlet can show a generic "try again" error instead of leaking transport
 * details to the browser.
 */
public class GoogleOAuthClient {

    private static final String AUTHORIZE_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean isConfigured() {
        return AppConfig.isConfigured("google.client.id", "google.client.secret", "google.redirect.uri");
    }

    /** Builds the URL to redirect the browser to in order to start consent. */
    public String buildAuthorizeUrl(String state) {
        String clientId = AppConfig.get("google.client.id");
        String redirectUri = AppConfig.get("google.redirect.uri");

        return AUTHORIZE_ENDPOINT
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&state=" + encode(state)
                + "&prompt=select_account";
    }

    /**
     * Exchanges an authorization code for tokens, then fetches the profile.
     * Throws IOException on any HTTP/network/parse failure.
     */
    public GoogleUserDTO exchangeCodeForProfile(String code) throws IOException {
        String accessToken = exchangeCodeForAccessToken(code);
        return fetchUserInfo(accessToken);
    }

    private String exchangeCodeForAccessToken(String code) throws IOException {
        String clientId = AppConfig.get("google.client.id");
        String clientSecret = AppConfig.get("google.client.secret");
        String redirectUri = AppConfig.get("google.redirect.uri");

        String form = "code=" + encode(code)
                + "&client_id=" + encode(clientId)
                + "&client_secret=" + encode(clientSecret)
                + "&redirect_uri=" + encode(redirectUri)
                + "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_ENDPOINT))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new IOException("Google token exchange failed: HTTP " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!json.has("access_token")) {
            throw new IOException("Google token exchange response missing access_token");
        }
        return json.get("access_token").getAsString();
    }

    private GoogleUserDTO fetchUserInfo(String accessToken) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERINFO_ENDPOINT))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = send(request);
        if (response.statusCode() != 200) {
            throw new IOException("Google userinfo request failed: HTTP " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        GoogleUserDTO dto = new GoogleUserDTO();
        dto.setSub(getAsString(json, "sub"));
        dto.setEmail(getAsString(json, "email"));
        dto.setEmailVerified(json.has("email_verified") && json.get("email_verified").getAsBoolean());
        dto.setName(getAsString(json, "name"));
        dto.setPicture(getAsString(json, "picture"));
        return dto;
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Google request interrupted", e);
        }
    }

    private String getAsString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
