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

package com.hp.hpl.jena.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Location files named by a URL
 */

public class LocatorURL implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorURL.class) ;
    static final String acceptHeader = "application/rdf+xml,application/xml;q=0.9,*/*;q=0.5" ;
    
    static final String[] schemeNames = { "http:" , "https:" } ;    // Must be lower case and include the ":"
    
    @Override
    public TypedStream open(String filenameOrURI)
    {
        if ( ! acceptByScheme(filenameOrURI) )
        {
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Not found : "+filenameOrURI) ; 
            return null;
        }
        
        try
        {
            URL url = new URL(filenameOrURI);
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Accept", acceptHeader) ;
            conn.setRequestProperty("Accept-Charset", "utf-8,*") ;
            conn.setDoInput(true) ;
            conn.setDoOutput(false) ;
            // Default is true.  See javadoc for HttpURLConnection
            //((HttpURLConnection)conn).setInstanceFollowRedirects(true) ;
            conn.connect() ;
            InputStream in = new BufferedInputStream(conn.getInputStream());
            
            if ( FileManager.logAllLookups  && log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI) ;
            return new TypedStream(in, conn.getContentType()) ; 
        }
        catch (java.io.FileNotFoundException ex) 
        {
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("LocatorURL: not found: "+filenameOrURI) ; 
            return null ;
        }
        catch (MalformedURLException ex)
        {
            log.warn("Malformed URL: " + filenameOrURI);
            return null;
        }
        // IOExceptions that occur sometimes.
        catch (java.net.UnknownHostException ex)
        {
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("LocatorURL: not found (UnknownHostException): "+filenameOrURI) ;
            return null ;
        }
        catch (java.net.ConnectException ex)
        { 
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("LocatorURL: not found (ConnectException): "+filenameOrURI) ;
            return null ;
        }
        catch (java.net.SocketException ex)
        {
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("LocatorURL: not found (SocketException): "+filenameOrURI) ;
            return null ;
        }
        // And IOExceptions we don't expect
        catch (IOException ex)
        {
            log.warn("I/O Exception opening URL: " + filenameOrURI+"  "+ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public boolean equals( Object other )
    {
        return other instanceof LocatorURL;
    }

    @Override
    public int hashCode()
    {
        return LocatorURL.class.hashCode();
    }
    
    @Override
    public String getName() { return "LocatorURL" ; } 
    
    private boolean acceptByScheme(String filenameOrURI)
    {
        String uriSchemeName = getScheme(filenameOrURI) ;
        if ( uriSchemeName == null )
            return false ;
        uriSchemeName = uriSchemeName.toLowerCase(Locale.ENGLISH) ;
        for ( String schemeName : schemeNames )
        {
            if ( uriSchemeName.equals( schemeName ) )
            {
                return true;
            }
        }
        return false ;
    }

    private boolean hasScheme(String uri, String scheme)
    {
        String actualScheme = getScheme(uri) ;
        if ( actualScheme == null )
            return false ;
        return actualScheme.equalsIgnoreCase(scheme) ; 
    }
    
    // Not perfect - but we support Java 1.3 (as of August 2004)
    private String getScheme(String uri)
    {
        int ch = uri.indexOf(':') ;
        if ( ch < 0 )
            return null ;
        
        // Includes the : 
        return uri.substring(0,ch+1) ;
    }

}
