/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * [See end of file]
 */

package fm2;

import java.util.*;

import org.openjena.atlas.AtlasException ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab;
import com.hp.hpl.jena.rdf.model.*;

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
 * 
 * @see FileManager
 */

public class LocationMapper
{
    static Logger log = LoggerFactory.getLogger(LocationMapper.class)  ;
    /** The default path for searching for the location mapper */
    public static final String DEFAULT_PATH =
        "file:location-mapping.rdf;file:location-mapping.n3;file:location-mapping.ttl;"+
        "file:etc/location-mapping.rdf;file:etc/location-mapping.n3;"+
        "file:etc/location-mapping.ttl" ;
    public static final String GlobalMapperSystemProperty1 = "http://jena.hpl.hp.com/2004/08/LocationMap" ;
    public static final String GlobalMapperSystemProperty2 = "LocationMap" ;
    
    static String s_globalMapperPath = null ; 
        
    Map<String, String> altLocations = new HashMap<String, String>() ;
    Map<String, String> altPrefixes = new HashMap<String, String>() ;
    
    static LocationMapper theMapper = null ;
    
    /** Get the global LocationMapper */
    public static LocationMapper get()
    {
        if ( theMapper == null )
        {
            theMapper = new LocationMapper() ;
            if ( getGlobalConfigPath() != null )
                SetupFileManagement.createLocationMapper(getGlobalConfigPath()) ;
        }
        return theMapper ;
    }
    
    /** Set the global lcoation mapper. (as returned by get())
     * If called before any call to get(), then the usual default global location mapper is not created 
     * @param globalLocationMapper
     */
    public static void setGlobalLocationMapper(LocationMapper globalLocationMapper)
    {
        theMapper = globalLocationMapper ;
    }

    /** Make a location mapper from the path settings */ 
    static public LocationMapper makeGlobal()
    {
        LocationMapper lMap = new LocationMapper() ;
        if ( getGlobalConfigPath() != null )
        {
            LocationMapper lMap2 = SetupFileManagement.createLocationMapper(getGlobalConfigPath()) ;
            copyFrom(lMap, lMap2) ;
        }
        return lMap ;
    }
    
    /** Create a LocationMapper with no mapping yet */
    public LocationMapper() { }
    
    /** Create a LocationMapper made like another one
     * This is a deep copy of the location and prefix maps..*/
    public LocationMapper(LocationMapper locMapper)
    {
        altLocations.putAll(locMapper.altLocations) ;
        altPrefixes.putAll(locMapper.altPrefixes) ;
    }
    
    /** Create a LocationMapper from an existing model
     * @see com.hp.hpl.jena.vocabulary.LocationMappingVocab
     */
    public LocationMapper(Model model)
    {
        LocationMapper lm = SetupFileManagement.processConfig(model) ;
        if ( lm == null )
            throw new AtlasException("Model does not provide a location mapping") ;
        copyFrom(this, lm) ;
         
    }
    
    /** Create a LocationMapper from a config file */
    public LocationMapper(String config)
    {
        LocationMapper lm = SetupFileManagement.createLocationMapper(config) ;
        if ( lm == null )
            throw new AtlasException("Config does not provide a location mapping") ;
        copyFrom(this, lm) ;
    }
    
    private static void copyFrom(LocationMapper lmap1, LocationMapper lmap2)
    {
        lmap1.altLocations.putAll(lmap2.altLocations) ;
        lmap1.altPrefixes.putAll(lmap2.altPrefixes) ;
    }
    
    public String altMapping(String uri)
    {
        return altMapping(uri, uri) ;
    }

    /** Apply mappings: first try for an exact alternative location, then
     *  try to remap by prefix, finally, try the special case of filenames
     *  in a specific base directory. 
     * @param uri
     * @param otherwise
     * @return The alternative location choosen
     */
    public String altMapping(String uri, String otherwise)
    {
        if ( altLocations.containsKey(uri)) 
            return altLocations.get(uri) ;
        String newStart = null ;
        String oldStart = null ;
        for ( Iterator<String> iter = altPrefixes.keySet().iterator() ; iter.hasNext() ;)
        {
            String prefix = iter.next() ;
            if ( uri.startsWith(prefix) )
            {
                String s = altPrefixes.get(prefix) ;
                if ( newStart == null || newStart.length() < s.length() )
                {
                    oldStart = prefix ;
                    newStart = s ;
                }
            }
        }
        
        if ( newStart != null )
            return newStart+uri.substring(oldStart.length()) ;
        
        return otherwise ;
    }
    

    public void addAltEntry(String uri, String alt)
    {
        altLocations.put(uri, alt) ;
    }

    public void addAltPrefix(String uriPrefix, String altPrefix) 
    {
        altPrefixes.put(uriPrefix, altPrefix) ;
    }

