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

package riotcmd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.AccessControlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.LocatorFile;

class LocatorOupputFile {
    static Logger log = LoggerFactory.getLogger(LocatorOupputFile.class) ;
    private String thisDir = null ;
    private String thisDirLogStr = "" ;
    
    public LocatorOupputFile(String dir)
    {
        if ( dir != null )
        {
            if ( dir.endsWith("/") || dir.endsWith(java.io.File.separator) )
                dir = dir.substring(0,dir.length()-1) ;
            thisDirLogStr = " ["+dir+"]" ;
        }
        thisDir = dir ;
    }

    LocatorOupputFile()
    {
        this(null) ;
    }
    
    @Override
    public boolean equals( Object other )
    {
        return
            other instanceof LocatorFile
            && equals( thisDir, ((LocatorOupputFile) other).thisDir );
    }
    
    private boolean equals( String a, String b )
    {
        return a == null ? b == null : a.equals(  b  );
    }

    @Override
    public int hashCode()
    {
        if ( thisDir == null )
            return 157 ;
        return thisDir.hashCode();
    }
    
    private File toFile(String filenameOrURI)
    {
        String fn = FileUtils.toFilename(filenameOrURI) ;
        if ( fn == null )
            return null ;
        
        if ( thisDir != null && ! fn.startsWith("/") && ! fn.startsWith(FileManager.filePathSeparator) )
            fn = thisDir+java.io.File.separator+fn ;
                     
        return new File(fn) ;
    }
    
    
    public boolean exists(String filenameOrURI)
    {
        File f = toFile(filenameOrURI) ;
        
        if ( f == null )
            return false ;
        
        return f.exists() ;
    }
    

    public OutputStream open(String filenameOrURI)
    {
        // Worry about %20.
        // toFile calls FileUtils.toFilename(filenameOrURI) ;
        File f = toFile(filenameOrURI) ;

        try {
            if ( f == null )
            {
                if ( log.isTraceEnabled())
                    log.trace("Not found: "+filenameOrURI+thisDirLogStr) ;
                return null ;
            }
        } catch (AccessControlException e) {
            log.warn("Security problem testing for file", e);
            return null;
        }
        
        try {
            OutputStream out = new FileOutputStream(f) ;

            if ( log.isTraceEnabled() )
                log.trace("Found: "+filenameOrURI+thisDirLogStr) ;
                
            
            // Create base -- Java 1.4-isms
            //base = f.toURI().toURL().toExternalForm() ;
            //base = base.replaceFirst("^file:/([^/])", "file:///$1") ;
            return out ;
        } catch (IOException ioEx)
        {
            // Includes FileNotFoundException
            // We already tested whether the file exists or not.
            // log.warn("File unreadable (but exists): "+f.getPath()+" Exception: "+ioEx.getMessage()) ;
            return null ;
        }
    }
    
    public String getDir()  { return thisDir ; }
    

    public String getName()
    {
        String tmp = "LocatorFile" ;
        if ( thisDir != null )
            tmp = tmp+"("+thisDir+")" ;
        return tmp ;
    }
}
