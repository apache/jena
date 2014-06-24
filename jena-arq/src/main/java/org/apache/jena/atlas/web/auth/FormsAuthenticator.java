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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.riot.web.HttpOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * An authenticator capable of making Form based logins and using cookies to
 * maintain authentication state. Different logins may be used for different
 * services as required.
 * </p>
 * <h3>Login Scope</h3>
 * <p>
 * Note that logins are not exclusively scoped to the exact URI rather they are
 * scoped to any URI derived from the given URI. For example if you declared
 * logins for {@code http://example.org} they would also apply to
 * {@code http://example.org/some/path/}. When determining credentials the
 * longest match applies, so in the previous example you could define different
 * logins for the two URIs and URIs derived from
 * {@code http://example.org/some/path/} would prefer the login for that URI
 * over those for {@code http://example.org}
 * </p>
 * 
 */
public class FormsAuthenticator extends AbstractScopedAuthenticator<FormLogin> {

    private static final Logger LOG = LoggerFactory.getLogger(FormsAuthenticator.class);

    private Map<URI, FormLogin> logins = new HashMap<>();

    /**
     * Creates a new authenticator with the given login
     * 
     * @param target
     *            Target URI
     * @param login
     *            Login
     */
    public FormsAuthenticator(URI target, FormLogin login) {
        if (target == null)
            throw new IllegalArgumentException("Target URI cannot be null");
        this.logins.put(target, login);
    }

    /**
     * Creates a new authenticator with the given logins
     * 
     * @param logins
     *            Logins
     */
    public FormsAuthenticator(Map<URI, FormLogin> logins) {
        this.logins.putAll(logins);
    }

    @Override
    public void apply(AbstractHttpClient client, HttpContext httpContext, URI target) {
        if (client == null)
            return;

        // Do we have a login available for the target server?
        FormLogin login = this.findCredentials(target);
        if (login == null)
            return;

        // We need to synchronize on the login because making a login attempt
        // may take a while and there is no point making multiple login attempts
        // against the same server
        synchronized (login) {

            // Have we already logged into this server?
            if (login.hasCookies()) {
                // Use existing cookies
                LOG.info("Using existing cookies to authenticate access to " + target.toString());
                CookieStore store = login.getCookies();
                if (store != null)
                    client.setCookieStore(store);

                // Check if any of the cookies have expired
                if (!store.clearExpired(Calendar.getInstance().getTime())) {
                    // No cookies were cleared so our cookies are still fresh
                    // and we don't need to login again
                    return;
                }

                // If we reach here then some of our cookies have expired and we
                // may no longer be logged in and should login again instead of
                // proceeding with the existing cookies
            }

            try {
                // Use a fresh Cookie Store for new login attempts
                CookieStore store = new BasicCookieStore();
                client.setCookieStore(store);

                // Try to login
                LOG.info("Making login attempt against " + login.getLoginFormURL() + " to obtain authentication for access to "
                        + target.toString());
                HttpPost post = new HttpPost(login.getLoginFormURL());
                post.setEntity(login.getLoginEntity());
                HttpResponse response = client.execute(post, httpContext);

				// Always read the payload to ensure reusable connections
				final String payload = HttpOp.readPayload(response.getEntity());

				// Check for successful login
                if (response.getStatusLine().getStatusCode() >= 400) {
                    LOG.warn("Failed to login against " + login.getLoginFormURL() + " to obtain authentication for access to "
                            + target.toString());
                    throw new HttpException(response.getStatusLine().getStatusCode(), "Login attempt failed - "
                            + response.getStatusLine().getReasonPhrase(), payload);
                }

                // Otherwise assume a successful login
                LOG.info("Successfully logged in against " + login.getLoginFormURL()
                        + " and obtained authentication for access to " + target.toString());
                login.setCookies(client.getCookieStore());

            } catch (UnsupportedEncodingException e) {
                throw new HttpException("UTF-8 encoding not supported on your platform", e);
            } catch (IOException e) {
                throw new HttpException("Error making login request", e);
            }
        }
    }
    
    @Override
    public void invalidate() {
        // Discard all cookies we have currently
        for (FormLogin login : this.logins.values()) {
            login.clearCookies();
        }
    }

    /**
     * Adds a login to the authenticator preserving any existing cookies
     * associated with the login
     * 
     * @param target
     *            Target URI
     * @param login
     *            Login
     */
    public void addLogin(URI target, FormLogin login) {
        if (target == null)
            throw new IllegalArgumentException("Target URI cannot be null");
        this.logins.put(target, login);

    }

    @Override
    protected FormLogin getCredentials(URI target) {
        return this.logins.get(target);
    }

    @Override
    protected String getUserNameFromCredentials(FormLogin credentials) {
        // Not used by this implementation because we override apply() so this
        // will never be needed and regardless isn't available
        return null;
    }

    @Override
    protected char[] getPasswordFromCredentials(FormLogin credentials) {
        // Not used by this implementation because we override apply() so this
        // will never be needed and regardless isn't available
        return null;
    }

}
