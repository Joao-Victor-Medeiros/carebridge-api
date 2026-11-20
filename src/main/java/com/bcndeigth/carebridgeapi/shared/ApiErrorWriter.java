package com.bcndeigth.carebridgeapi.shared;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.UUID;

public final class ApiErrorWriter {
    private ApiErrorWriter() {
    }

    public static void write(HttpServletResponse response, int statusCode, String error, String message)
            throws IOException {
        response.setStatus(statusCode);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"statusCode\":" + statusCode
                + ",\"message\":\"" + escape(message)
                + "\",\"error\":\"" + escape(error)
                + "\",\"requestId\":\"" + UUID.randomUUID() + "\"}");
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}