package com.duoc.frontend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtCookieService {

    private final String cookieName;
    private final boolean secure;
    private final long maxAgeSeconds;
    private final String sameSite;

    public JwtCookieService(
            @Value("${app.security.jwt-cookie.name}") String cookieName,
            @Value("${app.security.jwt-cookie.secure}") boolean secure,
            @Value("${app.security.jwt-cookie.max-age-seconds}") long maxAgeSeconds,
            @Value("${app.security.jwt-cookie.same-site}") String sameSite) {
        this.cookieName = cookieName;
        this.secure = secure;
        this.maxAgeSeconds = maxAgeSeconds;
        this.sameSite = sameSite;
    }

    public ResponseCookie createAuthCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }

    public ResponseCookie clearAuthCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();
    }

    public String extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
