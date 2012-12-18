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

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import org.apache.jena.atlas.web.TypedInputStream ;
import org.openjena.riot.RiotNotFoundException ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** Management of stream opening, including redirecting through a location mapper
 * whereby a name (e.g. URL) is redirected to another name (e.g. local file).
 * Includes filename to IRI, handling ".gz" and "-"
 */

public class StreamManager
{
    // Need to combine with IO to do the .gz and "-" things.
    
    private static Logger log = LoggerFactory.getLogger(StreamManager.class) ;
    
    public static boolean logAllLookups = true ; 
    
    private List<Locator> handlers = new ArrayList<Locator>() ;
    private LocationMapper2 mapper = null ;
    
    private static StreamManager globalStreamManager ;
    
    /** Return a default configuration StreamManager 
     *  with a {@link LocatorFile2}, 
     *  {@link LocatorURL2},
     *  and {@link LocatorClassLoader}
     */
    public static StreamManager makeDefaultStreamManager()
    {
        StreamManager streamManager = new StreamManager() ;
        streamManager.addLocator(new LocatorFile2(null)) ;
        streamManager.addLocator(new LocatorURL2()) ;
        streamManager.addLocator(new LocatorClassLoader(streamManager.getClass().getClassLoader())) ;
        return streamManager ;
    }
    
    public static StreamManager get()                           { return globalStreamManager ; }
    public static void setGlobal(StreamManager streamManager)   { globalStreamManager = streamManager; }
    static { setGlobal(makeDefaultStreamManager()) ; }
    
    /** Open a file using the locators of this FileManager.
     *  Returns null if not found.
     */
    public TypedInputStream open(String filenameOrURI)
    {
        if ( log.isDebugEnabled())
            log.debug("open("+filenameOrURI+")") ;
        
        String uri = mapURI(filenameOrURI) ;
        
        if ( log.isDebugEnabled() && ! uri.equals(filenameOrURI) )
            log.debug("open: mapped to "+uri) ;
        
        return openNoMapOrNull(uri) ;
    }

    /** Apply the mapping of a filename or URI */
    public String mapURI(String filenameOrURI)
    {
        if ( mapper == null )
            return filenameOrURI ; 
        
        String uri = mapper.altMapping(filenameOrURI, null) ;
    
        if ( uri == null )
        {
            if ( StreamManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not mapped: "+filenameOrURI) ;
            uri = filenameOrURI ;
        }
        else
        {
            if ( log.isDebugEnabled() )
                log.debug("Mapped: "+filenameOrURI+" => "+uri) ;
        }
        return uri ;
    }

    /** Open a file using the locators of this FileManager 
     *  but without location mapping.  Throws RiotNotFoundException if not found.*/ 
    public TypedInputStream openNoMap(String filenameOrURI)
    {
        TypedInputStream in = openNoMapOrNull(filenameOrURI) ;
        if ( in == null )
            throw new RiotNotFoundException(filenameOrURI) ;
        return in ;
    }

    /** Open a file using the locators of this FileManager 
     *  without location mapping. Return null if not found
     */ 
    
    public TypedInputStream openNoMapOrNull(String filenameOrURI)
    {
        for (Locator loc : handlers)
        {
            TypedInputStream in = loc.open(filenameOrURI) ;
            if ( in != null )
            {
                if ( log.isDebugEnabled() )
                    log.debug("Found: "+filenameOrURI+" ("+loc.getName()+")") ;
                return in ;
            }
        }
        return null; 
    }
    

    /** Set the location mapping */
    public void setLocationMapper(LocationMapper2 _mapper) { mapper = _mapper ; }
    
    /** Get the location mapping */
    public LocationMapper2 getLocationMapper() { return mapper ; }
    
    /** Return an immutable list of all the handlers */
    public List<Locator> locators() { return Collections.unmodifiableList(handlers) ; }

    /** Remove a locator */ 
    public void remove(Locator loc) { handlers.remove(loc) ; }

    /** Remove all locators */ 
    public void clearLocators()
    {
        handlers.clear() ;
    }

    /** Add a locator to the end of the locators list */ 
    public void addLocator(Locator loc)
    {
        handlers.add(loc) ;
    }
}
