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

package org.apache.jena.http;

import static org.apache.jena.atlas.lib.Lib.uppercase;

public enum HttpMethod {
    DELETE("DELETE"),
    HEAD("HEAD"),
    GET("GET"),
    QUERY("QUERY"),
    OPTIONS("OPTIONS"),
    PATCH("PATCH"),
    POST("POST"),
    PUT("PUT"),
    TRACE("TRACE")
    ;

    private final String method;

    public static final String METHOD_DELETE        = "DELETE";
    public static final String METHOD_HEAD          = "HEAD";
    public static final String METHOD_GET           = "GET";
    public static final String METHOD_QUERY         = "QUERY" ;
    public static final String METHOD_OPTIONS       = "OPTIONS";
    public static final String METHOD_PATCH         = "PATCH" ;
    public static final String METHOD_POST          = "POST";
    public static final String METHOD_PUT           = "PUT";
    public static final String METHOD_TRACE         = "TRACE";

    private HttpMethod(String name) {
        this.method = name;
    }

    public String method() { return method; }

    public static HttpMethod fromString(String name) {
        try {
            return HttpMethod.valueOf(uppercase(name));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
