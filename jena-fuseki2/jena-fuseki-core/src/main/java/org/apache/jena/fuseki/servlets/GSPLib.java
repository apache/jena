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

import javax.servlet.http.HttpServletRequest;

public class GSPLib {

    /** Check whether there is exactly one HTTP header value */
    public static boolean hasExactlyOneValue(HttpAction action, String name) {
        String[] values = action.request.getParameterValues(name);
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
