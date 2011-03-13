/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package fm2.atlas;

import java.io.BufferedInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.net.MalformedURLException ;
import java.net.URL ;
import java.net.URLConnection ;

import org.openjena.atlas.web.TypedStream ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Location files named by a URL */

public class LocatorURL implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorURL.class) ;
    static final String acceptHeader = "application/rdf+xml,application/xml;q=0.9,*/*;q=0.5" ;
    
    static final String[] schemeNames = { "http:" , "https:" } ;    // Must be lower case and include the ":"

    public TypedStream open(String filenameOrURI)
    {
        if ( ! acceptByScheme(filenameOrURI) )
        {
            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
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
            
            if ( StreamManager.logAllLookups  && log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI) ;
            // Parse content type into a ContentType and ...
            //return new TypedStream(in, ct) ;
            return new TypedStream(in) ;
        }
        catch (java.io.FileNotFoundException ex) 
        {
            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
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
            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
                log.trace("LocatorURL: not found (UnknownHostException): "+filenameOrURI) ;
            return null ;
        }
        catch (java.net.ConnectException ex)
        { 
            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
                log.trace("LocatorURL: not found (ConnectException): "+filenameOrURI) ;
            return null ;
        }
        catch (java.net.SocketException ex)
        {
            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
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
    
    public String getName() { return "LocatorURL" ; } 
    
    private boolean acceptByScheme(String filenameOrURI)
    {
        String uriSchemeName = getScheme(filenameOrURI) ;
        if ( uriSchemeName == null )
            return false ;
        uriSchemeName = uriSchemeName.toLowerCase() ; 
        for ( int i = 0 ; i < schemeNames.length ; i++ )
        {
            if ( uriSchemeName.equals(schemeNames[i]) )
                return true ;
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
/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */