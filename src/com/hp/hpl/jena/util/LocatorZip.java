/*
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import com.hp.hpl.jena.shared.JenaException;
import java.io.* ;
import java.util.zip.* ;
import org.apache.commons.logging.*;

/** Location files in a zip file
 *  
 * @author Andy Seaborne
 * @version $Id: LocatorZip.java,v 1.1 2004-08-31 09:49:50 andy_seaborne Exp $
 */
 

class LocatorZip implements Locator
{
    static Log log = LogFactory.getLog(LocatorZip.class) ;
    String zipFileName = null ; 
    ZipFile zipFile = null ;
    
    public LocatorZip(String zfn)
    {
        try {
            zipFileName = zfn ;
            zipFile = new ZipFile(zipFileName) ;
        } catch  (IOException ex)
        { 
            throw new JenaException("Problems accessing "+zipFileName, ex) ;
        }
    }
    
    public InputStream open(String filenameOrURI)
    {
        ZipEntry entry = zipFile.getEntry(filenameOrURI) ;
        if ( entry == null )
        {
            if ( FileManager.logLookupFailures && log.isDebugEnabled() )
                log.debug("Not found in : "+zipFileName+" : "+filenameOrURI) ; 
            return null ;
            
        }
        try
        {
            InputStream in = zipFile.getInputStream(entry) ;
            return in;
        }
        catch (IOException ex)
        {
            log.warn("IO Exception opening zip entry: " + filenameOrURI);
            return null;
        }
    }
    public String getName() { return "LocatorZip("+zipFileName+")" ; } 

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