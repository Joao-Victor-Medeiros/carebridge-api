package com.bcndeigth.carebridgeapi.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ApiErrorWriter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private ApiErrorWriter() {
    }

    public static String resolveRequestId(HttpServletRequest request) {
        if (request != null) {
            String headerId = request.getHeader(REQUEST_ID_HEADER);
            if (headerId != null && !headerId.isBlank()) {
                return headerId.trim();
            }
        }
        return UUID.randomUUID().toString();
    }

    public static void write(HttpServletResponse response, int statusCode, String error, String message)
            throws IOException {
        write(response, null, statusCode, error, message);
    }

    public static void write(
            HttpServletResponse response,
            HttpServletRequest request,
            int statusCode,
            String error,
            String message
    ) throws IOException {
        String requestId = resolveRequestId(request);
        response.setStatus(statusCode);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        response.getWriter().write("{\"statusCode\":" + statusCode
                + ",\"message\":\"" + escape(message)
                + "\",\"error\":\"" + escape(error)
                + "\",\"requestId\":\"" + escape(requestId) + "\"}");
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\r' -> sb.append("\\r");
                case '\n' -> sb.append("\\n");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}