/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Internal constants - configuration is in class ARQ
 * 
 * @author Andy Seaborne
 * @version $Id: ARQConstants.java,v 1.5 2007/02/06 18:21:33 andy_seaborne Exp $
 */
public class ARQConstants
{
    /** The prefix of XQuery/Xpath functions and operator */
    public static final String fnPrefix = "http://www.w3.org/2005/xpath-functions#" ;
    
    /** RDF namespace prefix */
    public static final String rdfPrefix = RDF.getURI() ;

    /** RDFS namespace prefix */
    public static final String rdfsPrefix = RDFS.getURI() ;

    /** OWL namespace prefix */
    public static final String owlPrefix = OWL.getURI() ;
    
    /** XSD namespace prefix */
    public static final String xsdPrefix = XSDDatatype.XSD+"#" ;
    
    /** The namespace of the XML results format */ 
    public static final String srxPrefix = "http://www.w3.org/2005/sparql-results#" ;
    
    /** XML namespace */
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace" ; 
    
    /** XML Schema namespace */
    public static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema#" ;
    
    public static final String javaClassURIScheme = "java:" ;
    
    /** The ARQ function library URI space */
    public static final String ARQFunctionLibraryURI = "http://jena.hpl.hp.com/ARQ/function#" ;
    
    /** The ARQ property function library URI space */
    public static final String ARQPropertyFunctionLibraryURI = "http://jena.hpl.hp.com/ARQ/pfunction#" ;
    
    /** The ARQ function library */
    public static final String ARQFunctionLibrary = javaClassURIScheme+"com.hp.hpl.jena.sparql.function.library." ;
    
    /** The ARQ property function library */
    public static final String ARQPropertyFunctionLibrary = javaClassURIScheme+"com.hp.hpl.jena.sparql.pfunction.library." ;

    //    /** The ARQ extensions library */
//    public static final String ARQExtensionLibrary = javaClassURIScheme+"com.hp.hpl.jena.sparql.extension.library." ;

    /** Common prefixes */
    protected static PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        globalPrefixMap.setNsPrefix("rdf",  rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  xsdPrefix) ;
        globalPrefixMap.setNsPrefix("owl" , owlPrefix) ;
        globalPrefixMap.setNsPrefix("fn" ,  fnPrefix) ; 
        globalPrefixMap.setNsPrefix("jfn",  ARQFunctionLibraryURI) ;
        globalPrefixMap.setNsPrefix("apf",  ARQPropertyFunctionLibraryURI) ;
    }
    public static PrefixMapping getGlobalPrefixMap() { return globalPrefixMap ; }
    
    public static Symbol allocSymbol(String shortName)
    { 
        if ( shortName.startsWith(ARQ.arqNS)) 
            throw new ARQInternalErrorException("Symbol short name begins with the ARQ namespace name: "+shortName) ;
        return new Symbol(ARQ.arqNS+shortName) ;
    }
    
    /** Marker for generated variables for bNodes in query patterns */ 
    public static String anonVarMarker = "=" ;
    
    /** Marker for general temporary variables (not blank node variables or system variables */
    public static final String allocVarMarker = "@" ;

    /** Marker for system symbols */
    public static final String systemVarMarker = "#" ;
    
    /** Context key for the current time of query execution */
    public static final Symbol sysCurrentTime  = new Symbol(systemVarMarker+"now") ;
    
    /** Context key for ARQ version */
    public static final Symbol sysVersionARQ   = new Symbol(systemVarMarker+"version/ARQ") ;
    /** Context key for Jena version */
    public static final Symbol sysVersionJena  = new Symbol(systemVarMarker+"version/Jena") ;

    /** Context key for making all SELECT queries have DISTINCt applied, whether stated ot not */
    public static final Symbol autoDistinct = ARQConstants.allocSymbol("autoDistinct") ;
    
    // Context keys : some here, some in ARQ - sort out
    
    /** Context key for StageBuilder used in BGP compilation */ 
    public static final Symbol stageGenerator =
        ARQConstants.allocSymbol("stageGenerator") ;

    /** Context key to control hiding non-distinuished variables */ 
    public static final Symbol hideNonDistiguishedVariables =
        ARQConstants.allocSymbol("hideNonDistiguishedVariables") ;
    
    /** The property function registry key */
    public static final Symbol registryPropertyFunctions =
        ARQConstants.allocSymbol("registryPropertyFunctions") ;
    
    /** The describe handler registry key */
    public static final Symbol registryDescribeHandlers =
        ARQConstants.allocSymbol("registryDescribeHandlers") ;

    /** The function library registry key */
    public static final Symbol registryFunctions =
        ARQConstants.allocSymbol("registryFunctions") ;
    
    /** The extension library registry key */
    public static final Symbol registryExtensions =
        ARQConstants.allocSymbol("registryExtensions") ;
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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