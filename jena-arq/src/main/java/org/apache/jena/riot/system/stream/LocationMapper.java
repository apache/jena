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

package org.apache.jena.riot.system.stream;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab ;

/** 
 * Alternative locations for URIs.  Maintains two maps:
 * single item alternatives and alternative prefixes.
 * To suggest an alternative location, first check the single items,
 * then check the prefixes.
 *    
 * A LocationMapper can be configured by an RDF file.  The default for this
 * is "etc/location-mapping.n3".
 * 
 * There is a default LocationMapper which is used by the global @link{FileManager}.
 */

public class LocationMapper
{
    static Logger log = LoggerFactory.getLogger(LocationMapper.class)  ;
    Map<String, String> altLocations = new HashMap<>() ;
    Map<String, String> altPrefixes = new HashMap<>() ;
    
    /** Create a LocationMapper with no mapping yet */
    public LocationMapper() { }
    
    /** Deep copy of location and prefix maps */
    @Override
    public LocationMapper clone() {
        return clone(this) ;
    }

    private static LocationMapper clone(LocationMapper other) {
        LocationMapper mapper = new LocationMapper() ;
        mapper.altLocations.putAll(other.altLocations) ;
        mapper.altPrefixes.putAll(other.altPrefixes) ;
        return mapper ;
    }

    public void copyFrom(LocationMapper lmap2) {
        this.altLocations.putAll(lmap2.altLocations) ;
        this.altPrefixes.putAll(lmap2.altPrefixes) ;
    }

    public String altMapping(String uri) {
        return altMapping(uri, uri) ;
    }

    /**
     * Apply mappings: first try for an exact alternative location, then try to
     * remap by prefix, finally, try the special case of filenames in a specific
     * base directory.
     * 
     * @param uri
     * @param otherwise
     * @return The alternative location choosen
     */
    public String altMapping(String uri, String otherwise) {
        if ( altLocations.containsKey(uri) )
            return altLocations.get(uri) ;
        String newStart = null ;
        String oldStart = null ;
        for ( String prefix : altPrefixes.keySet() )
        {
            if ( uri.startsWith( prefix ) )
            {
                String s = altPrefixes.get( prefix );
                if ( newStart == null || newStart.length() < s.length() )
                {
                    oldStart = prefix;
                    newStart = s;
                }
            }
        }

        if ( newStart != null )
            return newStart + uri.substring(oldStart.length()) ;

        return otherwise ;
    }

    public void addAltEntry(String uri, String alt) {
        altLocations.put(uri, alt) ;
    }

    public void addAltPrefix(String uriPrefix, String altPrefix) {
        altPrefixes.put(uriPrefix, altPrefix) ;
    }

    /** Iterate over all the entries registered */
    public Iterator<String> listAltEntries() {
        return altLocations.keySet().iterator() ;
    }

    /** Iterate over all the prefixes registered */
    public Iterator<String> listAltPrefixes() {
        return altPrefixes.keySet().iterator() ;
    }

    public void removeAltEntry(String uri) {
        altLocations.remove(uri) ;
    }

    public void removeAltPrefix(String uriPrefix) {
        altPrefixes.remove(uriPrefix) ;
    }

    public String getAltEntry(String uri) {
        return altLocations.get(uri) ;
    }

    public String getAltPrefix(String uriPrefix) {
        return altPrefixes.get(uriPrefix) ;
    }

    @Override
    public int hashCode() {
        int x = 0 ;
        x = x ^ altLocations.hashCode() ;
        x = x ^ altPrefixes.hashCode() ;
        return x ;
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof LocationMapper) )
            return false ;
        LocationMapper other = (LocationMapper)obj ;

        if ( !this.altLocations.equals(other.altLocations) )
            return false ;

        if ( !this.altPrefixes.equals(other.altPrefixes) )
            return false ;
        return true ;
    }

    @Override
    public String toString() {
        String s = "" ;
        for ( String k : altLocations.keySet() )
        {
            String v = altLocations.get( k );
            s = s + "(Loc:" + k + "=>" + v + ") ";
        }

        for ( String k : altPrefixes.keySet() )
        {
            String v = altPrefixes.get( k );
            s = s + "(Prefix:" + k + "=>" + v + ") ";
        }
        return s ;
    }

    public Model toModel() {
        Model m = ModelFactory.createDefaultModel() ;
        m.setNsPrefix("lmap", "http://jena.hpl.hp.com/2004/08/location-mapping#") ;
        toModel(m) ;
        return m ;
    }

    public void toModel(Model model) {
        for ( String s1 : altLocations.keySet() )
        {
            Resource r = model.createResource();
            Resource e = model.createResource();
            model.add( r, LocationMappingVocab.mapping, e );

            String k = s1;
            String v = altLocations.get( k );
            model.add( e, LocationMappingVocab.name, k );
            model.add( e, LocationMappingVocab.altName, v );
        }

        for ( String s : altPrefixes.keySet() )
        {
            Resource r = model.createResource();
            Resource e = model.createResource();
            model.add( r, LocationMappingVocab.mapping, e );
            String k = s;
            String v = altPrefixes.get( k );
            model.add( e, LocationMappingVocab.prefix, k );
            model.add( e, LocationMappingVocab.altPrefix, v );
        }
    }
}
