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

package org.apache.jena.sparql;

import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl ;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.util.Symbol ;

/**
 * Internal constants - configuration is in class ARQ
 */
public class ARQConstants
{
    /** The prefix of XQuery/Xpath functions and operator */
    public static final String fnPrefix = "http://www.w3.org/2005/xpath-functions#" ;

    /** The prefix of XQuery/Xpath functions and operator math: */
    public static final String mathPrefix = "http://www.w3.org/2005/xpath-functions/math#" ;

    // Using explicit constants here makes ARQConstants safe to use during initialization.
    // Otherwise it needs JenaSystem.init but the constants may be used during initialization
    // which leads to problems depending in the order of initialization.
    //
    // In
    //   static final x = someFunctionCall();
    // x is null until the class is initialized but class initialization can be cyclic
    // and is not always complete while another class is initializing and using this class.
    // See also SystemARQ.allocSymbol

    /** RDF namespace prefix */
    public static final String rdfPrefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"; // RDF.getURI()

    /** RDFS namespace prefix */
    public static final String rdfsPrefix = "http://www.w3.org/2000/01/rdf-schema#"; //RDFS.getURI() ;

    /** OWL namespace prefix */
    public static final String owlPrefix = "http://www.w3.org/2002/07/owl#"; //OWL.getURI() ;

    /** XSD namespace prefix */
    public static final String xsdPrefix = "http://www.w3.org/2001/XMLSchema#" ; //XSDDatatype.XSD+"#" ;

    /** The prefix of SPARQL functions and operator */
    public static final String fnSparql = "http://www.w3.org/ns/sparql#" ;

    /** The namespace of the XML results format */
    public static final String srxPrefix = "http://www.w3.org/2005/sparql-results#" ;

    /** XML namespace */
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace" ;

    /** XML Schema namespace */
    public static final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema#" ;

    /** The URI prefix that triggers JavaScript functions */
    public static final String JavaScriptURI = "http://jena.apache.org/ARQ/jsFunction#" ;

    /** Function called when JavaScript is initialized. */
    public static final String JavaScriptInitFunction = "arqJSinit";

    /** URI scheme that triggers the loader to load a java class */
    public static final String javaClassURIScheme = "java:" ;

    /** The ARQ function library URI space */
    public static final String ARQFunctionLibraryURI = "http://jena.apache.org/ARQ/function#" ;

    /** The ARQ aggregate function library URI space */
    public static final String ARQAggregateLibraryURI = "http://jena.apache.org/ARQ/function/aggregate#" ;

    /** The ARQ function library URI space - old Jena2 name
     * @deprecated Use #ARQFunctionLibraryURI
     */
    @Deprecated
    public static final String ARQFunctionLibraryURI_Jena2 = "http://jena.hpl.hp.com/ARQ/function#" ;

    /** The ARQ property function library URI space */
    public static final String ARQPropertyFunctionLibraryURI = "http://jena.apache.org/ARQ/property#" ;

    /** The ARQ property function library URI space - old Jena2 name
     * @deprecated Use #ARQFunctionLibraryURI
     */
    @Deprecated
    public static final String ARQPropertyFunctionLibraryURI_Jena2 = "http://jena.hpl.hp.com/ARQ/property#" ;

    /** The ARQ procedure library URI space */
    public static final String ARQProcedureLibraryURI = "http://jena.apache.org/ARQ/procedure#" ;

    /** The ARQ function library */
    public static final String ARQFunctionLibrary = javaClassURIScheme+"org.apache.jena.sparql.function.library." ;

    /** The ARQ property function library */
    public static final String ARQPropertyFunctionLibrary = javaClassURIScheme+"org.apache.jena.sparql.pfunction.library." ;

    /** The ARQ property function library */
    public static final String ARQProcedureLibrary = javaClassURIScheme+"org.apache.jena.sparql.procedure.library." ;

