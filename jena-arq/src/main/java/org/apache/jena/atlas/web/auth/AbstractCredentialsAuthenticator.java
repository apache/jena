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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;

/**
 * Abstract authenticator that uses user name and password credentials
 * 
 */
public abstract class AbstractCredentialsAuthenticator implements HttpAuthenticator {

    @Override
    public void apply(AbstractHttpClient client, HttpContext context, URI target) {
        // At least a user name is required or no authentication will be done
        if (!this.hasUserName(target))
            return;

        // Be careful to scope credentials to the specific URI so that
        // HttpClient won't try and send them to other servers
        HttpHost host = new HttpHost(target.getHost(), target.getPort());
        CredentialsProvider provider = new BasicCredentialsProvider();

        provider.setCredentials(new AuthScope(host), this.createCredentials(target));

        client.setCredentialsProvider(provider);
    }

    @Override
    public void invalidate() {
        // Does nothing by default
    }

    /**
     * Creates the credentials used to authenticate
     * <p>
     * This default implementation simply returns a
     * {@link UsernamePasswordCredentials} instance, derived implementations may
     * need to override this
     * </p>
     * 
     * @param target
     *            Target URI
     * @return Credentials
     */
    protected Credentials createCredentials(URI target) {
        String user = this.getUserName(target);
        char[] password = this.getPassword(target);
        return password != null ? new UsernamePasswordCredentials(user, new String(password)) : new UsernamePasswordCredentials(
                user, "");
    }

    /**
     * Gets whether there is a user name available for the target URI
     * 
     * @param target
     *            Target
     * @return True if a user name is available, false otherwise
     */
    protected abstract boolean hasUserName(URI target);

    /**
     * Gets the user name for the target URI
     * 
     * @param target
     *            Target
     * @return User name or null if none available
     */
    protected abstract String getUserName(URI target);

    /**
     * Gets whether there is a password available for the target URI
     * 
     * @param target
     *            Target
     * @return True if a password is available, false otherwise
     */
    protected abstract boolean hasPassword(URI target);

    /**
     * Gets the password for the target URI
     * 
     * @param target
     *            Target
     * @return Password or null if none available
     */
    protected abstract char[] getPassword(URI target);

}