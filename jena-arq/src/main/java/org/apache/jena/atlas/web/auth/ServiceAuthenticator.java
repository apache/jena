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
 * <p>
 * A HTTP authenticator which selects credentials based upon service context
 * found in the provided {@link Context}. May also optionally use fallback
 * credentials for URIs for which authentication has not been explicitly
 * configured. This works only with the Basic and Digest authentication schemes
 * so you may need to use an alternative authenticator if you need to use
 * another authentication scheme.
 * </p>
 */
public class ServiceAuthenticator extends AbstractScopedAuthenticator<Context> {

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

    @SuppressWarnings("unchecked")
    private Map<String, Context> getServiceContextMap() {
        return (Map<String, Context>) this.context.get(Service.serviceContext);

    }

    @Override
    protected Context getCredentials(URI target) {
        Map<String, Context> serviceContextMap = this.getServiceContextMap();
        if (serviceContextMap == null)
            return null;
        return serviceContextMap.get(target.toString());
    }

    @Override
    protected String getUserNameFromCredentials(Context credentials) {
        if (credentials != null) {
            // Use user name from service context
            return credentials.getAsString(Service.queryAuthUser);
        } else {
            // Use fallback user name
            return this.username;
        }
    }

    @Override
    protected char[] getPasswordFromCredentials(Context credentials) {
        if (credentials != null) {
            // Use password from service context
            String pwd = credentials.getAsString(Service.queryAuthPwd);
            return pwd != null ? pwd.toCharArray() : null;
        } else {
            // Use fallback password
            return this.password;
        }
    }
}
