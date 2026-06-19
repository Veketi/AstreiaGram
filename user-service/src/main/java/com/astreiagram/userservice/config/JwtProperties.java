package com.astreiagram.userservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
    private String serviceSecret;

    public String getSecret()              { return secret; }
    public void setSecret(String secret)   { this.secret = secret; }

    public long getExpirationMs()                  { return expirationMs; }
    public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }

    public String getServiceSecret()                   { return serviceSecret; }
    public void setServiceSecret(String serviceSecret) { this.serviceSecret = serviceSecret; }
}
