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

package com.hp.hpl.jena.tdb.base.file ;

import java.io.File ;
import java.io.IOException ;

import org.apache.jena.atlas.lib.Lib ;
import com.hp.hpl.jena.tdb.sys.Names ;

/**
 * Wrapper for a file system directory; can create filenames in that directory.
 * Enforces some simple consistency policies and provides a "typed string" for a
 * filename to reduce errors.
 */

public class Location {
    static String    pathSeparator = File.separator ; // Or just "/"

    private static String memNamePath = Names.memName+pathSeparator ;
        
    private String   pathname ;
    private MetaFile metafile      = null ;
    private boolean  isMem         = false ;
    private boolean  isMemUnique   = false ;
    private LocationLock lock ;

    static int       memoryCount   = 0 ;

    /**
     * Return a fresh memory location : always unique, never .equals to another
     * location.
     */
    static public Location mem() {
        return mem(null) ;
    }

    /** Return a memory location with a name */
    static public Location mem(String name) {
        Location loc = new Location() ;
        memInit(loc, name) ;
        return loc ;
    }

    private Location() {}

    private static void memInit(Location location, String name) {
        location.pathname = Names.memName ;
        if ( name != null ) {
            name = name.replace('\\', '/') ;
            location.pathname = location.pathname + '/' + name ;
        } else
            location.isMemUnique = true ;
        if ( !location.pathname.endsWith(pathSeparator) )
            location.pathname = location.pathname + '/' ;
        location.isMem = true ;
        location.metafile = new MetaFile(Names.memName, Names.memName) ;
        location.lock = new LocationLock(location);
    }

    public Location(String rootname) {
        super() ;
        if ( rootname.equals(Names.memName) ) {
            memInit(this, null) ;
            return ;
        }
        if ( rootname.startsWith(memNamePath) ) {
            String name = rootname.substring(memNamePath.length()) ;
            memInit(this, name) ;
            return ;
        }

        ensure(rootname) ;
        pathname = fixupName(rootname) ;
        // Metafilename for a directory.
        String metafileName = getPath(Names.directoryMetafile, Names.extMeta) ;

        metafile = new MetaFile("Location: " + rootname, metafileName) ;
        
        // Set up locking
        // Note that we don't check the lock in any way at this point, checking
        // and obtaining the lock is carried out by StoreConnection
        lock = new LocationLock(this);
    }

    // MS Windows:
    // getCanonicalPath is only good enough for existing files.
    // It leaves the case as it finds it (upper, lower) and lower cases
    // not-existing segments. But later creation of a segment with uppercase
    // changes the exact string returned.
    private String fixupName(String fsName) {
        if (  isMem() )
            return fsName ;
        File file = new File(fsName) ;
        try {
            fsName = file.getCanonicalPath() ;
        } catch (IOException ex) {
            throw new FileException("Failed to get canoncial path: " + file.getAbsolutePath(), ex) ;
        }

        if ( !fsName.endsWith(File.separator) && !fsName.endsWith(pathSeparator) )
            fsName = fsName + pathSeparator ;
        return fsName ;
    }
    
    public String getDirectoryPath() {
        return pathname ;
    }

    public MetaFile getMetaFile() {
        return metafile ;
    }

    public boolean isMem() {
        return isMem ;
    }

    public boolean isMemUnique() {
        return isMemUnique ;
    }
    
    public LocationLock getLock() {
    	return lock;
    }

    public Location getSubLocation(String dirname) {
        String newName = pathname + dirname ;
        ensure(newName) ;
        return new Location(newName) ;
    }

    private void ensure(String dirname) {
        if ( isMem() )
            return ;
        File file = new File(dirname) ;
        if ( file.exists() && !file.isDirectory() )
            throw new FileException("Existing file: " + file.getAbsolutePath()) ;
        if ( !file.exists() )
            file.mkdir() ;
    }
    
    public String getSubDirectory(String dirname) {
        return getSubLocation(dirname).getDirectoryPath() ;
    }

    /**
     * Return an absolute filename where relative names are resolved from the
     * location
     */
    public String absolute(String filename, String extension) {
        return (extension == null) ? absolute(filename) : absolute(filename + "." + extension) ;
    }

    /**
     * Return an absolute filename where relative names are resolved from the
     * location
     */
    public String absolute(String filename) {
        File f = new File(filename) ;
        // Location relative.
        if ( !f.isAbsolute() )
            filename = pathname + filename ;
        return filename ;
    }

    /** Does the location exist (and it a directory, and is accessible) */
    public boolean exists() {
        File f = new File(getDirectoryPath()) ;
        return f.exists() && f.isDirectory() && f.canRead() ;
    }

    public boolean exists(String filename) {
        return exists(filename, null) ;
    }

    public boolean exists(String filename, String ext) {
        String fn = getPath(filename, ext) ;
        File f = new File(fn) ;
        return f.exists() ;
    }

    /** Return the name of the file relative to this location */
    public String getPath(String filename) {
        return getPath(filename, null) ;
    }

    /** Return the name of the file, and extension, relative to this location */
    public String getPath(String filename, String ext) {
        check(filename, ext) ;
        if ( ext == null )
            return pathname + filename ;
        return pathname + filename + "." + ext ;
    }

    private void check(String filename, String ext) {
        if ( filename == null )
            throw new FileException("Location: null filename") ;
        if ( filename.contains("/") || filename.contains("\\") )
            throw new FileException("Illegal file component name: " + filename) ;
        if ( filename.contains(".") && ext != null )
            throw new FileException("Filename has an extension: " + filename) ;
        if ( ext != null ) {
            if ( ext.contains(".") )
                throw new FileException("Extension has an extension: " + filename) ;
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31 ;
        int result = isMem ? 1 : 2 ;
        result = prime * result + ((pathname == null) ? 0 : pathname.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true ;
        if ( obj == null )
            return false ;
        if ( getClass() != obj.getClass() )
            return false ;

        Location other = (Location)obj ;
        if ( isMem && !other.isMem )
            return false ;
        if ( !isMem && other.isMem )
            return false ;
        // Not == so ...
        if ( isMemUnique )
            return false ;

        return Lib.equal(pathname, other.pathname) ;
    }

    @Override
    public String toString() {
        return "location:" + pathname ;
    }
}
