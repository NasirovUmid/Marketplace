package com.pm.commonevents.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;


import java.net.URI;
import java.time.Instant;


public final class ApiProblem {
    public static ProblemDetail of(HttpStatus httpStatus, String code, String detail, HttpServletRequest httpServlet, Throwable ex) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(httpStatus, detail);
        pd.setTitle(httpStatus.getReasonPhrase());
        pd.setType(URI.create("about:blank: " + code));

        if (httpServlet != null) pd.setInstance(URI.create(httpServlet.getRequestURI()));

        pd.setProperty("code", code);
        pd.setProperty("timestamp", Instant.now().toString());
        pd.setProperty("method", httpServlet != null ? httpServlet.getMethod() : null);

        pd.setProperty("requestId", UUID.randomUUID().toString());

        if (ex != null) {
            pd.setProperty("exception", ex.getClass().getSimpleName());
        }

        if (ex != null) {
            pd.setProperty("exception", ex.getClass().getSimpleName());
        }

        return pd;
    }
}
