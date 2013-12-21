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

package org.apache.jena.atlas.lib;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import java.nio.channels.FileChannel ;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.logging.Log ;

public class FileOps
{
    private FileOps() {}
    
    /** Delete a file
     * 
     * @param filename
     */
    public static void delete(String filename)
    {
        delete(new File(filename), true) ;
    }
    
    /* Delete a file - don't check it worked */
    
    public static void deleteSilent(String filename)
    {
        delete(new File(filename), false) ;
    }
    
    public static void delete(File f, boolean reportExistsAfter)
    {
        try {
            /* Note: On windows, deleting a file which has been memory
             * mapped does not delete the file.
             */ 
            f.delete() ;
            if ( reportExistsAfter && f.exists() )
                Log.warn(FileOps.class, "delete: *** File still exists: "+f) ;
        } catch (SecurityException ex)
        {
            Log.warn(FileOps.class, "delete: "+f+": Security exception; "+ex.getMessage()) ;
        }
            
    }
    
    public static void clearDirectory(String dir)
    {
        File d = new File(dir) ;
        for ( File f : d.listFiles())
        {
            if ( f.isFile() )
                delete(f, false) ;
        }
    }

    /** See if there are any files in this directory */ 
    public static boolean existsAnyFiles(String dir)
    {
        File d = new File(dir) ;
        File[] entries = d.listFiles() ;
        if ( entries == null )
            // Not a directory
            return false ;
        return entries.length > 0 ;
    }

    public static boolean exists(String path)
    {
        File f = new File(path) ;
        return f.exists() ; 
    }
    
    public static boolean isEmpty(String filename)
    {
        File f = new File(filename) ;
        if ( f.exists() ) return true ;
        if ( f.isFile() ) return f.length() == 0 ;
        throw new AtlasException("Not a file") ;
    }

    public static void ensureDir(String dirname)
    {
        File dir = new File(dirname) ;
        if ( ! dir.exists() )
            dir.mkdirs() ;
    }
    
    /** Split a file name into path, basename and extension.  Nulls returned if don't make sense. */
    public static Tuple<String> splitDirBaseExt(String filename)
    {
        String path = null ;
        String basename = filename ;
        String ext = null ;
        
        int j = filename.lastIndexOf('/') ;
        if ( j < 0 )
            j = filename.lastIndexOf('\\') ;

        if ( j >= 0 )
        {
            path = filename.substring(0, j) ;
            basename = filename.substring(j+1) ;
        }
        
        int i = basename.lastIndexOf('.') ;
        
        if ( i > -1 )
        {
            ext = basename.substring(i+1) ;
            basename = basename.substring(0, i) ;
        }
        
        return Tuple.createTuple(path, basename, ext) ;
    }
    
    /** Split a file name into path and filename.  Nulls returned if don't make sense. */
    public static Tuple<String> splitDirFile(String filename)
    {
        String path = null ;
        String fn = filename ;
        
        int j = filename.lastIndexOf('/') ;
        if ( j < 0 )
            j = filename.lastIndexOf('\\') ;
        
        if ( j >= 0 )
        {
            path = filename.substring(0, j) ;
            fn = filename.substring(j+1) ;
        }
        return Tuple.createTuple(path, fn) ;
    }

    /** Return the basename (no path, no extension) */
    public static String basename(String filename)
    {
        int j = filename.lastIndexOf('/') ;
        if ( j < 0 )
            j = filename.lastIndexOf('\\') ;

        String fn = ( j >= 0 ) ? filename.substring(j+1) : filename ;
        int i = fn.lastIndexOf('.') ;
        
        if ( i > -1 )
            return fn.substring(0, i) ;
        return fn ;
    }
    
    /** Return the extension (or "") */
    public static String extension(String filename) {
        int iSlash = filename.lastIndexOf( '/' );      
        int iBack = filename.lastIndexOf( '\\' );
        int iExt = filename.lastIndexOf( '.' ); 
        if (iBack > iSlash) iSlash = iBack;
        return iExt > iSlash ? filename.substring( iExt+1 ).toLowerCase() : "";
    }
    
    public static String fullPath(String filename)
    {
        File f = new File(filename);
        return f.getAbsolutePath();
    }
    
    public static String fullDirectoryPath(String filename)
    {
        File f = new File(filename);
        if (f.isDirectory()) {
            return f.getAbsolutePath();
        } else if (f.getParentFile() != null) {
            return f.getParentFile().getAbsolutePath();
        } else {
            return f.getAbsolutePath();
        }
    }
    
    /** Copy a file */
    public static void copyFile(File source, File dest) {
        try {
            @SuppressWarnings("resource")
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            @SuppressWarnings("resource")
            FileChannel destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            sourceChannel.close();
            destChannel.close();
        } catch (IOException ex) { IO.exception(ex); }
    }

    
//    public static String getExt(String filename)
//    {
//        int i = filename.lastIndexOf('.') ;
//        int j = filename.lastIndexOf('/') ;
//        if ( i > j )
//            return filename.substring(i+1) ;
//        return null ;
//    }
}
