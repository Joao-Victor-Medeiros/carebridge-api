package com.bcndeigth.carebridgeapi.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

public final class ApiErrorWriter {

    private static final JsonMapper JSON = JsonMapper.builder().findAndAddModules().build();

    private ApiErrorWriter() {
    }

    public static void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
                             String message) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        JSON.writeValue(response.getWriter(), ErrorResponse.of(status, message, request.getRequestURI()));
    }
}
