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

import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.conneg.WebLib ;
import org.openjena.riot.WebContent ;

public class ResponseOps
{

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
        return expandShortName(x) ; 
    }

    public static String paramStylesheet(HttpServletRequest request)
    { return fetchParam(request, HttpNames.paramStyleSheet) ; }

    public static String paramOutput(HttpServletRequest request)
    {
        // Two names.
        String x = fetchParam(request, HttpNames.paramOutput1) ;
        if ( x == null )
            x = fetchParam(request, HttpNames.paramOutput2) ;
        return expandShortName(x) ; 
    }

    public static String paramAcceptField(HttpServletRequest request)
    {
        String acceptField = WebLib.getAccept(request) ;
        String acceptParam = fetchParam(request, HttpNames.paramAccept) ;
        
        if ( acceptParam != null )
            acceptField = acceptParam ;
        if ( acceptField == null )
            return null ;
        return expandShortName(acceptField) ; 
    }

    public static String expandShortName(String str)
    {
        if ( str == null )
            return null ;
        // Some short names.
        if ( str.equalsIgnoreCase(ResponseResultSet.contentOutputJSON) ) 
            return WebContent.contentTypeResultsJSON ;
        
        if ( str.equalsIgnoreCase(ResponseResultSet.contentOutputSPARQL) )
            return WebContent.contentTypeResultsXML ;
        
        if ( str.equalsIgnoreCase(ResponseResultSet.contentOutputXML) )
            return WebContent.contentTypeResultsXML ;
        
        if ( str.equalsIgnoreCase(ResponseResultSet.contentOutputText) )
            return WebContent.contentTypeTextPlain ;
        
        if ( str.equalsIgnoreCase(ResponseResultSet.contentOutputCSV) )
            return WebContent.contentTypeTextCSV ;
        
        if ( str.equalsIgnoreCase(ResponseResultSet.contentOutputTSV) )
            return WebContent.contentTypeTextTSV ;
        
        return str ;
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

