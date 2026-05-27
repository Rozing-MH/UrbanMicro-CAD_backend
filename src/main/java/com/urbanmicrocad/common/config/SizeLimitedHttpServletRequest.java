package com.urbanmicrocad.common.config;

import com.urbanmicrocad.common.exception.RequestBodyTooLargeException;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SizeLimitedHttpServletRequest extends HttpServletRequestWrapper {
    private final long maxBytes;

    public SizeLimitedHttpServletRequest(HttpServletRequest request, long maxBytes) {
        super(request);
        this.maxBytes = maxBytes;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new SizeLimitedServletInputStream(super.getInputStream(), maxBytes);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    private static class SizeLimitedServletInputStream extends ServletInputStream {
        private final ServletInputStream delegate;
        private final long maxBytes;
        private long bytesRead;

        private SizeLimitedServletInputStream(ServletInputStream delegate, long maxBytes) {
            this.delegate = delegate;
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value != -1) {
                countBytes(1);
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int read = delegate.read(buffer, offset, length);
            if (read > 0) {
                countBytes(read);
            }
            return read;
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }

        private void countBytes(int count) throws RequestBodyTooLargeException {
            bytesRead += count;
            if (bytesRead > maxBytes) {
                throw new RequestBodyTooLargeException();
            }
        }
    }
}
