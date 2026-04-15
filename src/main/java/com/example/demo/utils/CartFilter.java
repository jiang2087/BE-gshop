package com.example.demo.utils;

import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CartFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String cartKey = getCartKey(req);

        if (cartKey == null) {
            cartKey = "guest_" + UUID.randomUUID();

            Cookie cookie = new Cookie("cartKey", cartKey);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);

            res.addCookie(cookie);
        }

        req.setAttribute("cartKey", cartKey);

        chain.doFilter(request, response);
    }

    private String getCartKey(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie c : request.getCookies()) {
            if ("cartKey".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}