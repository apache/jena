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

package org.apache.jena.riot.system.stream ;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

import org.apache.jena.atlas.web.TypedInputStream ;
import org.apache.jena.riot.RiotNotFoundException ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Management of stream opening, including redirecting through a location mapper
 * whereby a name (e.g. URL) is redirected to another name (e.g. local file).
 * Includes filename to IRI, handling ".gz" and "-"
 */

public class StreamManager {
    // Need to combine with IO to do the .gz and "-" things.

    private static Logger        log           = LoggerFactory.getLogger(StreamManager.class) ;

    public static boolean        logAllLookups = true ;

    private List<Locator>        handlers      = new ArrayList<>() ;
    private LocationMapper       mapper        = null ;

    private static StreamManager globalStreamManager ;

    public StreamManager() {}

    /** Create a deep copy of this StreamManager */
    @Override
    public StreamManager clone() {
        return clone(this) ;
    }

    private static StreamManager clone(StreamManager other) {
        StreamManager sm = new StreamManager() ;
        sm.handlers.addAll(other.handlers) ;
        sm.mapper = other.mapper == null ? null : other.mapper.clone() ;
        return sm ;
    }

    /**
     * Return a default configuration StreamManager with a {@linkplain LocatorFile},
     * {@linkplain LocatorHTTP}, {@linkplain LocatorFTP} and {@linkplain LocatorClassLoader}
     */
    public static StreamManager makeDefaultStreamManager() {
        StreamManager streamManager = new StreamManager() ;
        streamManager.addLocator(new LocatorFile(null)) ;
        streamManager.addLocator(new LocatorHTTP()) ;
        streamManager.addLocator(new LocatorFTP()) ;
        streamManager.addLocator(new LocatorClassLoader(streamManager.getClass().getClassLoader())) ;
        streamManager.setLocationMapper(JenaIOEnvironment.getLocationMapper()) ;
        return streamManager ;
    }

    public static StreamManager get() {
        return globalStreamManager ;
    }

    public static void setGlobal(StreamManager streamManager) {
        globalStreamManager = streamManager ;
    }
    
    static { setGlobal(makeDefaultStreamManager()) ; }

    /**
     * Open a file using the locators of this FileManager. Returns null if not
     * found.
     */
    public TypedInputStream open(String filenameOrURI) {
        if ( log.isDebugEnabled() )
            log.debug("open(" + filenameOrURI + ")") ;

        String uri = mapURI(filenameOrURI) ;

        if ( log.isDebugEnabled() && !uri.equals(filenameOrURI) )
            log.debug("open: mapped to " + uri) ;

        return openNoMapOrNull(uri) ;
    }

    /** Apply the mapping of a filename or URI */
    public String mapURI(String filenameOrURI) {
        if ( mapper == null )
            return filenameOrURI ;

        String uri = mapper.altMapping(filenameOrURI, null) ;

        if ( uri == null ) {
            if ( StreamManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not mapped: " + filenameOrURI) ;
            uri = filenameOrURI ;
        } else {
            if ( log.isDebugEnabled() )
                log.debug("Mapped: " + filenameOrURI + " => " + uri) ;
        }
        return uri ;
    }

    /**
     * Open a file using the locators of this FileManager but without location
     * mapping. Throws RiotNotFoundException if not found.
     */
    public TypedInputStream openNoMap(String filenameOrURI) {
        TypedInputStream in = openNoMapOrNull(filenameOrURI) ;
        if ( in == null )
            throw new RiotNotFoundException(filenameOrURI) ;
        return in ;
    }

    /**
     * Open a file using the locators of this FileManager without location
     * mapping. Return null if not found
     */

    public TypedInputStream openNoMapOrNull(String filenameOrURI) {
        for (Locator loc : handlers) {
            TypedInputStream in = loc.open(filenameOrURI) ;
            if ( in != null ) {
                if ( log.isDebugEnabled() )
                    log.debug("Found: " + filenameOrURI + " (" + loc.getName() + ")") ;
                return in ;
            }
        }
        return null ;
    }

    /** Set the location mapping */
    public void setLocationMapper(LocationMapper _mapper) {
        mapper = _mapper ;
    }

    /** Get the location mapping */
    public LocationMapper getLocationMapper() {
        return mapper ;
    }

    /** Return an immutable list of all the handlers */
    public List<Locator> locators() {
        return Collections.unmodifiableList(handlers) ;
    }

    /** Remove a locator */
    public void remove(Locator loc) {
        handlers.remove(loc) ;
    }

    /** Remove all locators */
    public void clearLocators() {
        handlers.clear() ;
    }

    /** Add a locator to the end of the locators list */
    public void addLocator(Locator loc) {
        handlers.add(loc) ;
    }
}
