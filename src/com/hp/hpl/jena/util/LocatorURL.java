/*
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.*;

/** Location files named by a URL
 * 
 * @author Andy Seaborne
 * @version $Id: LocatorURL.java,v 1.3 2004-11-20 21:35:43 andy_seaborne Exp $
 */

public class LocatorURL implements Locator
{
    static Log log = LogFactory.getLog(LocatorURL.class) ;

    public InputStream open(String filenameOrURI)
    {
        if ( ! hasScheme(filenameOrURI, "http:") 
            // && ! hasScheme(filenameOrURI, "file:") // Leave a filelocator to hanlde this. 
            ) 
        {
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Not found: "+filenameOrURI) ; 
            return null;
        }
        
        try
        {
            URL url = new URL(filenameOrURI);
            InputStream in = new BufferedInputStream(url.openStream());
            if ( in == null )
            {
                if ( FileManager.logAllLookups && log.isTraceEnabled() )
                    log.trace("Not found: "+filenameOrURI) ; 
                return null ;
            }
            if ( FileManager.logAllLookups  && log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI) ;
            return in;
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
        catch (IOException ex)
        {
            log.warn("IO Exception opening URL: " + filenameOrURI);
            return null;
        }
    }
    public String getName() { return "LocatorURL" ; } 
    
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
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
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