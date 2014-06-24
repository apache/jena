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
import java.util.HashMap;
import java.util.Map;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * A scoped authenticator which is actually a wrapper around other
 * authenticators and thus can be used to mix and match different authentication
 * mechanisms to different services as desired
 * 
 */
public class DelegatingAuthenticator extends AbstractScopedAuthenticator<HttpAuthenticator> {

    private Map<URI, HttpAuthenticator> authenticators = new HashMap<>();

    /**
     * Creates a new delegating authenticator
     * 
     * @param target
     *            Target URI
     * @param authenticator
     *            Authenticator to delegate to
     */
    public DelegatingAuthenticator(URI target, HttpAuthenticator authenticator) {
        if (target == null)
            throw new IllegalArgumentException("Target URI cannot be null");
        this.authenticators.put(target, authenticator);
    }

    /**
     * Creates a new delegating authenticator
     * 
     * @param authenticators
     *            Mapping between URIs and authenticators
     */
    public DelegatingAuthenticator(Map<URI, HttpAuthenticator> authenticators) {
        this.authenticators.putAll(authenticators);
    }

    @Override
    public void apply(AbstractHttpClient client, HttpContext context, URI target) {
        HttpAuthenticator authenticator = this.findCredentials(target);
        if (authenticator != null) {
            authenticator.apply(client, context, target);
        }
    }

    @Override
    protected HttpAuthenticator getCredentials(URI target) {
        return this.authenticators.get(target);
    }

    @Override
    protected String getUserNameFromCredentials(HttpAuthenticator credentials) {
        // Not used by this implementation because we override apply() so this
        // will never be needed and regardless isn't available
        return null;
    }

    @Override
    protected char[] getPasswordFromCredentials(HttpAuthenticator credentials) {
        // Not used by this implementation because we override apply() so this
        // will never be needed and regardless isn't available
        return null;
    }

}
