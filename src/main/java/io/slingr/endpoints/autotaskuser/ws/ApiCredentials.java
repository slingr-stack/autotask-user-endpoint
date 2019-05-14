package io.slingr.endpoints.autotaskuser.ws;

import io.slingr.endpoints.utils.Json;

public class ApiCredentials {
    private String username;
    private String password;
    private String integrationCode;
    private String zoneUrl;
    private String webUrl;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        // there is a silly bug in Autotask where it has issues if the password is longer than 25
        // characters; however if you just send the first part of the password it works perfectly
        if (password != null && password.length() > 25) {
            return password.substring(0, 25);
        }
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIntegrationCode() {
        return integrationCode;
    }

    public void setIntegrationCode(String integrationCode) {
        this.integrationCode = integrationCode;
    }

    public String getZoneUrl() {
        return zoneUrl;
    }

    public void setZoneUrl(String zoneUrl) {
        this.zoneUrl = zoneUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public Json toJson() {
        return Json.map()
                .set("username", username)
                .set("password", password)
                .set("integrationCode", integrationCode)
                .set("zoneUrl", zoneUrl)
                .set("webUrl", webUrl);
    }

    public void fromJson(Json json) {
        username = json.string("username");
        password = json.string("password");
        integrationCode = json.string("integrationCode");
        zoneUrl = json.string("zoneUrl");
        webUrl = json.string("webUrl");
    }
}
