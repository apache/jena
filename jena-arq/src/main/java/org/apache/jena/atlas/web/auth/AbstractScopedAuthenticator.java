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
import java.net.URISyntaxException;

/**
 * <p>
 * An abstract helper for authenticators which scope credentials to URIs
 * </p>
 * <h3>Credentials Scope</h3>
 * <p>
 * Note that credentials are not necessarily considered to be exclusively scoped
 * to the exact URI rather they are scoped to any URI derived from the given
 * URI. For example if you declared credentials for {@code http://example.org}
 * they would also apply to {@code http://example.org/some/path/}. When
 * determining credentials the longest match applies, so in the previous example
 * you could define different credentials for the two URIs and URIs derived from
 * {@code http://example.org/some/path/} would prefer the credentials for that
 * URI over those for {@code http://example.org}
 * </p>
 * <p>
 * Implementations that wish to follow the above scoping policy should make use
 * of the findCredentials method
 * </p>
 * 
 * @param <T>
 *            Type used to store credential information
 */
public abstract class AbstractScopedAuthenticator<T> extends AbstractCredentialsAuthenticator {

    @Override
    protected final String getUserName(URI target) {
        return getUserNameFromCredentials(this.findCredentials(target));
    }

    @Override
    protected final char[] getPassword(URI target) {
        return getPasswordFromCredentials(this.findCredentials(target));
    }

    @Override
    protected final boolean hasUserName(URI target) {
        return this.getUserName(target) != null;
    }

    @Override
    protected final boolean hasPassword(URI target) {
        return this.getPassword(target) != null;
    }

    /**
     * Gets the credentials associated with the exact URI given
     * <p>
     * Called from {@link #findCredentials(URI)} as part of the credential
     * lookup process
     * </p>
     * 
     * @param target
     *            Target URI
     * @return Credentials
     */
    protected abstract T getCredentials(URI target);

    /**
     * Extract the user name from the given credentials
     * 
     * @param credentials
     *            Credentials
     * @return User Name
     */
    protected abstract String getUserNameFromCredentials(T credentials);

    /**
     * Extract the password from the given credentials
     * 
     * @param credentials
     *            Credentials
     * @return Password
     */
    protected abstract char[] getPasswordFromCredentials(T credentials);

    /**
     * Finds credentials for the given URI using a longest match approach
     * 
     * @param target
     * @return T
     */
    protected final T findCredentials(URI target) {
        // Try URI as-is to start with
        T creds = this.getCredentials(target);

        try {
            // If that fails strip down the URI recursively
            while (creds == null) {
                if (target.getFragment() != null) {
                    // If it has a fragment strip that off
                    target = new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(),
                            target.getPath(), target.getQuery(), null);
                } else if (target.getQuery() != null) {
                    // If it has a query string strip that off
                    target = new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(),
                            target.getPath(), null, null);
                } else if (target.getPath() != null) {
                    // Try and strip off last segment of the path
                    String currPath = target.getPath();
                    if (currPath.endsWith("/")) {
                        currPath = currPath.substring(0, currPath.length() - 1);
                        if (currPath.length() == 0)
                            currPath = null;
                        target = new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(), currPath,
                                null, null);
                    } else if (currPath.contains("/")) {
                        currPath = currPath.substring(0, currPath.lastIndexOf('/') + 1);
                        if (currPath.length() == 0)
                            currPath = null;
                        target = new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(), currPath,
                                null, null);
                    } else {
                        // If path is non-null it must always contain a /
                        // otherwise it would be an invalid path
                        // In this case bail out
                        return null;
                    }
                }

                creds = this.getCredentials(target);
            }
            return creds;
        } catch (URISyntaxException e) {
            // If we generate a malformed URL then bail out
            return null;
        }
    }
}