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

package org.apache.jena.atlas.web;

import org.apache.jena.web.HttpSC;

/**
 * Class of HTTP Exceptions
 */
public class HttpException extends RuntimeException {
    private final int statusCode;
    private final String statusLine;
	private final String response;

	// HTTP/2 does not have an information message.
	public HttpException(int statusCode) {
	    this(statusCode, null);
	}

	public HttpException(int statusCode, String statusLine) {
		super(exMessage(statusCode, statusLine));
		this.statusCode = statusCode;
		this.statusLine = statusLine ;
		this.response = null;
	}

    public HttpException(int statusCode, String statusLine, String response) {
        super(exMessage(statusCode, statusLine));
        this.statusCode = statusCode;
        this.statusLine = statusLine ;
        this.response = response;
    }

	private static String exMessage(int statusCode, String statusLine) {
	    if ( statusLine == null )
	        statusLine = HttpSC.getMessage(statusCode);
	    return statusCode+" - "+HttpSC.getMessage(statusCode);
	}

    public HttpException(String message) {
        super(message);
        this.statusCode = -1;
        this.statusLine = null ;
        this.response = null;
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.statusLine = null ;
        this.response = null;
    }

    public HttpException(Throwable cause) {
        super(cause);
        this.statusCode = -1;
        this.statusLine = null ;
        this.response = null;
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
		return response;
	}
}
