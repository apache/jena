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

package org.apache.jena.fuseki.auth;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiConfigException;

/** Authorization Policies.
 * See {@link Users} for special user names.
 */
public class Auth {
    public static final String dftRealm = "TripleStore";

    /** Any authenticated user. */
    public static AuthPolicy ANY_USER   = (user) -> user != null;

    /** Any user, whether authenticated or not. */
    public static AuthPolicy ANY_ANON   = (user) -> true;

    /** Never allow. */
    public static AuthPolicy DENY_ALL   = (user) -> false;

    /** A policy that allows specific users (convenience wrapped for {@link #policyAllowSpecific(Collection)}). */
    public static AuthPolicy policyAllowSpecific(String... allowedUsers) {
        return Auth.policyAllowSpecific(Arrays.asList(allowedUsers));
    }

    /**
     * A policy that allows specific users.
     * <ul>
     * <li>If any user is {@linkplain Users#UserDenyAll}, then this policy is the same as {@linkplain #DENY_ALL}.
     * <li>If any user is {@linkplain Users#UserAnyLoggedIn}, then this policy is the same as {@linkplain #ANY_USER}.
     * <li>If any user is {@linkplain Users#UserAnyAnon}, then this policy is the same as {@linkplain #ANY_ANON}.
     * </ul>
     */
    public static AuthPolicy policyAllowSpecific(Collection<String> allowedUsers) {
        Objects.requireNonNull(allowedUsers, "allowedUsers");

        if ( allowedUsers.isEmpty() )
            return Auth.DENY_ALL;
        if ( allowedUsers.contains(Users.UserDenyAll) ) {
            // The "Deny" user can only be used on it's own.
            if ( allowedUsers.size() > 1 )
                Fuseki.configLog.warn("Both '!' (deny all) and a list of users given");
            return Auth.DENY_ALL;
        }

        if ( allowedUsers.contains(Users.UserAnyLoggedIn) ) {
            if ( allowedUsers.size() > 1 )
                Fuseki.configLog.warn("Both 'any user' and a list of users given");
            return Auth.ANY_USER;
        }
        if ( allowedUsers.contains(Users.UserAnyAnon) ) {
            if ( allowedUsers.size() > 1 )
                Fuseki.configLog.warn("Both 'anon user' and a list of users given");
            return Auth.ANY_ANON;
        }

        if ( allowedUsers.stream().anyMatch(Objects::isNull) )
            throw new FusekiConfigException("null user found : "+allowedUsers);
        return new AuthUserList(allowedUsers);
    }

    /**
     * Test whether a user (principal) is allowed by a authorization policy.
     * The policy can be null, meaning no restrictions, and the function returns true.
     * {@code user} maybe null, meaning unauthenticated and any policy must deal with this.
     * @param user
     * @param policy
     * @return boolean True if the policy is null or allows the user.
     */
    public static boolean allow(String user, AuthPolicy policy) {
        if ( policy == null )
            return true;
        return policy.isAllowed(user);
    }

    /**
     * Test whether a user (principal) is allowed by a authorization policy
     * and perform an action if the policy does not allow the user.
     * The action can throw an exception.
     * Additional, return true/false - see {@link #allow(String, AuthPolicy)}.
     * The policy can be null, meaning no restrictions, and the function returns true.
     * {@code user} maybe null, meaning unauthenticated and any policy must deal with this.
     * @param user
     * @param policy
     * @param notAllowed Runnable to execute if the policy does not allow the user.
     */
    public static boolean allow(String user, AuthPolicy policy, Runnable notAllowed) {
        if ( allow(user, policy) )
            return true;
        notAllowed.run();
        return false;
    }

    /**
     * Calculate the value of the "Authentication" HTTP header for basic auth. Basic
     * auth is not secure when used over HTTP (the password can be extracted). Use
     * with HTTPS is better.
     * <p>
     * Unlike digest auth, basic auth can be setup without an extra round trip to the
     * server, making it easier for scripts where the body is not replayable.
     *
     * @param username
     * @param password
     * @return String
     */
    private static String basicAuth(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
