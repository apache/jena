/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.File;

/** Wrapper for a file system directory; can create filenames in that directory.
 *  Enforces some simple consistency policies
 *  on naming to reduce errors.   
 *   
 *   */ 

public class Location
{
    public static Location ensureDirectory(String dirname)
    {
        File file = new File(dirname) ;
        
        if ( ! file.exists() )
        {
            if ( ! file.mkdir() )
                throw new FileException("Failed to create directory: "+file.getAbsolutePath()) ;
        }
        if ( ! file.isDirectory() )
            throw new FileException("Not a directory: "+file.getAbsolutePath()) ;
        Location loc = new Location() ;
        loc.setPathname(dirname) ;
        return loc ;
    }
    
    String pathname ;
    private Location() {}
    
    private void setPathname(String pathname)
    {
        if ( ! pathname.endsWith(File.separator) )
            pathname = pathname + File.separator ;
        this.pathname = pathname ;
    }
    
    public Location(String rootname)
    { 
        File file = new File(rootname) ;
        
        if ( ! file.exists() )
        {
            file.mkdir() ;
            //throw new FileException("Not found: "+file.getAbsolutePath()) ;
        }

        if ( ! file.isDirectory() )
            throw new FileException("Not a directory: "+file.getAbsolutePath()) ;

        setPathname(file.getAbsolutePath()) ;
    }        
    
    public String getDirectoryPath()
    {
        return pathname ;
    }

    public Location getSubLocation(String dirname)
    {
        String newName = pathname+dirname ;
        File file = new File(newName) ;
        if ( file.exists() && ! file.isDirectory() )
            throw new FileException("Existing file: "+file.getAbsolutePath()) ;
        if ( ! file.exists() )
            file.mkdir() ;
        
        return new Location(newName) ;
    }

    public String getSubDirectory(String dirname)
    {
        return getSubLocation(dirname).getDirectoryPath() ;
    }

    
    public String getPath(String filename, String ext)
    {
        check(filename, null) ;
        if ( ext == null )
            return pathname+filename ;
        return pathname+filename+"."+ext ;
    }

    private void check(String filename, String ext)
    {
        if ( filename == null )
            throw new FileException("Location: null filename") ;
        if ( filename.contains("/") || filename.contains("\\") )
            throw new FileException("Illegal file component name: "+filename) ;
        if ( filename.contains(".") )
            throw new FileException("Filename has an extension: "+filename) ;
        if ( ext != null )
        {
            if ( ext.contains(".") )
                throw new FileException("Extension has an extension: "+filename) ;
        }
    }
    
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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