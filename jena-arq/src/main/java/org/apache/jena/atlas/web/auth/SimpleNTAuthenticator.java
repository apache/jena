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

import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;

/**
 * <p>
 * A HTTP Authenticator which provides authentication via user name and password
 * combinations. Works with the NTLM authentication scheme.
 * </p>
 * <p>
 * Use the parent class {@link SimpleAuthenticator} if you just need to do
 * Basic/Digest authentication.
 * </p>
 * <p>
 * This authenticator will present the given credentials to any server that
 * request authentication even though the credentials may not be valid there. It
 * is typically more secure to use the {@link ScopedNTAuthenticator} instead
 * which only presents credentials to specific servers.
 * </p>
 * 
 */
public class SimpleNTAuthenticator extends SimpleAuthenticator {

    private String workstation, domain;

    /**
     * Creates a new authenticator
     * 
     * @param username
     *            Username
     * @param password
     *            Password
     * @param workstation
     *            Workstation, this is the ID of your local workstation
     * @param domain
     *            Domain, this is the domain you are authenticating in which may
     *            not necessarily be the domain your workstation is in
     */
    public SimpleNTAuthenticator(String username, char[] password, String workstation, String domain) {
        super(username, password);
        this.workstation = workstation;
        this.domain = domain;
    }

    @Override
    protected Credentials createCredentials(URI target) {
        return new NTCredentials(this.getUserName(target), new String(this.getPassword(target)), this.workstation, this.domain);
    }

}
