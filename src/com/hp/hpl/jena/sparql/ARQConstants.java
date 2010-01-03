/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.hp.hpl.jena.sparql.util.Symbol;

import com.hp.hpl.jena.query.ARQ;

/**
 * Internal constants - configuration is in class ARQ
 * 
 * @author Andy Seaborne
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
    public static final String ARQPropertyFunctionLibraryURI = "http://jena.hpl.hp.com/ARQ/property#" ;
    
    /** The ARQ procedure library URI space */
    public static final String ARQProcedureLibraryURI = "http://jena.hpl.hp.com/ARQ/procedure#" ;
    
    /** The ARQ function library */
    public static final String ARQFunctionLibrary = javaClassURIScheme+"com.hp.hpl.jena.sparql.function.library." ;
    
    /** The ARQ property function library */
    public static final String ARQPropertyFunctionLibrary = javaClassURIScheme+"com.hp.hpl.jena.sparql.pfunction.library." ;

    /** The ARQ property function library */
    public static final String ARQProcedureLibrary = javaClassURIScheme+"com.hp.hpl.jena.sparql.procedure.library." ;

    /** Common prefixes */
    protected static final PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        //globalPrefixMap.setNsPrefixes(PrefixMapping.Standard) ;
        globalPrefixMap.setNsPrefix("rdf",  rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  xsdPrefix) ;
        globalPrefixMap.setNsPrefix("owl" , owlPrefix) ;
        globalPrefixMap.setNsPrefix("fn" ,  fnPrefix) ; 
        globalPrefixMap.setNsPrefix("afn",  ARQFunctionLibraryURI) ;
        globalPrefixMap.setNsPrefix("apf",  ARQPropertyFunctionLibraryURI) ;
    }
    public static PrefixMapping getGlobalPrefixMap() { return globalPrefixMap ; }
    
    public static Symbol allocSymbol(String shortName)
    { 
        if ( shortName.startsWith(ARQ.arqSymbolPrefix)) 
            throw new ARQInternalErrorException("Symbol short name begins with the ARQ namespace prefix: "+shortName) ;
        if ( shortName.startsWith("http:")) 
            throw new ARQInternalErrorException("Symbol short name begins with http: "+shortName) ;
        return allocSymbol(ARQ.arqNS, shortName) ;
    }
    
    public static Symbol allocSymbol(String base, String shortName)
    {
        return Symbol.create(base+shortName) ;
    }

    /* Variable names and allocated variables.
     * NB Must agree with the variable parsing rules in SSE 
     * Allocated variables use names that are not legal in SPARQL.
     * Examples include the "?" variable initial character.
     * 
     * We need to allocate so clashes never occur within scopes.
     * Distinguished (named variables) and non-distinguished (anon variables, bNodes) 
     * 
     * SSE also allows some convenience forms of exactly these string:
     *  
     * See: ParseHandlerPlain.emitVar
     * 
     * Naming:
     *   Named (distinguished) allocated variables start "?."
     *   Non-Distinguished, allocated variables start "??"
     * 
     * Scopes and usages:
     *   Global:        
     *      allocVarMarker          "?.."
     *   VarAlloc.getVarAllocator 
     *   
     *   Query:     Expressions and aggregates 
     *   Parser:    Used in turning blank nodes into variables in query patterns
     *              Via LabelToNodeMap ("??")
     *   Algebra Generator:
     *              PathCompiler ("??P")    : Non-distinguished variables.
     *   SSE 
     *      "?"     short hand for "some variable" using ?0, ?1, ?2 naming (legal SPARQL names)
     *      "??"    short hand for "some new anon variable"
     *      "?."    short hand for "some new named variable"
     *      
     *  See also sysVarAllocNamed and sysVarAllocAnon for symbols to identify in a context. 
     */
    
    /** Marker for generated variables for non-distinguished in query patterns (??a etc) */
    public static final String allocVarAnonMarker = "?" ;
    
    /** Marker for general temporary variables (not blank node variables) */
    public static final String allocVarMarker = "." ;
    
    // Secondary marker for globally allocated variables. 
    private static final String globalVar =     "." ;
    
    // Spare primary marker.
    //private static final String executionVar =  "@" ;
    
    // These strings are without the leading "?"

    // Put each constant here and not in the place the variable allocator created.
    // Alwats 0, 1, 2, 3 after these prefixes. 
    
    public static final String allocGlobalVarMarker     = allocVarMarker+globalVar ;    // VarAlloc
    public static final String allocPathVariables       = allocVarAnonMarker+"P" ;      // PathCompiler
    public static final String allocQueryVariables      = allocVarMarker ;              // Query
    public static final String allocParserAnonVars      = allocVarAnonMarker ;          // LabelToModeMap
    
    // SSE
    public static final String allocSSEUnamedVars       = "_" ;                         // ParseHandlerPlain - SSE token "?" - legal SPARQL
    public static final String allocSSEAnonVars         = allocVarAnonMarker ;          // ParseHandlerPlain - SSE token "??"
    public static final String allocSSENamedVars        = allocVarMarker ;              // ParseHandlerPlain - SSE token "?."
    
    /** Marker for system symbols */
    public static final String systemVarNS = "http://jena.hpl.hp.com/ARQ/system#" ;
    
    /** Context key for the query for the current query execution 
     * (may be null if was not created from a query string )
     * */
    public static final Symbol sysCurrentQuery  = Symbol.create(systemVarNS+"query") ;

    /** Context key for the OpExecutor to be used */
    public static final Symbol sysOpExecutorFactory  = Symbol.create(systemVarNS+"opExecutorFactory") ;

    /** Context key for the optimizer to be used */
    public static final Symbol sysOptimizer  = Symbol.create(systemVarNS+"optimizer") ;
    
    /** Context key for the dataset for the current query execution 
     * May be null if was not created from a query string.
     */
    public static final Symbol sysCurrentDataset  = Symbol.create(systemVarNS+"dataset") ;

    /** Context key for the algebra expression of the query execution after optimization */
    public static final Symbol sysCurrentAlgebra  = Symbol.create(systemVarNS+"algebra") ;

