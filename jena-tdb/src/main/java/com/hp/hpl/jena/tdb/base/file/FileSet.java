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

import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.Tuple ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Naming metadata management to a collection of related files
 *  (same directory, same basename within directory, various extensions).
 */
public class FileSet
{
    // Cope with "in-memory" fileset (location == null)
    
    private static Logger log = LoggerFactory.getLogger(FileSet.class) ;
    
    private Location location ;
    private String basename ;

    /** FileSet for "in-memory" */
    public static FileSet mem()
    {
        FileSet fs = new FileSet(Location.mem(), "mem" ) ;
        return fs ;
    }

    //private FileSet() {}        // Uninitialized.

    
    /** Create a FileSet given Location (directory) and name within the directory */  
    public FileSet(String directory, String basename)
    {
        initFileSet(new Location(directory), basename) ;
    }
    
    /** Create a FileSet given Location (directory) and name within the directory */  
    public FileSet(String filename)
    {
        Tuple<String> t = FileOps.splitDirFile(filename) ;
        String dir = t.get(0) ;
        String fn = t.get(1) ;
        if ( dir == null )
            dir = "." ;
        initFileSet(new Location(dir), fn) ;
    }
    
    /** Create a FileSet given Location (directory) and name within the directory */  
    public FileSet(Location directory, String basename)
    {
        initFileSet(directory, basename) ;
    }
    
    private void initFileSet(Location directory, String basename)
    {
        // Default - don't use the locations metadata 
        initFileSet(directory, basename, false) ;
    }
    
    private void initFileSet(Location directory, String basename, boolean useLocationMetadata)
    {
        this.location = directory ;
        this.basename = basename ;
    }
    
    public Location getLocation()   { return location ; }
    public String getBasename()     { return basename ; }
    //public MetaFile getMetaFile()   { return metafile ; }
    
    public boolean isMem()
    {
        return location.isMem() ;
    }

    public boolean exists(String ext)
    {
        if ( location.isMem() )
            return true ;
        String fn = filename(ext) ;
        File f = new File(fn) ;
        if ( f.isDirectory() )
            log.warn("File clashes with a directory") ;
        return f.exists() && f.isFile() ;
    }
    
    @Override
    public String toString()
    {
        return "FileSet:"+filename(null) ;
    }
    
    public String filename(String ext)
    {
        return location.getPath(basename, ext) ;
    }
}
