package com.lms.dto;

/**
 * Maps the fields we need from Google's OpenID "userinfo" response.
 * Populated by util.GoogleOAuthClient (via Gson) and handed to
 * UserService.loginWithGoogle - the servlet never touches the DB itself.
 */
public class GoogleUserDTO {

    private String sub;
    private String email;
    private boolean emailVerified;
    private String name;
    private String picture;

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