//    /** Context key for the algebra execution engine of the query execution */
//    public static final Symbol sysCurrentOpExec   = Symbol.create(systemVarNS+"opExec") ;

    /** Context key for the current time of query execution */
    public static final Symbol sysCurrentTime  = Symbol.create(systemVarNS+"now") ;
    
    /** Context key for ARQ version */
    public static final Symbol sysVersionARQ   = Symbol.create(systemVarNS+"version/ARQ") ;
    
    /** Context key for Jena version */
    public static final Symbol sysVersionJena  = Symbol.create(systemVarNS+"version/Jena") ;

    /** Context key for the execution-scoped named variable generator */
    public static final Symbol sysVarAllocNamed  = Symbol.create(systemVarNS+"namedVarAlloc") ;
    
    /** Context key for the execution-scoped bNode variable generator */
    public static final Symbol sysVarAllocAnon  = Symbol.create(systemVarNS+"namedVarAnon") ;
    
    /** Context key for making all SELECT queries have DISTINCT applied, whether stated ot not */
    public static final Symbol autoDistinct = ARQConstants.allocSymbol("autoDistinct") ;
    
    // Context keys : some here, some in ARQ - sort out
    
    /** The property function registry key */
    public static final Symbol registryPropertyFunctions =
        ARQConstants.allocSymbol("registryPropertyFunctions") ;
    
    /** The describe handler registry key */
    public static final Symbol registryDescribeHandlers =
        ARQConstants.allocSymbol("registryDescribeHandlers") ;

    /** The function library registry key */
    public static final Symbol registryFunctions =
        ARQConstants.allocSymbol("registryFunctions") ;
    
    /** The function library registry key */
    public static final Symbol registryProcedures =
        ARQConstants.allocSymbol("registryProcedures") ;
    
    /** The extension library registry key */
    public static final Symbol registryExtensions =
        ARQConstants.allocSymbol("registryExtensions") ;
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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