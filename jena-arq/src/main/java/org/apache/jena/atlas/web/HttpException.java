/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.atlas.web;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

import org.apache.jena.web.HttpSC;

/**
 * Class of HTTP Exceptions
 */
public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String statusLine;
    // The body of the response, if any.
    private final HttpHeaders responseHeaders;
    private final String responseBody;

    /** System error setting up the HTTP request */
    public static HttpException error(String exceptionMessage) {
        return new HttpException(exceptionMessage);
    }

    /** System error setting up the HTTP request */
    public static HttpException error(String exceptionMessage, Throwable cause) {
        return new HttpException(exceptionMessage, cause);
    }

    /** HTTP error */
    public static HttpException create(int httpStatusCode) {
        return HttpException.builder()
                .statusCode(httpStatusCode)
                .build();
    }

    /** HTTP error */
    public static HttpException create(HttpResponse<?> response) {
        return HttpException.builder()
                .statusCode(response.statusCode())
                .httpHeaders(response.headers())
                .build();
    }

    /**
     * Replicate the details of an {@code HttpException};
     * the stacktrace will be the callers location.
     */
    public static HttpException create(HttpException other) {
        return HttpException.builder()
                .statusCode(other.getStatusCode())
                .statusLine(other.getStatusLine())
                .responseMessage(other.getResponse())
                .cause(other.getCause())
                .build();
    }

    public static Builder builder() {
        return new HttpException.Builder();
    }

    public static class Builder {
        private int statusCode = -1;
        private String statusLine = null;
        private String responseMessage = null;
        private Throwable cause = null;
        private HttpHeaders httpHeaders = null;

        public Builder() {}

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder statusLine(String statusLine) {
            this.statusLine = statusLine;
            return this;
        }

        public Builder responseMessage(String responseMessage) {
            this.responseMessage = responseMessage;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder httpHeaders(HttpHeaders httpHeaders) {
            this.httpHeaders = httpHeaders;
            return this;
        }
        public HttpException build() {
            return new HttpException(statusCode, statusLine, responseMessage, cause);
        }
    }

    // HTTP/2 does not have an information message.

    /** @deprecated Use {@link HttpException#create(int)} */
    @Deprecated
    public HttpException(int statusCode) {
        this(statusCode, null, null, null, null);
    }

    /** @deprecated Use {@link HttpException#builder()} */
    @Deprecated
    public HttpException(int statusCode, String statusLine) {
        this(statusCode, statusLine, null, null, null);
    }

    /** @deprecated Use {@link HttpException#create(HttpResponse)} or {@link HttpException#builder()} */
    @Deprecated
    public HttpException(int statusCode, String statusLine, String responseMessage) {
        this(statusCode, statusLine, null, responseMessage, null);
    }

    /** @deprecated Use {@link HttpException#create(HttpResponse)} or {@link HttpException#builder()} */
    @Deprecated
    public HttpException(int statusCode, String statusLine, String responseMessage, Throwable cause) {
        this(statusCode, statusLine, null, responseMessage, cause);
    }

    private HttpException(int statusCode, String statusLine, HttpHeaders responseHttpHeaders, String responseBody, Throwable cause) {
        super(exMessage(statusCode, statusLine), cause);
        this.statusCode = statusCode;
        this.statusLine = statusLine ;
        this.responseHeaders = responseHttpHeaders;
        this.responseBody = responseBody;
    }

    private static String exMessage(int statusCode, String statusLine) {
        if ( statusLine == null )
            statusLine = HttpSC.getMessage(statusCode);
        return statusCode+" - "+HttpSC.getMessage(statusCode);
    }

    /** @deprecated Use {@link HttpException#error(String)} */
    @Deprecated
    private HttpException(String message) {
        super(message);
        this.statusCode = -1;
        this.statusLine = null ;
        this.responseHeaders = null;
        this.responseBody = null;
    }

    /** @deprecated Use {@link HttpException#error(String, Throwable)} */
    @Deprecated
    private HttpException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.statusLine = null ;
        this.responseHeaders = null;
        this.responseBody = null;
    }

    /** @deprecated Use {@link HttpException#builder()} */
    @Deprecated
    public HttpException(Throwable cause) {
        super(cause);
        this.statusCode = -1;
        this.statusLine = null ;
        this.responseHeaders = null;
        this.responseBody = null;
    }

    /**
     * Gets the status code, may be -1 if unknown
     * @return Status Code if known, -1 otherwise
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the status line text, may be null if unknown.
     * HTTP/2 does not have status line text; the default HTTP 1.1 message is returned.
     * @return Status line
     */
    public String getStatusLine() {
        return this.statusLine;
    }

    /**
     * The response payload from the remote.
     * @return The payload, or null if no payload
     */
    public String getResponse() {
        return responseBody;
    }

    /**
     * The response headers.
     */
    public HttpHeaders getHttpResponseHeader() {
        return responseHeaders;
    }

}
