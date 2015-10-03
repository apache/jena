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

package org.apache.jena.sdb;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sdb.assembler.AssemblerVocab ;
import org.apache.jena.sdb.core.SDBConstants ;
import org.apache.jena.sdb.engine.QueryEngineSDB ;
import org.apache.jena.sdb.modify.UpdateEngineSDB ;
import org.apache.jena.sdb.util.DerbyUtils ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.lib.Metadata ;
import org.apache.jena.sparql.mgt.SystemInfo ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.MappingRegistry ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.system.JenaSystem ;
import org.apache.jena.vocabulary.OWL ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.RDFS ;

public class SDB
{
    // Must be first, especially the initLock setup.
    private static volatile boolean initialized = false ;
    private static Object initLock = new Object() ;

    /** IRI for SDB */  
    public static final String sdbIRI = "http://jena.hpl.hp.com/#sdb" ;
    
    /** SDB namespace */
    public final static String namespace = "http://jena.hpl.hp.com/2007/sdb#" ;
    public final static String symbolPrefix = "sdb" ;
    public final static String symbolSpace = "http://jena.hpl.hp.com/SDB/symbol#" ;

    
    // ----------------------------------
    public static final Symbol useQuadRewrite           = SDBConstants.allocSymbol("useQuadRewrite") ;
    public static final Symbol unionDefaultGraph        = SDBConstants.allocSymbol("unionDefaultGraph") ;

    /** Control whether GraphSDB streams results to find - must ensure all Jena iterators are closed if this is set true */

    public static final Symbol streamGraphAPI           = SDBConstants.allocSymbol("streamGraphAPI") ;
    
    /** Control/attempt  JDBC streaming - mosty databases allow only one outstanding streaming request */  
    public static final Symbol jdbcStream               = SDBConstants.allocSymbol("jdbcStream") ;
    public static final Symbol jdbcFetchSize            = SDBConstants.allocSymbol("jdbcFetchSize") ;
    // See also SDBConstants.jdbcFetchSizeOff
    
    public static final Symbol annotateGeneratedSQL     = SDBConstants.allocSymbol("annotateGeneratedSQL") ;
    // ----------------------------------
    
    // Global context is the ARQ context.
    public static Context getContext() { return ARQ.getContext() ; }
    
    static { 
        JenaSystem.init(); 
    }
    
//    /** Used by Jena assemblers for registration */ 
//    public static void whenRequiredByAssembler( AssemblerGroup g )
//    {
//        AssemblerUtils.init() ;         // ARQ 
//        AssemblerVocab.register(g) ;    // SDB
//    }
    
    public static void init() {
        if ( initialized ) 
            return ;
        synchronized(initLock) {
            if ( initialized ) {
                if ( JenaSystem.DEBUG_INIT )
                    System.err.println("SDB.init - skip") ;
                return ;
            }
            initialized = true ;
            if ( JenaSystem.DEBUG_INIT )
                System.err.println("SDB.init - start") ;

            // Better not to break up BGPs too much.
            ARQ.getContext().set(ARQ.optFilterPlacement, false) ;
            MappingRegistry.addPrefixMapping(SDB.symbolPrefix, SDB.symbolSpace) ;

            // Default is 1000 4Kpages.
            DerbyUtils.setDerbyPageCacheSize(10000) ;

            // Wire in the SDB query engine
            QueryEngineSDB.register() ;
            // Wire in the SDB update engine
            UpdateEngineSDB.register() ;

            SDB.getContext().setIfUndef(useQuadRewrite,        false) ;
            SDB.getContext().setIfUndef(streamGraphAPI,        false) ;
            SDB.getContext().setIfUndef(jdbcStream,            true) ;
            //SDB.getContext().setIfUndef(jdbcFetchSize,         ???) ;
            SDB.getContext().setIfUndef(annotateGeneratedSQL,  true) ;
            //SDB.getContext().setIfUndef(unionDefaultGraph,     false) ;
            AssemblerVocab.init(); 
            if ( JenaSystem.DEBUG_INIT )
                System.err.println("SDB.init - finish") ;
        }
    }
    
    /** RDF namespace prefix */
    private static final String rdfPrefix = RDF.getURI() ;

    /** RDFS namespace prefix */
    private static final String rdfsPrefix = RDFS.getURI() ;

    /** OWL namespace prefix */
    private static final String owlPrefix = OWL.getURI() ;
    
    /** XSD namespace prefix */
    private static final String xsdPrefix = XSDDatatype.XSD+"#" ;
    
//    /** The namespace of the XML results format */ 
//    private static final String srxPrefix = "http://www.w3.org/2005/sparql-results#" ;
    
    protected static PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        globalPrefixMap.setNsPrefix("rdf",  rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  xsdPrefix) ;
        globalPrefixMap.setNsPrefix("owl" , owlPrefix) ;
        globalPrefixMap.setNsPrefix("sdb" , namespace) ;
    }
    public static PrefixMapping getGlobalPrefixMapping() { return globalPrefixMap ; }
    
    
    public static Symbol allocSymbol(String shortName)
    { 
        if ( shortName.startsWith(ARQ.arqParamNS) )
            throw new ARQInternalErrorException("Symbol short name begins with the ARQ namespace name: "+shortName) ;
        return Symbol.create(ARQ.arqParamNS+shortName) ;
    }

    // ----------------------------------
    
    static private String metadataLocation = "org/apacge/jena/sdb/sdb-properties.xml" ;
    static private Metadata metadata = new Metadata(metadataLocation) ;
    
    /** The root package name for SDB */   
    public static final String PATH = "org.apache.jena.sdb";
   
    /** The product name */   
    public static final String NAME = "SDB";
   
    /** The full name of the current ARQ version */   
    public static final String VERSION = metadata.get(PATH+".version", "unknown") ;
   
    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = metadata.get(PATH+".build.datetime", "unset") ;
    
 // Final initialization (in case any statics in this file are important). 
    static {
        initlization2() ;
    }

    private static void initlization2()
    { 
        SystemInfo systemInfo = new SystemInfo(SDB.sdbIRI, SDB.PATH, SDB.VERSION, SDB.BUILD_DATE) ;
        SystemARQ.registerSubSystem(systemInfo) ;
    }
}
