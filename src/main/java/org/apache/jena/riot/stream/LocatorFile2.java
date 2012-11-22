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

package org.apache.jena.riot.stream;

import java.io.File ;
import java.io.IOException ;
import java.io.InputStream ;
import java.security.AccessControlException ;

import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.TypedInputStream2 ;
import org.openjena.atlas.io.IO ;
import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.web.ContentType ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.util.FileUtils ;

/** Location files in the filing system.
 *  A FileLocator can have a "current directory" - this is separate from any
 *  location mapping (see @link{LocationMapping}) as it applies only to files.
 */

public class LocatorFile2 implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorFile2.class) ;
    private final String thisDir ;
    private final String thisDirLogStr ;

    /** Create a LocatorFile */
    public LocatorFile2() { this(null) ; }
    
    /** Create a LocatorFile that uses the argument as it's working directory
     * The working directory should be a UNIX style file name,
     * (relative or absolute), not a URI.
     */
    public LocatorFile2(String dir)
    {
        if ( dir != null )
        {
            if ( dir.endsWith("/") || dir.endsWith(java.io.File.separator) )
                dir = dir.substring(0,dir.length()-1) ;
            thisDirLogStr = " ["+dir+"]" ;
        }
        else
            thisDirLogStr = "" ;
        thisDir = dir ;
    }

    // Two LocatorFile are the same if they would look up names to the same files.
    
    /** To a File, after processing the filename for file: or relative filename */
    public String toFileName(String filenameIRI)
    {
        // Do not use : it will ignore the directory. 
        //IRILib.filenameToIRI
        
        String scheme = FileUtils.getScheme(filenameIRI) ;
        String fn = filenameIRI ;
        // Windows : C:\\ is not a scheme name!
        if ( scheme != null && scheme.length() > 1 )
        {
            if ( ! scheme.equalsIgnoreCase("file") )
                // Not filename or a file: IRI
                return null ;
            fn = IRILib.IRIToFilename(filenameIRI) ;
        }
        // fn is the file name to use.
        // If it is relative, and we have a different working directory, prepend that.  
        // "/" is a path separator on Windows as well.
        if ( thisDir != null && ! fn.startsWith("/") && ! fn.startsWith(File.separator) )
            fn = thisDir+"/"+fn ;
        return fn ;
    }
    
    public String getThisDir()
    {
        return thisDir ;
    }

    public boolean exists(String fileIRI)
    {
        String fn = toFileName(fileIRI) ;
        if ( fn == null )
            return false ;
        
        return exists$(fn) ;
    }
    
    private boolean exists$(String fn)
    {
        return new File(fn).exists() ;
    }

    /** Open anything that looks a bit like a file name */ 
    @Override
    public TypedInputStream2 open(String filenameIRI)
    {
        String fn = toFileName(filenameIRI) ;
        if ( fn == null )
            return null ;
        
        try {
            if ( ! exists$(fn) )
            {
                if ( StreamManager.logAllLookups && log.isTraceEnabled())
                    log.trace("Not found: "+filenameIRI+thisDirLogStr) ;
                return null ;
            }
        } catch (AccessControlException e) {
            log.warn("Security problem testing for file", e);
            return null;
        }
        
        try {
            InputStream in = IO.openFileEx(fn) ;

            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Found: "+filenameIRI+thisDirLogStr) ;
            
            ContentType ct = RDFLanguages.guessContentType(filenameIRI) ;
            return new TypedInputStream2(in, ct, filenameIRI) ;
        } catch (IOException ioEx)
        {
            // Includes FileNotFoundException
            // We already tested whether the file exists or not.
            log.warn("File unreadable (but exists): "+fn+" Exception: "+ioEx.getMessage()) ;
            return null ;
        }
    }
    
    @Override
    public String getName()
    {
        String tmp = "LocatorFile" ;
        if ( thisDir != null )
            tmp = tmp+"("+thisDir+")" ;
        return tmp ;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31 ;
        int result = 1 ;
        result = prime * result + ((thisDir == null) ? 0 : thisDir.hashCode()) ;
        result = prime * result + ((thisDirLogStr == null) ? 0 : thisDirLogStr.hashCode()) ;
        return result ;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true ;
        if (obj == null) return false ;
        if (getClass() != obj.getClass()) return false ;
        LocatorFile2 other = (LocatorFile2)obj ;
        if (thisDir == null)
        {
            if (other.thisDir != null) return false ;
        } else
            if (!thisDir.equals(other.thisDir)) return false ;
        if (thisDirLogStr == null)
        {
            if (other.thisDirLogStr != null) return false ;
        } else
            if (!thisDirLogStr.equals(other.thisDirLogStr)) return false ;
        return true ;
    }
}
