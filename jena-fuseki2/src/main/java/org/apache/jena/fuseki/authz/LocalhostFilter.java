/**
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

package org.apache.jena.fuseki.authz;

import javax.servlet.ServletRequest ;
import javax.servlet.ServletResponse ;

import org.apache.shiro.web.filter.authz.PortFilter ;

/**
 * A Filter that can allow or deny access based on whether the
 * the host that sent the request is the loopback address (AKA localhost).
 * Use of the external IP address of the local machine does not permit access,
 * only the loopback interface is authorized.
 * Responds with HTTP 403 on any denied request.
 * 
 * Example:
 * <pre>
 * [main]
 * localhost=org.apache.jena.fuseki.authz.LocalhostFilter
 * ...
 * [urls]
 * /LocalFilesForLocalPeople/** = localhost
 * </pre>
 * @see PortFilter
 */

public class LocalhostFilter extends AuthorizationFilter403 {
    
    private static final String message = "Access denied : only localhost access allowed" ;   
    
    public LocalhostFilter() { super(message); } 

    private static String LOCALHOST_IpV6 =  "0:0:0:0:0:0:0:1" ;
    private static String LOCALHOST_IpV4 =  "127.0.0.1" ;   // Strictly, 127.*.*.*
    
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        String remoteAddr = request.getRemoteAddr() ;
        if ( LOCALHOST_IpV6.equals(remoteAddr) || LOCALHOST_IpV4.equals(remoteAddr) )
            return true ;
        return false ;
    }
}


