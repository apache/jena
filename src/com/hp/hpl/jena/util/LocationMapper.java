/*
 * (c) Copyright 2002, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.InputStream;
import java.util.*;

import com.hp.hpl.jena.JenaRuntime;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.JenaException;

import org.apache.commons.logging.*;

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
 *  
 * @author Andy Seaborne
 * @version $Id: LocationMapper.java,v 1.3 2004-12-07 18:40:25 andy_seaborne Exp $
 */

public class LocationMapper
{
    static Log log = LogFactory.getLog(LocationMapper.class)  ;
    /** The default path for searching for the location mapper */
    public static final String DEFAULT_PATH =
        "file:location-mapping.rdf;file:location-mapping.n3;" +
        "file:etc/location-mapping.rdf;file:etc/location-mapping.n3;" ;
    public static final String GlobalMapperSystemProperty1 = "http://jena.hpl.hp.com/2004/08/LocationMap" ;
    public static final String GlobalMapperSystemProperty2 = "LocationMap" ;
    
    static String s_globalMapperPath = null ; 
        
    Map altLocations = new HashMap() ;
    Map altPrefixes = new HashMap() ;
    
    static LocationMapper theMapper = null ;
    
    /** Get the global LocationMapper */
    public static LocationMapper get()
    {
        if ( theMapper == null )
        {
            theMapper = new LocationMapper() ;
            if ( getGlobalConfigPath() != null )
            theMapper.init(getGlobalConfigPath(), false) ;
        }
        return theMapper ;
    }

    /** Create a LocationMApper with no mapping yet */
    public LocationMapper() { }
    
    /** Create a LocationMapper from a config file */
    public LocationMapper(String config)
    {
        init(config, true) ;
    }
    
    private void init(String configPath, boolean configMustExist)
    {
        if ( configPath == null )
        {
            log.warn("Null configuration") ;
            return ;
        }
        
        // Make a file manager to look for the location mapping file
        FileManager fm = new FileManager() ;
        fm.addLocatorFile() ;
        fm.addLocatorSystemClassLoader() ;
        
        try {
            String uriConfig = null ; 
            InputStream in = null ;
            
            StringTokenizer pathElems = new StringTokenizer( configPath, FileManager.PATH_DELIMITER );
            while (pathElems.hasMoreTokens()) {
                String uri = pathElems.nextToken();
                in = fm.openNoMap(uri) ;
                if ( in != null )
                {
                    uriConfig = uri ;
                    break ;
                }
            }

            if ( in == null )
            {
                if ( ! configMustExist )
                    log.debug("Failed to find configuration: "+configPath) ;
                return ;
            }
            String syntax = FileUtils.guessLang(uriConfig) ;
            Model model = ModelFactory.createDefaultModel() ;
            model.read(in, null, syntax) ;
            processConfig(model) ;
        } catch (JenaException ex)
        {
            LogFactory.getLog(LocationMapper.class).warn("Error in configuration file: "+ex.getMessage()) ;
        }
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
            return (String)altLocations.get(uri) ;
        String newStart = null ;
        String oldStart = null ;
        for ( Iterator iter = altPrefixes.keySet().iterator() ; iter.hasNext() ;)
        {
            String prefix = (String)iter.next() ;
            if ( uri.startsWith(prefix) )
            {
                String s = (String)altPrefixes.get(prefix) ;
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
    
    public void removeAltEntry(String uri)
    {
        altLocations.remove(uri) ;
    }

    public void removeAltPrefix(String uriPrefix) 
    {
        altPrefixes.remove(uriPrefix) ;
    }
    public String getAltEntry(String uri, String alt)
    {
        return (String)altLocations.get(uri) ;
    }

    public String getAltPrefix(String uriPrefix, String altPrefix) 
    {
        return (String)altPrefixes.get(uriPrefix) ;
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
    
    
    private void processConfig(Model m)
    {
        StmtIterator mappings =
            m.listStatements(null, LocationMappingVocab.mapping, (RDFNode)null) ;

        for (; mappings.hasNext();)
        {
            Statement s = mappings.nextStatement() ;
            Resource mapping =  s.getResource() ;
            
            if ( mapping.hasProperty(LocationMappingVocab.name) )
            {
                try 
                {
                    String name = mapping.getRequiredProperty(LocationMappingVocab.name)
                                        .getString() ;
                    String altName = mapping.getRequiredProperty(LocationMappingVocab.altName)
                                        .getString() ;
                    addAltEntry(name, altName) ;
                    log.debug("Mapping: "+name+" => "+altName) ;
                } catch (JenaException ex)
                {
                    log.warn("Error processing name mapping: "+ex.getMessage()) ;
                    return ;
                }
                
            }
            
            if ( mapping.hasProperty(LocationMappingVocab.prefix) )
            {
                try 
                {
                    String prefix = mapping.getRequiredProperty(LocationMappingVocab.prefix)
                                        .getString() ;
                    String altPrefix = mapping.getRequiredProperty(LocationMappingVocab.altPrefix)
                                        .getString() ;
                    addAltPrefix(prefix, altPrefix) ;
                    log.debug("Prefix mapping: "+prefix+" => "+altPrefix) ;
                } catch (JenaException ex)
                {
                    log.warn("Error processing prefix mapping: "+ex.getMessage()) ;
                    return ;
                }
            }
        }
    }
    
    
    // This code has a list of mappings  
//    private void processConfigList(Model m)
//    {
//        StmtIterator mappings =
//            m.listStatements(null, LocationMappingVocab.mapping, (RDFNode)null) ;
//
//        for (; mappings.hasNext();)
//        {
//            Statement s = mappings.nextStatement() ;
//            Resource listItem =  s.getResource() ;
//            
//            for (; !listItem.equals(RDF.nil);)
//            {
//                Resource r = listItem.getRequiredProperty(RDF.first).getResource();
//                if ( r.hasProperty(LocationMappingVocab.name) )
//                {
//                    String name = r.getRequiredProperty(LocationMappingVocab.name).getString() ;
//                    String altName = r.getRequiredProperty(LocationMappingVocab.altName).getString() ;
//                    addAltEntry(name, altName) ;
//                }
//                if ( r.hasProperty(LocationMappingVocab.prefix) )
//                {
//                    String prefix = r.getRequiredProperty(LocationMappingVocab.prefix).getString() ;
//                    String altPrefix = r.getRequiredProperty(LocationMappingVocab.altPrefix).getString() ;
//                    addAltEntry(prefix, altPrefix) ;
//                }
//                listItem = listItem.getRequiredProperty(RDF.rest).getResource();
//            }
//        }
//    }
}

/*
 *  (c) Copyright 2002 Hewlett-Packard Development Company, LP
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
