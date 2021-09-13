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

package org.apache.jena.fuseki.servlets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.riot.web.HttpNames;

public class GSPLib {
    
    /** Test whether the operation has either of the GSP parameters. */
    public static boolean hasGSPParams(HttpAction action) {
        if ( action.getRequestQueryString() == null )
            return false;
        boolean hasParamGraphDefault = action.getRequestParameter(HttpNames.paramGraphDefault) != null;
        if ( hasParamGraphDefault )
            return true;
        boolean hasParamGraph = action.getRequestParameter(HttpNames.paramGraph) != null;
        if ( hasParamGraph )
            return true;
        return false;
    }

    /** Test whether the operation has exactly one GSP parameter and no other parameters. */ 
    public static boolean hasGSPParamsStrict(HttpAction action) {
        if ( action.getRequestQueryString() == null )
            return false;
        Map<String, String[]> params = action.getRequestParameterMap();
        if ( params.size() != 1 )
            return false;
        boolean hasParamGraphDefault = GSPLib.hasExactlyOneValue(action, HttpNames.paramGraphDefault);
        boolean hasParamGraph = GSPLib.hasExactlyOneValue(action, HttpNames.paramGraph);
        // Java XOR
        return hasParamGraph ^ hasParamGraphDefault;
    }

    /** Check whether there is exactly one HTTP header value */
    public static boolean hasExactlyOneValue(HttpAction action, String name) {
        String[] values = action.getRequestParameterValues(name);
        if ( values == null )
            return false;
        if ( values.length == 0 )
            return false;
        if ( values.length > 1 )
            return false;
        return true;
    }

    /** Get one value where there may be several HTTP header values.
     * Multiple values causes an exception.
     * No value returns null.
     */
    public static String getOneOnly(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if ( values == null )
            return null;
        if ( values.length == 0 )
            return null;
        if ( values.length > 1 )
            ServletOps.errorBadRequest("Multiple occurrences of '" + name + "'");
        return values[0];
    }
}
