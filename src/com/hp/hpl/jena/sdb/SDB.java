/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.engine.QueryEngineFactorySDB;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


public class SDB
{
    static boolean initialized = false ;
    static public void init()
    {
        // Called from 
        // + StoreFactory
        // + DatasetStore
        // Commands call AssemblerVocab.init() ;
        if ( initialized ) return ;
        AssemblerVocab.init() ;
        QueryEngineRegistry.get().add(new QueryEngineFactorySDB()) ;
        initialized = true ;
    }
    
    /** RDF namespace prefix */
    private static final String rdfPrefix = RDF.getURI() ;

    /** RDFS namespace prefix */
    private static final String rdfsPrefix = RDFS.getURI() ;

    /** OWL namespace prefix */
    private static final String owlPrefix = OWL.getURI() ;
    
    /** XSD namespace prefix */
    private static final String xsdPrefix = XSDDatatype.XSD+"#" ;
    
    /** The namespace of the XML results format */ 
    private static final String srxPrefix = "http://www.w3.org/2005/sparql-results#" ;
    
 
    
    protected static PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        globalPrefixMap.setNsPrefix("rdf",  rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  xsdPrefix) ;
        globalPrefixMap.setNsPrefix("owl" , owlPrefix) ;
    }
    public static PrefixMapping getGlobalPrefixMapping() { return globalPrefixMap ; }
    
    // ----------------------------------
    
    /** The root package name for SDB */   
    public static final String PATH = "com.hp.hpl.jena.sdb";
   
    /** The product name */   
    public static final String NAME = "@name@";
   
    /** The SDB web site : see also http://jena.sourceforge.net*/   
    public static final String WEBSITE = "@website@";
   
    /** The full name of the current ARQ version */   
    public static final String VERSION = "@version@";
   
    /** The major version number for this release of SDB (ie '2' for SDB 2.0) */
    public static final String MAJOR_VERSION = "@version-major@";
   
    /** The minor version number for this release of SDB (ie '0' for SDB 2.0) */
    public static final String MINOR_VERSION = "@version-minor@";
   
    /** The version status for this release of SDB (eg '-beta1' or the empty string) */
    public static final String VERSION_STATUS = "@version-status@";
   
    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = "@build-time@";
    
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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