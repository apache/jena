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

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

/**
 * Interface for classes that are able to apply some form of authentication to a
 * {@link HttpClient} instance. Provides a unified mechanism for applying
 * authentication that is agnostic of the actual authentication mechanism being
 * used.
 * 
 */
public interface HttpAuthenticator {

    /**
     * Applies any necessary authentication methods to the given HTTP Client
     * <p>
     * The {@code target} parameter indicates the URI to which the request is
     * being made and so may be used by an authenticator to determine whether it
     * actually needs to apply any authentication or to scope authentication
     * appropriately.
     * </p>
     * 
     * @param client
     *            HTTP Client
     * @param httpContext
     *            HTTP Context
     * @param target
     *            Target URI to which code wants to authenticate
     */
    public void apply(AbstractHttpClient client, HttpContext httpContext, URI target);

    /**
     * Invalidates the authenticator
     * <p>
     * Allows code to inform the authenticator that any cached authentication
     * information should be invalidated. This can be useful after an
     * authentication attempt fails or after a certain amount of time is passed.
     * For many authenticators this may actually be a no-op since when using
     * standard HTTP authentication typically you authenticate on every request
     * and there are no cached authentication information. However more complex
     * authentication mechanisms such as Form Based authentication may have
     * cached information that discarding will force subsequent requests to
     * re-authenticate.
     * </p>
     */
    public void invalidate();
}
