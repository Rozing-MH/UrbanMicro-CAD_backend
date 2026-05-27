package com.urbanmicrocad.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanmicrocad.common.exception.RequestBodyTooLargeException;
import com.urbanmicrocad.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class RequestBodySizeFilter extends OncePerRequestFilter {
    private static final long MAX_API_BODY_BYTES = 2_100_000;
    private static final Set<String> BODY_METHODS = Set.of(
        HttpMethod.POST.name(),
        HttpMethod.PUT.name(),
        HttpMethod.PATCH.name()
    );

    private final ObjectMapper objectMapper;

    public RequestBodySizeFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/") || !BODY_METHODS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        if (request.getContentLengthLong() > MAX_API_BODY_BYTES) {
            writePayloadTooLarge(response);
            return;
        }
        try {
            filterChain.doFilter(new SizeLimitedHttpServletRequest(request, MAX_API_BODY_BYTES), response);
        } catch (RequestBodyTooLargeException ex) {
            writePayloadTooLarge(response);
        }
    }

    private void writePayloadTooLarge(HttpServletResponse response) throws IOException {
        response.setStatus(413);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(413, "请求体过大"));
    }
}
