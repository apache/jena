/*
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.*;

/** Location files in the filing system.
 *  A FileLocator can have a "current directory" - this is separate from any
 *  location mapping (see @link{LocationMapping}) as it applies only to files.
 * 
 * @author Andy Seaborne
 * @version $Id: LocatorFile.java,v 1.3 2004-11-20 21:35:43 andy_seaborne Exp $
 */

public class LocatorFile implements Locator
{
    static Log log = LogFactory.getLog(LocatorFile.class) ;
    private String altDir = null ;
    private String altDirLogStr = "" ;
    
    LocatorFile(String dir)
    {
        if ( false )
        {
            if ( dir == null )
            {
                try {
                    //String wd = System.getProperty("user.dir") ;
                    String wd = new File(".").getCanonicalPath() ;
                    log.debug("Base file directory: "+wd) ;
                } catch (IOException ex)
                {
                    log.error("Failed to discover the working directory", ex) ;
                }
                return ;
            }
            else
            {
                log.debug("Base file directory: "+dir) ;
            }
        }
        if ( dir != null )
        {
            if ( dir.endsWith("/") || dir.endsWith(java.io.File.separator) )
                dir = dir.substring(0,dir.length()-1) ;
            altDirLogStr = " ["+dir+"]" ;
        }
        altDir = dir ;
    }

    LocatorFile()
    {
        this(null) ;
    }
    
    private File toFile(String filenameOrURI)
    {
        String fn = FileUtils.toFilename(filenameOrURI) ;
        if ( fn == null )
            return null ;
        
        if ( altDir != null && ! fn.startsWith("/") && ! fn.startsWith(FileManager.filePathSeparator) )
            fn = altDir+java.io.File.separator+fn ;
                     
        return new File(fn) ;
    }
    
    
    public boolean exists(String filenameOrURI)
    {
        File f = toFile(filenameOrURI) ;
        
        if ( f == null )
            return false ;
        
        return f.exists() ;
    }
    
    public InputStream open(String filenameOrURI)
    {
        File f = toFile(filenameOrURI) ;

        if ( f == null || !f.exists() )
        {
            if ( FileManager.logAllLookups && log.isTraceEnabled())
                log.trace("Not found: "+filenameOrURI+altDirLogStr) ;
            return null ;
        }
        
        try {
            InputStream in = new FileInputStream(f) ;
            if ( in == null )
            {
                // Should not happen 
                if ( FileManager.logAllLookups && log.isTraceEnabled() )
                    log.trace("LocatorFile: Failed to open: "+filenameOrURI+altDirLogStr) ;
                return null ;
            }
            
            if ( FileManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI+altDirLogStr) ;
                
            
            // Create base -- Java 1.4-isms
            //base = f.toURI().toURL().toExternalForm() ;
            //base = base.replaceFirst("^file:/([^/])", "file:///$1") ;
            return in ;
        } catch (IOException ioEx)
        {
            // Includes FileNotFoundException
            // We already tested whether the file exists or not.
            log.warn("File unreadable (but exists): "+f.getPath()+" Exception: "+ioEx.getMessage()) ;
            return null ;
        }
    }
    public String getName() { return "LocatorFile" ; } 
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