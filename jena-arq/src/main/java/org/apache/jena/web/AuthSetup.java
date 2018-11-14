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

package org.apache.jena.web;

import org.apache.http.auth.AuthScope;

/** Struct for the authentication information.
 * See {@link AuthScope} for "ANY" constants.
 */
public class AuthSetup {
    public final String host;
    public final int port;
    public final String user;
    public final String password;
    public final String realm;
    
    public AuthSetup(String host, Integer port, String user, String password, String realm) {
        this.host = any(host, AuthScope.ANY_HOST);
        this.port = (port == null || port <= 0 ) ? AuthScope.ANY_PORT : port; 
        this.user = user;
        this.password = password;
        this.realm = any(host, AuthScope.ANY_REALM);
    }
    
    public AuthScope authScope() {
        return new AuthScope(host, port, realm, AuthScope.ANY_SCHEME);
    }
    
    private <X> X any(X value, X anyVal) {
        if ( value == null )
            return anyVal;
        return value;
    }
}
