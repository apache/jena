/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.file;

import java.io.File;

import com.hp.hpl.jena.tdb.sys.Names;

/** 
 *  Wrapper for a file system directory; can create filenames in that directory.
 *  Enforces some simple consistency policies and provides a
 *  "typed string" for a filename to reduce errors.
 */   
 
public class Location
{
    static String pathSeparator = File.separator ;  // Or just "/"
    
//    // Filename bashing moved to FileOps.
//    private static Location dirname(String filename)
//    {
//        filename = filename.replace('\'', '/') ;
//        int i = filename.lastIndexOf('/') ;
//        if ( i == filename.length()-1 )
//            return new Location(filename) ;
//        String dirname = filename.substring(0, i) ; // Exclude final /
//        return new Location(dirname) ;
//    }
//    
//    private static Location ensureDirectory(String dirname)
//    {
//        File file = new File(dirname) ;
//        
//        if ( ! file.exists() )
//        {
//            if ( ! file.mkdirs() )
//                throw new FileException("Failed to create directory: "+file.getAbsolutePath()) ;
//        }
//        if ( ! file.isDirectory() )
//            throw new FileException("Not a directory: "+file.getAbsolutePath()) ;
//        Location loc = new Location() ;
//        loc.setPathname(dirname) ;
//        return loc ;
//    }
    
    private String pathname ;
    private MetaFile metafile = null ; 
    
    static Location mem = new Location() ;
    // Every mem()is a fresh location and importantly fresh metadata.
    static public Location mem() { return new Location(); } 
    
    private Location()
    {
        pathname = Names.memName ;
        metafile = new MetaFile(Names.memName, Names.memName) ;
    }
    
    public Location(String rootname)
    { 
        super() ;
        if ( rootname.equals(Names.memName) )
        {
            pathname = Names.memName ;
            metafile = new MetaFile(Names.memName, Names.memName) ;
            return ;
        }
        
        // Prefer "/"
        rootname = rootname.replace('\'', '/') ;
        File file = new File(rootname) ;
        
        if ( ! file.exists() )
        {
            file.mkdir() ;
            //throw new FileException("Not found: "+file.getAbsolutePath()) ;
        }
        else if ( ! file.isDirectory() )
            throw new FileException("Not a directory: "+file.getAbsolutePath()) ;

        pathname = file.getAbsolutePath() ;
        if ( ! pathname.endsWith(File.separator) && !pathname.endsWith(pathSeparator) )
            pathname = pathname + pathSeparator ;
        
        // Metafilename for a directory.
        String metafileName = getPath(Names.directoryMetafile, Names.extMeta) ;
        
        metafile = new MetaFile("Location: "+rootname, metafileName) ;
    }        

    public String getDirectoryPath()    { return pathname ; }
    public MetaFile getMetaFile()       { return metafile ; }
    public boolean isMem()              { return Names.isMem(pathname) ; }
    
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

    /** Return an absolute filename where relative names are resolved from the location */ 
    public String absolute(String filename, String extension)
    { 
        return (extension == null) ? absolute(filename) : absolute(filename+"."+extension) ;
    }
    
    /** Return an absolute filename where relative names are resolved from the location */ 
    public String absolute(String filename)
    {
        File f = new File(filename) ;
        // Location relative.
        if ( ! f.isAbsolute() )
            filename = pathname+filename ;
        return filename ;
    }
 
    public boolean exists(String filename) { return exists(filename, null) ; }
    
    public boolean exists(String filename, String ext)
    {
        String fn = getPath(filename, ext) ;
        File f = new File(fn) ;
        return f.exists() ;
    }

    /** Return the name of the file relative to this location */ 
    public String getPath(String filename)
    {
        return getPath(filename, null) ;
    }
    
    /** Return the name of the file, and extension, relative to this location */ 
    public String getPath(String filename, String ext)
    {
        check(filename, ext) ;
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
        if ( filename.contains(".") && ext != null )
            throw new FileException("Filename has an extension: "+filename) ;
        if ( ext != null )
        {
            if ( ext.contains(".") )
                throw new FileException("Extension has an extension: "+filename) ;
        }
    }
    
    @Override
    public String toString() { return "location:"+pathname ; }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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