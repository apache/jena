/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package fm2.jenautil;

import java.io.InputStream ;
import java.util.StringTokenizer ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.JenaRuntime ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.vocabulary.LocationMappingVocab ;

import fm2.atlas.LocationMapper ;

/** Code for using the general facilities of the location mapper/ filemanager subsystem
 *  and set up for Jena usage. e.g. find a location mapper with RDf description. 
 */
public class JenaIOEnvironment
{
    // TODO Singletons
    
    static LocationMapper theMapper = null ;
    /** Get the global LocationMapper */
    public static LocationMapper getLocationMapper()
    {
        if ( theMapper == null )
        {
            theMapper = new LocationMapper() ;
            if ( getGlobalConfigPath() != null )
                JenaIOEnvironment.createLocationMapper(getGlobalConfigPath()) ;
        }
        return theMapper ;
    }
    
    static Logger log = LoggerFactory.getLogger(JenaIOEnvironment.class)  ;
    
    /** The default path for searching for the location mapper */
    public static final String DEFAULT_PATH =
        "file:location-mapping.rdf;file:location-mapping.n3;file:location-mapping.ttl;"+
        "file:etc/location-mapping.rdf;file:etc/location-mapping.n3;"+
        "file:etc/location-mapping.ttl" ;
    public static final String GlobalMapperSystemProperty1 = "http://jena.hpl.hp.com/2004/08/LocationMap" ;
    public static final String GlobalMapperSystemProperty2 = "LocationMap" ;

    static String s_globalMapperPath = null ; 



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

    /** Get the global LocationMapper */
    public static LocationMapper get()
    {
        if ( theMapper == null )
        {
            theMapper = new LocationMapper() ;
            if ( getGlobalConfigPath() != null )
                JenaIOEnvironment.createLocationMapper(getGlobalConfigPath()) ;
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
            LocationMapper lMap2 = JenaIOEnvironment.createLocationMapper(getGlobalConfigPath()) ;
            lMap.copyFrom(lMap2) ;
        }
        return lMap ;
    }
  

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /** Create a LocationMapper based on Model */
    public static LocationMapper processConfig(Model m)
    {
        LocationMapper locMap = new LocationMapper() ; 
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
                    locMap.addAltEntry(name, altName) ;
                    log.debug("Mapping: "+name+" => "+altName) ;
                } catch (JenaException ex)
                {
                    log.warn("Error processing name mapping: "+ex.getMessage()) ;
                    throw ex ;
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
                    locMap.addAltPrefix(prefix, altPrefix) ;
                    log.debug("Prefix mapping: "+prefix+" => "+altPrefix) ;
                } catch (JenaException ex)
                {
                    log.warn("Error processing prefix mapping: "+ex.getMessage()) ;
                    throw ex ;
                }
            }
        }
        return locMap ;
    }

    /** Search a path (which is delimited by ";" because ":" is used in URIs)
     *  to find a description of a LocationMapper, then create and return a
     *  LocationMapper based on the description.
     */
    public static LocationMapper createLocationMapper(String configPath)
    {
        LocationMapper locMap = new LocationMapper() ;
        if ( configPath == null || configPath.length() == 0 )
        {
            log.warn("Null configuration") ;
            return null ;
        }
        
        // Make a file manager to look for the location mapping file
        FileManager fm = new FileManager() ;
        fm.addLocatorFile() ;
        fm.addLocatorClassLoader(fm.getClass().getClassLoader()) ;
        
        try {
            String uriConfig = null ; 
            InputStream in = null ;
            
            StringTokenizer pathElems = new StringTokenizer( configPath, FileManager.PATH_DELIMITER );
            while (pathElems.hasMoreTokens()) {
                String uri = pathElems.nextToken();
                if ( uri == null || uri.length() == 0 )
                    break ;
                
                in = fm.openNoMap(uri) ;
                if ( in != null )
                {
                    uriConfig = uri ;
                    break ;
                }
            }
    
            if ( in == null )
            {
                log.debug("Failed to find configuration: "+configPath) ;
                return null ;
            }
            // TODO
            String syntax = FileUtils.guessLang(uriConfig) ;
            Model model = ModelFactory.createDefaultModel() ;
            model.read(in, uriConfig, syntax) ;
            processConfig(model) ;
        } catch (JenaException ex)
        {
            LoggerFactory.getLogger(LocationMapper.class).warn("Error in configuration file: "+ex.getMessage()) ;
        }
        return locMap ;
    }

}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
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