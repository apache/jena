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

import javax.servlet.ServletContext;

import org.apache.jena.atlas.lib.Registry;
import org.apache.jena.fuseki.Fuseki;

/**
 * A {@link SecurityRegistry} is mapping from a string (typically a user name or role
 * name) to a {@link SecurityPolicy}, where the {@link SecurityPolicy}
 * is the access control operations for the user/role.
 */ 
public class SecurityRegistry extends Registry<String, SecurityPolicy>{
    
    public static SecurityRegistry get(ServletContext cxt) {
        return (SecurityRegistry)cxt.getAttribute(Fuseki.attrSecurityRegistry);
    }
    
    public static void set(ServletContext cxt, SecurityRegistry securityRegistry) {
        cxt.setAttribute(Fuseki.attrSecurityRegistry, securityRegistry);
    }

    public SecurityRegistry() {}
    
    @Override
    public SecurityPolicy get(String actor) {
        if ( actor == null )
            return SecurityPolicy.NONE;
        SecurityPolicy policy = super.get(actor);
        if ( policy == null )
            policy = SecurityPolicy.NONE;
        return policy;
    }
    
    @Override 
    public String toString() {
        if ( true ) 
            return "SecurityRegistry"+keys();
        // Long form.
        StringJoiner sj1 = new StringJoiner("\n", "{ SecurityRegistry\n", "\n}");
        super.keys().forEach(u->{
            SecurityPolicy x = super.get(u);
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
