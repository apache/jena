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

import java.io.IOException ;
import java.util.Locale ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.fuseki.HttpNames ;

public class ResponseOps
{
    // Helpers
    public static void put(Map<String, String> map, String key, String value)
    {
        map.put(key.toLowerCase(Locale.ROOT), value) ;
    }
    
    public static boolean isEOFexception(IOException ioEx)
    {
        if ( ioEx.getClass().getName().equals("org.mortbay.jetty.EofException eofEx") )
            return true ;
        if ( ioEx instanceof java.io.EOFException )
            return true ;
        return false ;
    }

    public static String paramForceAccept(HttpServletRequest request)
    {
        String x = fetchParam(request, HttpNames.paramForceAccept) ;
        return x ; 
    }

    public static String paramStylesheet(HttpServletRequest request)
    { return fetchParam(request, HttpNames.paramStyleSheet) ; }

    public static String paramOutput(HttpServletRequest request, Map<String,String> map)
    {
        // Two names.
        String x = fetchParam(request, HttpNames.paramOutput1) ;
        if ( x == null )
            x = fetchParam(request, HttpNames.paramOutput2) ;
        return expandShortName(x, map) ;
    }

    public static String expandShortName(String str, Map<String,String> map)
    {
        if ( str == null )
            return null ;
        // Force keys to lower case. See put() above.
        String key = str.toLowerCase(Locale.ROOT) ;
        String str2 = map.get(key) ;
        if ( str2 == null )
            return str ;
        return str2 ;
    }

    public static String paramCallback(HttpServletRequest request)
    { 
        return fetchParam(request, HttpNames.paramCallback) ;
    }

    public static String fetchParam(HttpServletRequest request, String parameterName)
    {
        String value = request.getParameter(parameterName) ;
        if ( value != null )
        {
            value = value.trim() ;
            if ( value.length() == 0 )
                value = null ;
        }
        return value ;
    }

}

