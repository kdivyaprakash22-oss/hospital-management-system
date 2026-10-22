package com.hospital;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class WebUtil {
    private static final String CSRF_ATTRIBUTE = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private WebUtil() {}

    public static String csrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_ATTRIBUTE);
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(CSRF_ATTRIBUTE, token);
        }
        return token;
    }

    public static boolean validCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object expected = session.getAttribute(CSRF_ATTRIBUTE);
        String supplied = request.getParameter("csrfToken");
        if (!(expected instanceof String) || supplied == null) return false;
        return MessageDigest.isEqual(
            ((String) expected).getBytes(StandardCharsets.UTF_8),
            supplied.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String escape(Object value) {
        if (value == null) return "";
        String text = String.valueOf(value);
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
}
