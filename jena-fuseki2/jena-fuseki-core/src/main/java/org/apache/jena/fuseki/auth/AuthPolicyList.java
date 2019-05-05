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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An {@link AuthPolicy} that combines a number of {@link AuthPolicy AuthPolicies}.
 * All policies must authorize access for this policy to allow access.
 */
public class AuthPolicyList implements AuthPolicy {

    // Thread safe.
    // Use a
    private final Queue<AuthPolicy> policies = new ConcurrentLinkedQueue<>();

    /**
     * Merge {@link AuthPolicy AuthPolicies}, returning a combination of the two if both are non-null.
     * If either is null, return the other.
     * If both null, return null.
     */
    public static AuthPolicy merge(AuthPolicy policy1, AuthPolicy policy2) {
        if ( policy1 == null )
            return policy2;
        if ( policy2 == null )
            return policy1;
        if ( policy1 instanceof AuthPolicyList) {
            AuthPolicyList x = new AuthPolicyList((AuthPolicyList)policy1);
            x.add(policy2);
            return x;
        }
        AuthPolicyList x = new AuthPolicyList();
        x.add(policy1);
        x.add(policy2);
        return x;
    }

    private AuthPolicyList(AuthPolicyList other) {
        policies.addAll(other.policies);
    }

    public AuthPolicyList() { }

    public void add(AuthPolicy policy) {
        policies.add(policy);
    }

    @Override
    public boolean isAllowed(String user) {
        for ( AuthPolicy policy : policies ) {
            if ( ! policy.isAllowed(user) )
                return false;
        }
        return true;
    }
}
