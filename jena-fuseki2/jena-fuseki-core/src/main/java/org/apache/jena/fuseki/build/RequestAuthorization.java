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

package org.apache.jena.fuseki.build;

import java.util.*;

import org.apache.jena.fuseki.Fuseki;

/**
 * Policy for allowing users to execute a request. Assumes the user has been
 * authenticated.
 */
public class RequestAuthorization {

    private static String ANY_USER  = "*";
    private final Set<String>        allowedUsers;
    private final boolean            allowAllUsers;
    private final boolean            allowAnon;

    /** Allow specific users */ 
    public static RequestAuthorization policyAllowSpecific(String... allowedUsers) {
        return new RequestAuthorization(Arrays.asList(allowedUsers), false, false);
    }

    /** Allow specific users */ 
    public static RequestAuthorization policyAllowSpecific(Collection<String> allowedUsers) {
        return new RequestAuthorization(allowedUsers, false, false);
    }

    /** Allow authenticated (logged in) user. */ 
    public static RequestAuthorization policyAllowAuthenticated() {
        return new RequestAuthorization(null, true, false);
    }

    /** Allow without authentication */ 
    public static RequestAuthorization policyAllowAnon() {
        return new RequestAuthorization(null, true, true);
    }

    /** Allow without authentication */ 
    public static RequestAuthorization policyNoAccess() {
        return new RequestAuthorization(Collections.emptySet(), false, false);
    }

    public RequestAuthorization(Collection<String> allowed, final boolean allowAllUsers, final boolean allowAnon) {
        // -- anon.
        if ( allowAnon ) {
            if ( !isNullOrEmpty(allowed) ) {
                //warn
            }
            this.allowAnon = true;
            this.allowAllUsers = true;
            this.allowedUsers = Collections.emptySet();
            return;
        }
        this.allowAnon = false;
        
        // -- "any user"
        if ( allowAllUsers || contains(allowed, ANY_USER) ) {
            if ( allowed != null && allowed.size() > 1 )
                Fuseki.configLog.warn("Both 'any user' and a list of users given");
            this.allowAllUsers = true;
            this.allowedUsers = Collections.emptySet();
            return ;
        }

        // -- List of users
        this.allowedUsers = (allowed == null) ? Collections.emptySet() : new HashSet<>(allowed);
        this.allowAllUsers = false;
    }
    
    public boolean isAllowed(String user) {
        if ( allowAnon )
            return true;
        if ( user == null )
            return false;
        if ( allowAllUsers )
            return true;
        if ( contains(allowedUsers, user) )
            return true;
        return false;
    }

    public boolean isDenied(String user) {
        return !isAllowed(user);
    }

    static <T> boolean isNullOrEmpty(Collection<T> collection) {
        if ( collection == null )
            return true;
        return collection.isEmpty(); 
    }
    
    static <T> boolean contains(Collection<T> collection, T obj) {
        if ( collection == null )
            return false;
        return collection.contains(obj); 
    }
}
