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

package org.apache.jena.riot.web;

import java.util.Locale;

public enum HttpMethod {
    // METHOD_ only for transition/rename.
    METHOD_DELETE("DELETE"),
    METHOD_HEAD("HEAD"),
    METHOD_GET("GET"),
    METHOD_QUERY("QUERY"),
    METHOD_OPTIONS("OPTIONS"),
    METHOD_PATCH("PATCH"),
    METHOD_POST("POST"),
    METHOD_PUT("PUT"),
    METHOD_TRACE("TRACE")
    ;

    private final String method;

    private HttpMethod(String name) {
        this.method = name;
    }

    public String method() { return method; }

    public static HttpMethod fromString(String name) {
        try {
            return HttpMethod.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}