package com.streaker.utlis.logs;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

public class StatusCaptureWrapper extends HttpServletResponseWrapper {
    private int httpStatus = SC_OK;

    public StatusCaptureWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void setStatus(int sc) {
        super.setStatus(sc);
        httpStatus = sc;
    }

    @Override
    public void sendError(int sc) throws IOException {
        super.sendError(sc);
        httpStatus = sc;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        super.sendError(sc, msg);
        httpStatus = sc;
    }

    public int getStatusCode() {
        return httpStatus;
    }
}
