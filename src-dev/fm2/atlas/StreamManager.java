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

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.lib.IRILib ;
import org.openjena.atlas.web.TypedStream ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Operations to open streams, indirecting via a LocationMapper.
 * Inckludes filename to IRI, handling ".gz" and "-" 
 *  
 *  */ 
public class StreamManager
{
    // Need to combine with IO to do the .gz and "-" things.
    
    private static Logger log = LoggerFactory.getLogger(StreamManager.class) ;
    
    public static boolean logAllLookups = true ; 
    
    protected List<Locator> handlers = new ArrayList<Locator>() ;
    protected LocationMapper mapper = null ;
    
    /** Open a file using the locators of this FileManager */
    public InputStream open(String filenameOrURI)
    {
        if ( log.isDebugEnabled())
            log.debug("open("+filenameOrURI+")") ;
        
        String uri = mapURI(filenameOrURI) ;
        
        if ( log.isDebugEnabled() && ! uri.equals(filenameOrURI) )
            log.debug("open: mapped to "+uri) ;
        
        return openNoMap(uri) ;
    }

    /** Apply the mapping of a filename or URI */
    public String mapURI(String filenameOrURI)
    {
        // XXX NO
        // allow any string (e.g. relative filename - made absolute in LocatorFile)
        
        String uriFilename = IRILib.filenameToIRI(filenameOrURI) ;
        
        
        if ( mapper == null )
            return uriFilename ; 
        
        String uri = mapper.altMapping(uriFilename, null) ;
    
        if ( uri == null )
        {
            if ( StreamManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not mapped: "+filenameOrURI) ;
            uri = uriFilename ;
        }
        else
        {
            if ( log.isDebugEnabled() )
                log.debug("Mapped: "+filenameOrURI+" => "+uri) ;
        }
        return uri ;
    }

    /** Open a file using the locators of this FileManager 
     *  but without location mapping */ 
    public InputStream openNoMap(String filenameOrURI)
    {
        TypedStream in = openNoMapOrNull(filenameOrURI) ;
        if ( in == null )
            return null ;
//        if ( in == null )
//            throw new NotFoundException(filenameOrURI) ;
        return in.getInput() ;
    }

    /** Open a file using the locators of this FileManager 
     *  but without location mapping.
     *  Return null if not found
     */ 
    
    public TypedStream openNoMapOrNull(String filenameOrURI)
    {
        String uriFilename = IRILib.filenameToIRI(filenameOrURI) ;
        for (Locator loc : handlers)
        {
            TypedStream in = loc.open(uriFilename) ;
            if ( in != null )
            {
                if ( log.isDebugEnabled() )
                    log.debug("Found: "+uriFilename+" ("+loc.getName()+")") ;
                return in ;
            }
        }
        return null; 
    }

}
