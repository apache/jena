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

package org.apache.jena.fuseki.conneg;

import java.util.Enumeration ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.fuseki.HttpNames ;

public class WebLib
{
    /** Split a string, removing whitespace around the split string.
     * e.g. Use in splitting HTTP accept/content-type headers.  
     */
    public static String[] split(String s, String splitStr)
    {
        String[] x = s.split(splitStr,2) ;
        for ( int i = 0 ; i < x.length ; i++ )
        {
            x[i] = x[i].trim() ;
        }
        return x ;
    }

    /** Migrate to WebLib */
    public static String getAccept(HttpServletRequest httpRequest)
    {
        // There can be multiple accept headers -- note many tools don't allow these to be this way (e.g. wget, curl)
        Enumeration<String> en = httpRequest.getHeaders(HttpNames.hAccept) ;
        if ( ! en.hasMoreElements() )
            return null ;
        StringBuilder sb = new StringBuilder() ;
        String sep = "" ;
        for ( ; en.hasMoreElements() ; )
        {
            String x = en.nextElement() ;
            sb.append(sep) ;
            sep = ", " ;
            sb.append(x) ;
        }
        return sb.toString() ;
    }
}
