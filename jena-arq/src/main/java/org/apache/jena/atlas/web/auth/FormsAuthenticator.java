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
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.web.HttpException;

/**
 * An authenticator capable of making Form based logins and using cookies to
 * maintain authentication state
 * 
 */
public class FormsAuthenticator implements HttpAuthenticator {

    private Map<URI, CookieStore> cookies = new HashMap<URI, CookieStore>();
    private Map<URI, FormLogin> logins = new HashMap<URI, FormLogin>();
    
    /**
     * Creates a new authenticator with the given login
     * @param target Target URI
     * @param login Login
     */
    public FormsAuthenticator(URI target, FormLogin login) {
        if (target == null) throw new IllegalArgumentException("Target URI cannot be null");
        this.logins.put(target, login);
    }
    
    /**
     * Creates a new authenticator with the given logins
     * @param logins Logins
     */
    public FormsAuthenticator(Map<URI, FormLogin> logins) {
        this.logins.putAll(logins);
    }

    @Override
    public synchronized void apply(AbstractHttpClient client, HttpContext httpContext, URI target) {
        if (client == null)
            return;

        synchronized (this.cookies) {
            if (this.cookies.containsKey(target)) {
                // Use existing cookies
                CookieStore store = this.cookies.get(target);
                if (store != null)
                    client.setCookieStore(store);
                return;
            }
        }
        
        // Do we have a login available for the target server?
        FormLogin login = this.logins.get(target);
        if (login == null)
            return;
        
        // Use a fresh Cookie Store for new login attempts
        CookieStore store = new BasicCookieStore();
        client.setCookieStore(store);

        try {
            // Try to login
            HttpPost post = new HttpPost(login.getLoginFormURL());
            post.setEntity(login.getLoginEntity());
            HttpResponse response = client.execute(post, httpContext);

            // Check for successful login
            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new HttpException(response.getStatusLine().getStatusCode(), "Login attempt failed - "
                        + response.getStatusLine().getReasonPhrase());
            }

            // Otherwise assume a successful login
            synchronized (this.cookies) {
                this.cookies.put(target, client.getCookieStore());
            }

        } catch (UnsupportedEncodingException e) {
            throw new HttpException("UTF-8 encoding not supported on your platform", e);
        } catch (IOException e) {
            throw new HttpException("Error making login request", e);
        }
    }
    
    /**
     * Adds a login to the authenticator, if the authenticator had previously logged into the given URI cookies for that URI are discarded
     * @param target Target URI
     * @param login Login
     */
    public void addLogin(URI target, FormLogin login) {
        if (target == null) throw new IllegalArgumentException("Target URI cannot be null");
        this.logins.put(target, login);
        synchronized (this.cookies) {
            this.cookies.remove(target);
        }
    }

}
