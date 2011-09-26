/**
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

package fm2.atlas;

import java.io.File ;
import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.InputStream ;
import java.security.AccessControlException ;

import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.lib.Lib ;
import org.openjena.atlas.web.TypedStream ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.util.FileUtils ;

/** Location files in the filing system.
 *  A FileLocator can have a "current directory" - this is separate from any
 *  location mapping (see @link{LocationMapping}) as it applies only to files.
 */

public class LocatorFile implements Locator
{
    static Logger log = LoggerFactory.getLogger(LocatorFile.class) ;
    private String altDir = null ;
    private String altDirLogStr = "" ;

    public LocatorFile() { this(null) ; }
    
    public LocatorFile(String dir)
    {
        if ( dir != null )
        {
            if ( dir.endsWith("/") || dir.endsWith(java.io.File.separator) )
                dir = dir.substring(0,dir.length()-1) ;
            altDirLogStr = " ["+dir+"]" ;
        }
        altDir = dir ;
    }

    // Two LocatorFile are the same if they would look up names to the same files.
    
    @Override
    public boolean equals( Object other )
    {
        return
            other instanceof LocatorFile
            && Lib.equal(altDir, ((LocatorFile) other).altDir );
    }
    
    @Override
    public int hashCode()
    {
        if ( altDir == null ) return 57 ;
        return altDir.hashCode();
    }
    
    /** To a File, after processing the filename for file: or relative filename */
    public File toFile(String filenameIRI)
    {
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
        // AltDir.
        // "/" is a path separator on Windows.
        if ( altDir != null && ! fn.startsWith("/") && ! fn.startsWith(File.pathSeparator) )
            fn = altDir+"/"+fn ;
        return new File(fn) ;
    }
    
    public boolean exists(String fileIRI)
    {
        File f = toFile(fileIRI) ;
        
        if ( f == null )
            return false ;
        
        return f.exists() ;
    }
    
    /** Opne anything that looks a bit like a file name */ 
    public TypedStream open(String filenameIRI)
    {
        File f = toFile(filenameIRI) ;

        try {
            if ( f == null || !f.exists() )
            {
                if ( StreamManager.logAllLookups && log.isTraceEnabled())
                    log.trace("Not found: "+filenameIRI+altDirLogStr) ;
                return null ;
            }
        } catch (AccessControlException e) {
            log.warn("Security problem testing for file", e);
            return null;
        }
        
        try {
            InputStream in = new FileInputStream(f) ;

            if ( StreamManager.logAllLookups && log.isTraceEnabled() )
                log.trace("Found: "+filenameIRI+altDirLogStr) ;
            
            // Guess content from extension.
            Lang lang = Lang.guess(filenameIRI) ;
            String contentType = WebContent.mapLangToContentType(lang) ;
            String charset = WebContent.getCharsetForContentType(contentType) ;
            return new TypedStream(in, contentType, charset) ;
        } catch (IOException ioEx)
        {
            // Includes FileNotFoundException
            // We already tested whether the file exists or not.
            log.warn("File unreadable (but exists): "+f.getPath()+" Exception: "+ioEx.getMessage()) ;
            return null ;
        }
    }
    
    public String getName()
    {
        String tmp = "LocatorFile" ;
        if ( altDir != null )
            tmp = tmp+"("+altDir+")" ;
        return tmp ;
    }
}
