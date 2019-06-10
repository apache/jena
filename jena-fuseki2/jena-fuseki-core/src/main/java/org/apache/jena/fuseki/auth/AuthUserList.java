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

import java.util.*;

/**
 * Policy for allowing users to execute a request.
 * Assumes the user has been authenticated.
 */
class AuthUserList implements AuthPolicy {

    private final Set<String>  allowedUsers;

    /*package*/ AuthUserList(Collection<String> allowed) {
        this.allowedUsers = (allowed == null) ? Collections.emptySet() : new HashSet<>(allowed);
    }

    @Override
    public boolean isAllowed(String user) {
        if ( user == null )
            return false;
        if ( contains(allowedUsers, user) )
            return true;
        return false;
    }

    @Override
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