    /** Common prefixes */
    protected static final PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        //globalPrefixMap.setNsPrefixes(PrefixMapping.Standard) ;
        globalPrefixMap.setNsPrefix("rdf",  rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  xsdPrefix) ;
        globalPrefixMap.setNsPrefix("owl" , owlPrefix) ;
        globalPrefixMap.setNsPrefix("fn" ,  fnPrefix) ;
        // Treat op: as fn: (op: has no namespace in XSD F&O).
        globalPrefixMap.setNsPrefix("op" ,  fnPrefix) ;
        globalPrefixMap.setNsPrefix("math" ,  mathPrefix) ;
        globalPrefixMap.setNsPrefix("afn",  ARQFunctionLibraryURI) ;
        globalPrefixMap.setNsPrefix("apf",  ARQPropertyFunctionLibraryURI) ;
    }
    public static PrefixMapping getGlobalPrefixMap() { return globalPrefixMap ; }

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
     *   Quad transform hidden vars: "?*"
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
     *
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

    /** Marker for variables renamed to make variables hidden by scope have globally unique names */
    public static final String allocVarScopeHiding =  "/" ;

    /** Marker for variables renamed to make variables hidden because of quad transformation */
    public static final String allocVarQuad =  "*g" ;

    // Spare primary marker.
    //private static final String executionVar =  "@" ;

    // These strings are without the leading "?"

    // Put each constant here and not in the place the variable allocator created.
    // Always 0, 1, 2, 3 after these prefixes.

    //public static final String allocGlobalVarMarker     = allocVarMarker+globalVar ;    // VarAlloc
    public static final String allocPathVariables       = allocVarAnonMarker+"P" ;      // PathCompiler
    public static final String allocQueryVariables      = allocVarMarker ;              // Query

    /** Marker for RDF-star variables */
    public static final String allocVarTripleTerm      = "~";                           // RX, SolverRX

    public static final String allocParserAnonVars      = allocVarAnonMarker ;          // LabelToModeMap
    // SSE
    public static final String allocSSEUnamedVars       = "_" ;                         // ParseHandlerPlain - SSE token "?" - legal SPARQL
    public static final String allocSSEAnonVars         = allocVarAnonMarker ;          // ParseHandlerPlain - SSE token "??"
    public static final String allocSSENamedVars        = allocVarMarker ;              // ParseHandlerPlain - SSE token "?."

    /** Marker for system symbols */
    public static final String systemVarNS = "http://jena.apache.org/ARQ/system#" ;

    /** Context key for the query for the current query execution
     * (may be null if was not created from a query string )
     *
     * No longer used; use sysCurrentStatement instead.
     */
    @Deprecated
    public static final Symbol sysCurrentQuery          = Symbol.create(systemVarNS+"query") ;

    /** Context key for the current statement execution (Query or UpdateRequest)
     * Prologue is the common base class
     */
    public static final Symbol sysCurrentStatement      = Symbol.create(systemVarNS+"statement") ;


    /** Context key for the OpExecutor to be used */
    public static final Symbol sysOpExecutorFactory     = Symbol.create(systemVarNS+"opExecutorFactory") ;

    /** Context key for the optimizer factory to be used */
    public static final Symbol sysOptimizerFactory      = Symbol.create(systemVarNS+"optimizerFactory") ;

    /** Context key for the optimizer used in this execution */
    public static final Symbol sysOptimizer             = Symbol.create(systemVarNS+"optimizer") ;

    /** Context key for the dataset for the current query execution. */
    public static final Symbol sysCurrentDataset        = Symbol.create(systemVarNS+"dataset") ;

    public static final Symbol sysVarAllocRDFStar       = Symbol.create(systemVarNS+"varAllocRDFStar") ;

    /** Context key for the dataset description (if any).
     *  See the <a href="http://www.w3.org/TR/sparql11-protocol">SPARQL protocol</a>.
     *  <p>
     *  A dataset description specified outside the query should override a dataset description
     *  in query and also the implicit dataset of a service. The order is:
     *  <ol>
     *  <li>Dataset description from the protocol</li>
     *  <li>Dataset description from the query (FROM/FROM NAMED)</li>
     *  <li>Dataset of the service</li>
     *  </ol>
     *  Use in other situations should reflect this design.
     *  The value of this key in a Context must be an object of type DatasetDescription.
     */
    public static final Symbol sysDatasetDescription    = Symbol.create(systemVarNS+"datasetDescription") ;

    /** Context key for the algebra expression of the query execution after optimization */
    public static final Symbol sysCurrentAlgebra        = Symbol.create(systemVarNS+"algebra") ;

//    /** Context key for the algebra execution engine of the query execution */
//    public static final Symbol sysCurrentOpExec   = Symbol.create(systemVarNS+"opExec") ;

    /** Context key for the current time of query execution */
    public static final Symbol sysCurrentTime           = Symbol.create(systemVarNS+"now") ;

    /** Context key for ARQ version */
    public static final Symbol sysVersionARQ            = Symbol.create(systemVarNS+"version/ARQ") ;

    /** Context key for Jena version */
    public static final Symbol sysVersionJena           = Symbol.create(systemVarNS+"version/Jena") ;

    /** Context key for the execution-scoped named variable generator */
    public static final Symbol sysVarAllocNamed         = Symbol.create(systemVarNS+"namedVarAlloc") ;

    /** Context key for the execution-scoped bNode variable generator */
    public static final Symbol sysVarAllocAnon          = Symbol.create(systemVarNS+"namedVarAnon") ;

    /** Graphs forming the default graph (List&lt;String&gt;) (Dynamic dataset) */
    public static final Symbol symDatasetDefaultGraphs  = SystemARQ.allocSymbol("datasetDefaultGraphs") ;

    /** Graphs forming the named graphs (List&lt;String&gt;) (Dynamic dataset) */
    public static final Symbol symDatasetNamedGraphs    = SystemARQ.allocSymbol("datasetNamedGraphs") ;

    /** Context symbol for a supplied {@link Prologue} (used for text out of result sets). */
    public static final Symbol symPrologue              = SystemARQ.allocSymbol("prologue");

    /**
     * Internal use context symbol for an AtomicBoolean to signal that a query has been cancelled.
     * Used by {@code QueryExecutionMain} and {@code QueryIterProcessBinding}.
     * JENA-2141.
     */
    public static final Symbol symCancelQuery           = SystemARQ.allocSymbol("cancelQuery");

    /** Context key for making all SELECT queries have DISTINCT applied, whether stated or not */
    public static final Symbol autoDistinct             = SystemARQ.allocSymbol("autoDistinct") ;

    // Context keys : some here, some in ARQ - sort out

    /** The property function registry key */
    public static final Symbol registryPropertyFunctions =
        SystemARQ.allocSymbol("registryPropertyFunctions") ;

    /** The describe handler registry key */
    public static final Symbol registryDescribeHandlers =
        SystemARQ.allocSymbol("registryDescribeHandlers") ;

    /** The function library registry key */
    public static final Symbol registryFunctions =
        SystemARQ.allocSymbol("registryFunctions") ;

    /** The service executor library registry key */
    public static final Symbol registryServiceExecutors =
        SystemARQ.allocSymbol("registryServiceExecutors") ;

    /** The function library registry key */
    public static final Symbol registryProcedures =
        SystemARQ.allocSymbol("registryProcedures") ;

    /** The extension library registry key */
    public static final Symbol registryExtensions =
        SystemARQ.allocSymbol("registryExtensions") ;
}
