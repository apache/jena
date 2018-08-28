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

package org.apache.jena.fuseki.access;

import java.util.StringJoiner;

import org.apache.jena.atlas.lib.Registry;

/**
 * Am {@link AuthorizationService} implements as a mapping from a string (typically a user name or role
 * name) to a {@link SecurityContext}, where the {@link SecurityContext}
 * is the access control operations for the user/role.
 */ 
public class SecurityRegistry extends Registry<String, SecurityContext> implements AuthorizationService {
    
    public SecurityRegistry() {}
    
    @Override
    public SecurityContext get(String actor) {
        if ( actor == null )
            return SecurityContext.NONE;
        SecurityContext sCxt = super.get(actor);
        if ( sCxt == null )
            sCxt = SecurityContext.NONE;
        return sCxt;
    }
    
    @Override 
    public String toString() {
        return "SecurityRegistry"+keys();
    }        
 
    public String toLongString() {
        // Long form.
        StringJoiner sj1 = new StringJoiner("\n", "{ SecurityRegistry\n", "\n}");
        super.keys().forEach(u->{
            SecurityContext x = super.get(u);
            StringJoiner sj2 = new StringJoiner("");
            sj2.add("  ")
                .add(u)
                .add(" -> ")
                .add(x.toString());
            sj1.add(sj2.toString());
        });
        return sj1.toString();
    }
}
