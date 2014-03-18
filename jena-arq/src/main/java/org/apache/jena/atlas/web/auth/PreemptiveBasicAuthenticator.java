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

import org.apache.http.HttpHost;
import org.apache.http.auth.ChallengeState;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.HttpContext;

/**
 * A decorator for other authenticators that may be used to enable preemptive
 * basic authentication.
 * <p>
 * This can <strong>only</strong> be used with servers that support Basic HTTP
 * authentication.  For any other authentication scheme the use of this
 * authenticator will result in authentication failures.
 * </p>
 * <h3>Security Concerns</h3>
 * <p>
 * It is <strong>important</strong> to note that preemptive basic authentication
 * is less secure because it can expose credentials to servers that do not
 * require them.
 * </p>
 * <h3>Standard vs Proxy Authentication</h3>
 * <p>
 * Doing preemptive authentication requires knowing in advance whether you will
 * be doing standard or proxy authentication i.e. whether the remote server will
 * challenge with 401 or 407. If you need both you can take advantage of this
 * being a decorator and simply layer multiple instances of this.
 * </p>
 * <p>
 * However you must remember that this <strong>only</strong> works for Basic
 * HTTP authentication, any other authentication scheme cannot be done
 * preemptively because it requires a more complex and secure challenge response
 * process.
 * </p>
 */
public class PreemptiveBasicAuthenticator implements HttpAuthenticator {

    private HttpAuthenticator authenticator;
    private boolean isProxy = false;

    /**
     * Creates a new decorator over the given authenticator
     * 
     * @param authenticator
     *            Authenticator to decorate
     */
    public PreemptiveBasicAuthenticator(HttpAuthenticator authenticator) {
        this(authenticator, false);
    }

    /**
     * Creates a new decorator over the given authenticator
     * 
     * @param authenticator
     *            Authenticator to decorate
     * @param forProxy
     *            Whether preemptive authentication is for a proxy
     */
    public PreemptiveBasicAuthenticator(HttpAuthenticator authenticator, boolean forProxy) {
        if (authenticator == null)
            throw new IllegalArgumentException("Must provide an authenticator to decorate");
        this.authenticator = authenticator;
    }

    @Override
    public void apply(AbstractHttpClient client, HttpContext httpContext, URI target) {
        this.authenticator.apply(client, httpContext, target);

        // Enable preemptive basic authentication
        // For nice layering we need to respect existing auth cache if present
        AuthCache authCache = (AuthCache) httpContext.getAttribute(ClientContext.AUTH_CACHE);
        if (authCache == null)
            authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme(this.isProxy ? ChallengeState.PROXY : ChallengeState.TARGET);
        // TODO It is possible that this overwrites existing cached credentials
        // so potentially not ideal.
        authCache.put(new HttpHost(target.getHost(), target.getPort()), basicAuth);
        httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
    }

    @Override
    public void invalidate() {
        // Does nothing
    }

}
