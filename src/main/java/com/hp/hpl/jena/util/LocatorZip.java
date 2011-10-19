/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.shared.JenaException;

/** Location files in a zip file
 *  
 * @author Andy Seaborne
 * @version $Id: LocatorZip.java,v 1.1 2009-06-29 08:55:47 castagna Exp $
 */
 

class LocatorZip implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorZip.class) ;
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
    
    @Override
    public TypedStream open(String filenameOrURI)
    {
        ZipEntry entry = zipFile.getEntry(filenameOrURI) ;
        if ( entry == null )
        {
            if ( FileManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not found: "+zipFileName+" : "+filenameOrURI) ; 
            return null ;
            
        }
        try
        {
            InputStream in = zipFile.getInputStream(entry) ;
            
            if ( in == null )
            {
                if ( FileManager.logAllLookups && log.isTraceEnabled() )
                    log.trace("Not found: "+filenameOrURI) ; 
                return null ;
            }
            
            if ( FileManager.logAllLookups  && log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI) ;
            return new TypedStream(in) ;
        }
        catch (IOException ex)
        {
            log.warn("IO Exception opening zip entry: " + filenameOrURI);
            return null;
        }
    }
    @Override
    public String getName() { return "LocatorZip("+zipFileName+")" ; } 

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