    /** Iterate over all the entries registered */ 
    public Iterator<String> listAltEntries()  { return altLocations.keySet().iterator() ; } 
    /** Iterate over all the prefixes registered */ 
    public Iterator<String> listAltPrefixes() { return altPrefixes.keySet().iterator() ; } 
    
    public void removeAltEntry(String uri)
    {
        altLocations.remove(uri) ;
    }

    public void removeAltPrefix(String uriPrefix) 
    {
        altPrefixes.remove(uriPrefix) ;
    }
    public String getAltEntry(String uri)
    {
        return altLocations.get(uri) ;
    }

    public String getAltPrefix(String uriPrefix) 
    {
        return altPrefixes.get(uriPrefix) ;
    }
    
    
    static private String getGlobalConfigPath()
    {
        if ( s_globalMapperPath == null )
            s_globalMapperPath = JenaRuntime.getSystemProperty(GlobalMapperSystemProperty1,null) ;
        if ( s_globalMapperPath == null )
            s_globalMapperPath = JenaRuntime.getSystemProperty(GlobalMapperSystemProperty2,null) ;
        if ( s_globalMapperPath == null )
            s_globalMapperPath = DEFAULT_PATH ;
        return s_globalMapperPath ;
    }
    
    @Override
    public int hashCode()
    {
        int x = 0 ;
        x = x ^ altLocations.hashCode() ;
        x = x ^ altPrefixes.hashCode() ;
        return x ;
        
//        // Err - this is hashMap hash. 
//        
//        for ( Iterator<String> iter = altLocations.keySet().iterator() ; iter.hasNext() ; )
//        {
//            String k = iter.next() ;
//            String v = altLocations.get(k) ;
//            x = x ^ k.hashCode() ^ v.hashCode() ;
//        }
//        for ( Iterator<String> iter = altPrefixes.keySet().iterator() ; iter.hasNext() ; )
//        {
//            String k = iter.next() ;
//            String v = altPrefixes.get(k) ;
//            x = x ^ k.hashCode() ^ v.hashCode() ;
//        }
//        return x ;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if ( ! ( obj instanceof LocationMapper ) )
            return false ;
        LocationMapper other = (LocationMapper)obj ;
        
        if ( ! this.altLocations.equals(other.altLocations) )
            return false ;
        
        if ( ! this.altPrefixes.equals(other.altPrefixes) )
            return false ;
        return true ; 

//        if ( altLocations.size() != other.altLocations.size() )
//            return false ;
//        
//        if ( altPrefixes.size() != other.altPrefixes.size() )
//            return false ;
//        
//        for ( Iterator<String> iter = altLocations.keySet().iterator() ; iter.hasNext() ; )
//        {
//            String k = iter.next() ;
//            String v = altLocations.get(k) ;
//            if ( ! other.altLocations.get(k).equals(v) )
//                return false ;
//        }
//        for ( Iterator<String> iter = altPrefixes.keySet().iterator() ; iter.hasNext() ; )
//        {
//            String k = iter.next() ;
//            String v = altPrefixes.get(k) ;
//            if ( ! other.altPrefixes.get(k).equals(v) )
//                return false ;
//        }
//        return true ;
    }
    
    @Override
    public String toString()
    {
        String s = "" ;
        for ( Iterator<String> iter = altLocations.keySet().iterator() ; iter.hasNext() ; )
        {
            String k = iter.next() ;
            String v = altLocations.get(k) ;
            s = s+"(Loc:"+k+"=>"+v+") " ;
        }

        for ( Iterator<String> iter = altPrefixes.keySet().iterator() ; iter.hasNext() ; )
        {
            String k = iter.next() ;
            String v = altPrefixes.get(k) ;
            s = s+"(Prefix:"+k+"=>"+v+") " ;
        }
        return s ;
    }
    
    public Model toModel()
    {
        Model m = ModelFactory.createDefaultModel() ;
        m.setNsPrefix("lmap", "http://jena.hpl.hp.com/2004/08/location-mapping#") ;
        toModel(m) ;
        return m ;
    }
    
    public void toModel(Model model)
    {
        
        for ( Iterator<String> iter = altLocations.keySet().iterator() ; iter.hasNext() ; )
        {
            Resource r = model.createResource() ;
            Resource e = model.createResource() ;
            model.add(r, LocationMappingVocab.mapping, e) ;
            
            String k = iter.next() ;
            String v = altLocations.get(k) ;
            model.add(e, LocationMappingVocab.name, k) ;
            model.add(e, LocationMappingVocab.altName, v) ;
        }

        for ( Iterator<String> iter = altPrefixes.keySet().iterator() ; iter.hasNext() ; )
        {
            Resource r = model.createResource() ;
            Resource e = model.createResource() ;
            model.add(r, LocationMappingVocab.mapping, e) ;
            String k = iter.next() ;
            String v = altPrefixes.get(k) ;
            model.add(e, LocationMappingVocab.prefix, k) ;
            model.add(e, LocationMappingVocab.altPrefix, v) ;
        }
    }
}

/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
