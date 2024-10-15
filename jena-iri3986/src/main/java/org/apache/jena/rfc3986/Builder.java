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

package org.apache.jena.rfc3986;

/**
 * IRI builder.
 * <p>
 * See {@link RFC3986#newBuilder()}
 */
public class Builder{
    private String scheme = null;
    private String authority = null;
    private String host = null;
    private int port = -1;
    private String path = "";
    private String query = null;
    private String fragment = null;
    /*package*/ Builder() {}

    static class IRIBuildException extends RuntimeException {
        public IRIBuildException(String message) {super(message); }
    }

    public String scheme() {
        return scheme;
    }

    public Builder scheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String authority() {
        return authority;
    }

    /** Set the "auhtority" part of an IRI - this clear previous host and port settings" */
    public Builder authority(String authority) {
        this.authority = authority;
        this.host = null;
        this.port = -1;
        return this;
    }

    public String host() {
        return host;
    }

    public Builder host(String host) {
        this.authority = null;
        this.host = host;
        return this;
    }

    public int port() {
        return port;
    }

    public Builder port(int port) {
        if ( port < 0 )
            throw new IRIBuildException("port is less than zero");
        this.authority = null;
        this.port = port;
        return this;
    }

    public String path() {
        return path;
    }

    public Builder path(String path) {
        this.path = path;
        return this;
    }

    public String query() {
        return query;
    }

    public Builder query(String query) {
        this.query = query;
        return this;
    }

    public String fragment() {
        return fragment;
    }

    public Builder fragment(String fragment) {
        this.fragment = fragment;
        return this;
    }

    public IRI3986 build() {
        String a = authority;
        if ( host != null )
            a = host;
        if ( port >= 0 ) {
            if ( host == null )
                a = ":" + port;
            else
                a = host + ":" + port;
        }
        return IRI3986.build(scheme, a, path, query, fragment);
    }
}