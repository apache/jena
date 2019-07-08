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

/**
 * Class of HTTP Exceptions from Atlas code
 * 
 */
public class HttpException extends RuntimeException {
    private int statusCode = -1;
    private String statusLine = null ;
	private String response;

	public HttpException(int statusCode, String statusLine, String response) {
		super(statusCode + " - " + statusLine);
		this.statusCode = statusCode;
		this.statusLine = statusLine ;
		this.response = response;
	}

    
    public HttpException(String message) {
        super(message);
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public HttpException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Gets the status code, may be -1 if unknown
     * @return Status Code if known, -1 otherwise
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the response code, may be -1 if unknown
     * @return Response Code if known, -1 otherwise
     * @deprecated Use {@link #getStatusCode()}
     */
    @Deprecated
    public int getResponseCode() {
        return getStatusCode();
    }
    
    /**
     * Gets the response code, may be null if unknown
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
