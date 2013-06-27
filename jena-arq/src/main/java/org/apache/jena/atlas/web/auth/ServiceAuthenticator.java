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

package org.apache.jena.atlas.web.auth;

import java.net.URI;
import java.util.Map;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sparql.engine.http.Service;
import com.hp.hpl.jena.sparql.util.Context;

/**
 * A HTTP authenticator which selects credentials based upon service context
 * found in the provided {@link Context}. May also optionally use fallback
 * credentials for URIs for which authentication has not been explicitly
 * configured.
 * 
 */
public class ServiceAuthenticator extends AbstractScopedAuthenticator {

    private Context context;
    private String username;
    private char[] password;

    /**
     * Creates new authenticator using the standard ARQ context
     */
    public ServiceAuthenticator() {
        this(ARQ.getContext(), null, null);
    }

    /**
     * Creates a new authenticator using the standard ARQ context, the provided
     * credentials are used as fallback if the context contains no registered
     * credentials for a target URI
     * 
     * @param username
     *            Fallback user name
     * @param password
     *            Fallback password
     */
    public ServiceAuthenticator(String username, char[] password) {
        this(ARQ.getContext(), username, password);
    }

    /**
     * Creates a new authenticator using the given context
     * 
     * @param context
     *            Context
     */
    public ServiceAuthenticator(Context context) {
        this(context, null, null);
    }

    /**
     * Creates a new authenticator using the given context, the provided
     * credentials are used as fallback if the context contains no registered
     * credentials for a target URI
     * 
     * @param context
     *            Context
     * @param username
     *            Fallback user name
     * @param password
     *            Fallback password
     */
    public ServiceAuthenticator(Context context, String username, char[] password) {
        this.context = context;
        this.username = username;
        this.password = password;
    }

    @Override
    public String getUserName(URI target) {
        if (this.context != null) {

            @SuppressWarnings("unchecked")
            Map<String, Context> serviceContextMap = (Map<String, Context>) this.context.get(Service.serviceContext);
            if (serviceContextMap != null && serviceContextMap.containsKey(target.toString())) {
                // Try to obtain Context for target URI
                Context serviceContext = serviceContextMap.get(target.toString());
                if (serviceContext != null) {
                    // Service Context exists for target URI
                    return serviceContext.getAsString(Service.queryAuthUser);
                }
            }
        }
        // Use fallback user name
        return this.username;
    }

    @Override
    public char[] getPassword(URI target) {
        if (this.context != null) {

            @SuppressWarnings("unchecked")
            Map<String, Context> serviceContextMap = (Map<String, Context>) this.context.get(Service.serviceContext);
            if (serviceContextMap != null && serviceContextMap.containsKey(target.toString())) {
                // Try to obtain Context for target URI
                Context serviceContext = serviceContextMap.get(target.toString());
                if (serviceContext != null) {
                    // Service Context exists for target URI
                    String pwd = serviceContext.getAsString(Service.queryAuthPwd);
                    return pwd != null ? pwd.toCharArray() : null;
                }
            }
        }
        // User fallback password
        return this.password;
    }
}
