/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.sparql.engine.http;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.QueryException;
import org.apache.jena.web.HttpSC;

/**
 * Exception class for all operations in the SPARQL client library. Error codes are
 * as HTTP status codes.
 */
public class QueryExceptionHTTP extends QueryException
{
    public static final int noStatusCode = -1234;
    private int statusCode = noStatusCode;
    private final String responseMessage;
    private String statusLine;
    private String response;

    // Codes for extra errors.  We use HTTP error codes so
    // these are negative to avoid clashes
    public static final int NoServer = -404;

    public static QueryExceptionHTTP rewrap(HttpException httpEx) {
        // The historical contract of HTTP Queries has been to throw QueryExceptionHTTP however using the standard
        // ARQ machinery we use these days means the internal HTTP errors come back as HttpException
        // Therefore we need to wrap appropriately
        int responseCode = httpEx.getStatusCode();
        if (responseCode != -1) {
            // Was an actual HTTP error
            String responseLine = httpEx.getStatusLine() != null ? httpEx.getStatusLine() : HttpSC.getMessage(responseCode);
            return new QueryExceptionHTTP(responseCode, responseLine, httpEx);
        } else if (httpEx.getMessage() != null) {
            // Some non-HTTP error with a valid message e.g. Socket Communications failed, IO error
            return new QueryExceptionHTTP(responseCode, "Unexpected error making the query: " + httpEx.getMessage(), httpEx);
        } else if (httpEx.getCause() != null) {
            // Some other error with a cause e.g. Socket Communications failed, IO error
            return new QueryExceptionHTTP(responseCode, "Unexpected error making the query, see cause for further details", httpEx);
        } else {
            // Some other error with no message and no further cause
            return new QueryExceptionHTTP(responseCode, "Unexpected error making the query", httpEx);
        }
    }

    /**
     * Constructor for QueryExceptionHTTP.
     * @param responseCode
     * @param responseMessage
     */
    public QueryExceptionHTTP(int responseCode, String responseMessage) {
        super(responseMessage);
        this.statusCode = responseCode;
        this.responseMessage = responseMessage;
    }

    /**
     * Constructor for QueryExceptionHTTP.
     * @param responseCode
     */
    public QueryExceptionHTTP(int responseCode) {
        super();
        this.statusCode = responseCode;
        this.responseMessage = null;
    }

    /** The code for the reason for this exception
     * @return statusCode
     */
    public int getStatusCode() { return statusCode; }

    /** The message for the reason for this exception
     * @return message
     */
    public String getResponseMessage() { return responseMessage; }

    /** The response for this exception if available from HTTP
     * @return response or {@code null} if no HTTP response was received
     */
    public String getResponse() { return response; }

    /** The status line for the response for this exception if available from HTTP
     * @return status line or {@code null} if no HTTP response was received
     */
    public String getStatusLine() { return statusLine; }

    /**
     * Constructor for HttpException used for some unexpected execution error.
     * @param cause
     */
    public QueryExceptionHTTP(Throwable cause) {
        super(cause);
        this.statusCode = noStatusCode;
        this.responseMessage = null;
    }

    public QueryExceptionHTTP(String msg, Throwable cause) {
        super(msg, cause);
        this.statusCode = noStatusCode;
        this.responseMessage = msg;
    }

    public QueryExceptionHTTP(int responseCode, String message, Throwable cause) {
        this(message, cause);
        this.statusCode = responseCode;
    }

    public QueryExceptionHTTP(int responseCode, String message, final HttpException ex) {
        this(responseCode, message, ex.getCause());
        this.statusLine = ex.getStatusLine();
        this.response = ex.getResponse();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpException: ");
        int code = getStatusCode();
        if ( code != QueryExceptionHTTP.noStatusCode ) {
            sb.append(code);
            if ( getResponseMessage() != null ) {
                sb.append(" ");
                sb.append(getResponseMessage());
            }
        } else {
            sb.append(getCause().toString() + ": " + getMessage());
        }
        return sb.toString();
    }
}
