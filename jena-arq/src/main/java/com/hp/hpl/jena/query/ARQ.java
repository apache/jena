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

package com.hp.hpl.jena.query;

import org.apache.jena.riot.RIOT ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.SystemARQ ;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformOrderByDistinctApplication ;
import com.hp.hpl.jena.sparql.engine.http.Service ;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder ;
import com.hp.hpl.jena.sparql.lib.Metadata ;
import com.hp.hpl.jena.sparql.mgt.ARQMgt ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.mgt.Explain.InfoLevel ;
import com.hp.hpl.jena.sparql.mgt.SystemInfo ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.MappingRegistry ;
import com.hp.hpl.jena.sparql.util.Symbol ;

/** ARQ - miscellaneous settings */

public class ARQ
{
    /** Name of the execution logger */
    public static final String logExecName = "com.hp.hpl.jena.arq.exec" ;
    
    /** Name of the information logger */
    public static final String logInfoName = "com.hp.hpl.jena.arq.info" ;
    
    /** Name of the logger for remote HTTP requests */
    public static final String logHttpRequestName = "com.hp.hpl.jena.arq.service" ;
    
    private static final Logger logExec = LoggerFactory.getLogger(logExecName) ;
    private static final Logger logInfo = LoggerFactory.getLogger(logInfoName) ;
    private static final Logger logHttpRequest = LoggerFactory.getLogger(logHttpRequestName) ;

    /** The execution logger */
    public static Logger getExecLogger() { return logExec ; }
    
    /** The information logger */
    public static Logger getInfoLogger() { return logInfo ; }
    
    /** The HTTP Request logger */
    public static Logger getHttpRequestLogger() { return logHttpRequest ; }

    /** Symbol to enable logging of execution.  
     * Must also set log4j, or other logging system,
     * for logger "com.hp.hpl.jena.sparql.exec"
     * e.g. log4j.properties -- log4j.logger.com.hp.hpl.jena.sparql.exec=INFO
     * See the <a href="http://jena.apache.org/documentation/query/logging.html">ARQ Logging Documentation</a>.
     */
    public static final Symbol symLogExec           = ARQConstants.allocSymbol("logExec") ;
    
    /** Get the currently global execution logging setting */  
    public static Explain.InfoLevel getExecutionLogging() { return (Explain.InfoLevel)ARQ.getContext().get(ARQ.symLogExec) ; }
    
    /** Set execution logging - logging is to logger "com.hp.hpl.jena.arq.exec" at level INFO.
     *  An appropriate logging configuration is also required.
     */
    public static void setExecutionLogging(Explain.InfoLevel infoLevel)
    {
        if ( InfoLevel.NONE.equals(infoLevel) )
        {
            ARQ.getContext().unset(ARQ.symLogExec) ;
            return ;
        }
        
        ARQ.getContext().set(ARQ.symLogExec, infoLevel) ;
//        if ( ! getExecLogger().isInfoEnabled() )
//            getExecLogger().warn("Attempt to enable execution logging but the logger '"+logExecName+"' is not logging at level INFO") ;
    }

    /** IRI for ARQ */  
    public static final String arqIRI = "http://jena.hpl.hp.com/#arq" ;

    /** Root of ARQ-defined parameter names */  
    public static final String arqParamNS = "http://jena.hpl.hp.com/ARQ#" ;
    
    /** Prefix for ARQ-defined parameter names */  
    public static final String arqSymbolPrefix = "arq" ;
    
    /** Stick exactly to the spec.
     */
    public static final Symbol strictSPARQL =
        ARQConstants.allocSymbol("strictSPARQL") ;
    
    /** Controls bNode labels as &lt;_:...&gt; or not -
     * that is a pseudo URIs.
     * This does not affect [] or _:a bNodes as variables in queries. 
     */

    public static final Symbol constantBNodeLabels =
        ARQConstants.allocSymbol("constantBNodeLabels") ;
    
    /** Enable built-in property functions - also called "magic properties".
     * These are properties in triple patterns that need
     * calculation, not matching.  See ARQ documentation for more details.
     * rdfs:member and http://jena.hpl.hp.com/ARQ/list#member are provided.
     */

    public static final Symbol enablePropertyFunctions =
        ARQConstants.allocSymbol("enablePropertyFunctions") ;

    /** Enable logging of execution timing. 
     */

    public static final Symbol enableExecutionTimeLogging =
        ARQConstants.allocSymbol("enableExecutionTimeLogging") ;

    /** If true, XML result sets written will contain the graph bNode label
     *  See also inputGraphBNodeLabels
     */
    
    public static final Symbol outputGraphBNodeLabels =  
        ARQConstants.allocSymbol("outputGraphBNodeLabels") ;

    /** If true, XML result sets will use the bNode label in the result set itself.
     *  See also outputGraphBNodeLabels
     */
    
    public static final Symbol inputGraphBNodeLabels =  
        ARQConstants.allocSymbol("inputGraphBNodeLabels") ;

    /** Turn on processing of blank node labels in queries */  
    public static void enableBlankNodeResultLabels() { enableBlankNodeResultLabels(true) ; }
    
    /** Turn on/off processing of blank node labels in queries */  
    public static void enableBlankNodeResultLabels(boolean val)
    { 
        Boolean b = val;
        globalContext.set(inputGraphBNodeLabels, b) ;
        globalContext.set(outputGraphBNodeLabels, b) ;
    }


    /**
     * Set timeout.  The value of this symbol gives thevalue of the timeout in milliseconds
     * <ul>
     * <li>A Number; the long value is used</li>
     * <li>A string, e.g. "1000", parsed as a number</li>
     * <li>A string, as two numbers separated by a comma, e.g. "500,10000" parsed as two numbers</li>
     * </ul>
     * @see QueryExecution#setTimeout(long)
     * @see QueryExecution#setTimeout(long,long)
     */
    public static final Symbol queryTimeout = ARQConstants.allocSymbol("queryTimeout") ;
    
    // This can't be a context constant because NodeValues don't look in the context.
//    /**
//     * Context symbol controlling Roman Numerals in Filters.
//     */
//    public static final Symbol enableRomanNumerals = ARQConstants.allocSymbol("romanNumerals") ;

    /**
     * Context key for StageBuilder used in BGP compilation 
     */
    public static final Symbol stageGenerator = ARQConstants.allocSymbol("stageGenerator") ;

    /**
     * Context key to control hiding non-distinuished variables 
     */
    public static final Symbol hideNonDistiguishedVariables = ARQConstants.allocSymbol("hideNonDistiguishedVariables") ;

    /**
     * Use the SAX parser for XML result sets.  The default is to use StAX for
     * full streaming of XML results.  The SAX parser takes a copy of the result set
     * before giving the ResultSet to the calling application.
     */
    public static final Symbol useSAX = ARQConstants.allocSymbol("useSAX") ;
    
    /** 
     * Indicate whether duplicate select and groupby variables are allowed. 
     * If false, duplicates are silently supressed; it's not an error.  
     */
    public static final boolean allowDuplicateSelectColumns = false ;

    /**
     * Determine which regular expression system to use.
     * The value of this context entry should be a string or symbol
     * of one of the following:
     *   javaRegex :   use java.util.regex (support features outside the strict SPARQL regex language)
     *   xercesRegex : use the internal XPath regex engine (more compliant)  
     */
    
    public static final Symbol regexImpl =  ARQConstants.allocSymbol("regexImpl") ;
    
        
    /** Symbol to name java.util.regex regular expression engine */ 
    public static final Symbol javaRegex =  ARQConstants.allocSymbol("javaRegex") ;
    /** Symbol to name the Xerces-J regular expression engine */ 
    public static final Symbol xercesRegex =  ARQConstants.allocSymbol("xercesRegex") ;

    /** 
     * Use this Symbol to allow passing additional query parameters to a 
     * SERVICE <IRI> call.
     * Parameters need to be grouped by SERVICE <IRI>,  
     * a Map<String, Map<String,List<String>>> is assumed.
     * The key of the first map is the SERVICE IRI, the value is a Map 
     * which maps the name of a query string parameters to its values.
     * 
     * @see com.hp.hpl.jena.sparql.engine.http.Service
     */
    public static final Symbol serviceParams = ARQConstants.allocSymbol("serviceParams") ;
    
    /**
     * Control whether SERVICE processing is allowed.
     * If the context of the query exexcution contains this, 
     * and it's set to "false", then SERVICE is not allowed.
     */
    
    public static final Symbol serviceAllowed = Service.serviceAllowed ;
    
    /**
     * A Long value that specifies the number of bindings (or triples for CONSTRUCT queries) to be stored in memory by sort
     * operations or hash tables before switching to temporary disk files.  The value defaults to -1, which will always
     * keep the bindings in memory and never write to temporary files.  The amount of memory used will vary based on
     * the size of the bindings.  If you are retrieving large literal strings, then you may need to lower the value. 
     * <p/>
     * Note that for a complex query, several sort or hash operations might be running in parallel; each one will be
     * allowed to retain as many bindings in memory as this value specifies before it starts putting data in temporary
     * files.  Also, several running sessions could be doing such operations concurrently.  Therefore, the total number
     * of bindings held in memory could be many times this value; it is necessary to keep this fact in mind when
     * choosing the value.
     * <p/>
     * Operations currently affected by this symbol: <br>
     * ORDER BY, SPARQL Update, CONSTRUCT (optionally)
     * <p/>
     * TODO: Give a reasonable suggested value here.  10,000?
     * <p/>
     * @see <a href="https://issues.apache.org/jira/browse/JENA-119">JENA-119</a>
     */
    // Some possible additions to the list:
    // Sort: DISTINCT, merge joins<br>
    // Hash table: GROUP BY, MINUS, SERVICE, VALUES, and hash joins <br>
    public static final Symbol spillToDiskThreshold = ARQConstants.allocSymbol("spillToDiskThreshold") ;
    
    // Optimizer controls.
    
    /** 
     *  Globally switch the default optimizer on and off : 
     *  Note that storage subsystems may also be applying 
     *  separately controlled optimizations.
     */
    
    public static void enableOptimizer(boolean state) 
    {
        enableOptimizer(ARQ.getContext(), state) ;
    }
    
    /** 
     *  Switch the default optimizer on and off for a specific Context. 
     *  Note that storage subsystems may also be applying 
     *  separately controlled optimizations.
     */
    public static void enableOptimizer(Context context, boolean state) 
    {
        context.set(ARQ.optimization, state) ;
    }
    
    /** 
     *  Context key controlling whether the main query engine applies the
     *  default optimization transformations.
     */  
    public static final Symbol optimization = ARQConstants.allocSymbol("optimization") ;
    
    /** 
     *  Context key controlling whether the main query engine flattens simple paths
     *  (e.g. <tt>?x :p/:q ?z => ?x :p ?.0 . ?.0 ?q ?z</tt>)   
     *  <p>Default is "true"
     */  
    public static final Symbol optPathFlatten = ARQConstants.allocSymbol("optPathFlatten") ;
    
    /** 
     *  Context key controlling whether the main query engine moves filters to the "best" place.
     *  Default is "true" - filter placement is done.
     */  
    public static final Symbol optFilterPlacement = ARQConstants.allocSymbol("optFilterPlacement") ;

    /** 
     *  Context key controlling whether to do filter placement within BGP and quad blocks.
     *  Modies the effect of optFilterPlacement. 
     *  Default is "true" - filter placement is pushed into BGPs.
     */  
    public static final Symbol optFilterPlacementBGP = ARQConstants.allocSymbol("optFilterPlacementBGP") ;
    
    /** 
     *  Context key controlling whether the main query engine moves filters to the "best" place using 
     *  the more limited and conservative strategy which does not place as many filters
     *  Must be explicitly set "true" to operate.
     *  Filter placement, via {@linkplain #optFilterPlacement} must also be active (which it is by default).
     * @see #optFilterPlacement
     */ 
    public static final Symbol optFilterPlacementConservative = ARQConstants.allocSymbol("optFilterPlacementConservative") ;

    /** 
     *  Context key controlling whether an ORDER BY-LIMIT query is done avoiding total sort using an heap.
     *  Default is "true" - total sort if avoided by default when ORDER BY is used with LIMIT.
     */  
    public static final Symbol optTopNSorting = ARQConstants.allocSymbol("optTopNSorting") ;
    
    /** 
     *  Context key controlling whether a DISTINCT-ORDER BY query is done by replacing the distinct with a reduced.
     *  Default is "true" - the reduced operator does not need to keep a data structure with all previously seen bindings.
     */  
    public static final Symbol optDistinctToReduced = ARQConstants.allocSymbol("optDistinctToReduced") ;
    
    /**
     * Context key controlling whether a DISTINCT-ORDER BY query is done by applying the ORDER BY after the DISTINCT
     * when default SPARQL semantics usually mean ORDER BY applies before DISTINCT.  This optimization applies only
     * in a subset of cases unlike the more general {@link #optDistinctToReduced} optimization.
     * <p>
     * See {@link TransformOrderByDistinctApplication} for more discussion on exactly when this may apply
     * </p>
     */
    public static final Symbol optOrderByDistinctApplication = ARQConstants.allocSymbol("optOrderByDistinctApplication");

    /** 
     *  Context key controlling whether the standard optimizer applies
     *  optimizations to equalities in FILTERs.
     *  This optimization is conservative - it does not take place if
     *  there is a potential risk of changing query semantics. 
     */  
    public static final Symbol optFilterEquality = ARQConstants.allocSymbol("optFilterEquality") ;
    
    /**
     * Context key controlling whether the standard optimizer applies 
     * optimizations to inequalities in FILTERs
     * This optimization is conservative - it does not take place if
     * there is a potential risk of changing query semantics
     */
    public static final Symbol optFilterInequality = ARQConstants.allocSymbol("optFilterInequality") ;
    
    /**
     * Context key controlling whether the standard optimizer applies optimizations to implicit joins in FILTERs.
     * This optimization is conservative - it does not take place if there is a potential risk of changing query semantics.
     */
    public static final Symbol optFilterImplicitJoin = ARQConstants.allocSymbol("optFilterImplicitJoin");
    
    /**
     * Context key controlling whether the standard optimizer applies optimizations to implicit left joins.
     * This optimization is conservative - it does not take place if there is a potential risk of changing query semantics.
     */
    public static final Symbol optImplicitLeftJoin = ARQConstants.allocSymbol("optImplicitLeftJoin");

    /** 
     *  Context key for a declaration that xsd:strings and simple literals are
     *  different in the storage.  They are the same value in a memory store.
     *  When in doubt, xsd:strings are assumed to be the same value as simple literals   
     */  
    public static final Symbol optTermStrings = ARQConstants.allocSymbol("optTermStrings") ;
    
    /**
     * Context key controlling whether the standard optimizer applies constant folding to expressions
     */
    public static final Symbol optExprConstantFolding = ARQConstants.allocSymbol("optExprConstantFolding");

    /** 
     *  Context key controlling whether the standard optimizer applies
     *  optimizations to conjunctions (&&) in filters.
     */  
    public static final Symbol optFilterConjunction = ARQConstants.allocSymbol("optFilterConjunction") ;

    /** 
     *  Context key controlling whether the standard optimizer applies
     *  optimizations to IN and NOT IN.
     */  
    public static final Symbol optFilterExpandOneOf = ARQConstants.allocSymbol("optFilterExpandOneOf") ;

    /** 
     *  Context key controlling whether the standard optimizer applies
     *  optimizations to disjunctions (||) in filters.
     */  
    public static final Symbol optFilterDisjunction = ARQConstants.allocSymbol("optFilterDisjunction") ;
    
    /**
     * Context key controlling whether the standard optimizer applies table empty promotion
     */
    public static final Symbol optPromoteTableEmpty = ARQConstants.allocSymbol("optPromoteTableEmpty") ;
    
    /**
     * Context key controlling whether the standard optimizer applies optimizations to the evaluation
     * of joins to favour index joins wherever possible
     */
    public static final Symbol optIndexJoinStrategy = ARQConstants.allocSymbol("optIndexJoinStrategy");
    
    /**
     * Context key controlling whether the standard optimizater applies optimizations to joined BGPs to
     * merge them into single BGPs.
     * By default, this transformation is applied.
     */
    public static final Symbol optMergeBGPs = ARQConstants.allocSymbol("optMergeBGPs");
    
    /**
     * Context key controlling whether the standard optimizater applies the optimization
     * to combine stacks of (extend) into one compound operation.  Ditto (assign). 
     * By default, this transformation is applied.
     */
    public static final Symbol optMergeExtends = ARQConstants.allocSymbol("optMergeExtends");

    /** 
     *  Context key controlling whether the main query engine processes property functions.
     *  
     */  
    public static final Symbol propertyFunctions = ARQConstants.allocSymbol("propertyFunctions") ;
    
    /**
     * Expression evaluation without extension types (e.g. xsd:date, language tags)
     */
    public static final Symbol extensionValueTypes = ARQConstants.allocSymbol("extensionValueTypesExpr") ;

    /**
     * Generate the ToList operation in the algebra (as ARQ is stream based, ToList is a non-op).
     * Default is not to do so.  Strict mode will also enable this.
     */
    public static final Symbol generateToList = ARQConstants.allocSymbol("generateToList") ;

    /** Set strict mode, including expression evaluation */
    public static void setStrictMode() { setStrictMode(ARQ.getContext()) ; }
    
    /** Set strict mode for a given Context.
     *  
     *  Does not influence expression evaluation because NodeValues
     *  are controlled globally, not per context.
     */
    public static void setStrictMode(Context context)
    {
        SystemARQ.StrictDateTimeFO      = true ;
        SystemARQ.ValueExtensions       = false ;
        SystemARQ.SameValueAsString     = false ;
        SystemARQ.EnableRomanNumerals   = false ;
        
        context.set(optimization,                   false) ;
        context.set(hideNonDistiguishedVariables,   true) ;
        context.set(strictSPARQL,                   true) ;
        context.set(enablePropertyFunctions,        false) ;
        
        context.set(extensionValueTypes,            false) ;
        context.set(constantBNodeLabels,            false) ;
        context.set(generateToList,                 true) ;
        context.set(regexImpl,                      xercesRegex) ;
        
        //context.set(filterPlacement,            false) ;
    }

    public static boolean isStrictMode()       { return ARQ.getContext().isTrue(strictSPARQL) ; }
    
    /** Set normal mode, including expression evaluation */
    public static void setNormalMode() {
        SystemARQ.StrictDateTimeFO      = false ;
        SystemARQ.ValueExtensions       = true ;
        SystemARQ.SameValueAsString     = true ;
        SystemARQ.EnableRomanNumerals   = false ;
        setNormalMode(ARQ.getContext()) ; 
    }
        
    /** Explicitly set the values for normal operation.
     *  Does not influence expression evaluation.
     */
    public static void setNormalMode(Context context)
    {
        context.set(optimization,                   true) ;
        context.set(hideNonDistiguishedVariables,   false) ;
        context.set(strictSPARQL,                   false) ;
        context.set(enablePropertyFunctions,        true) ;
        
        context.set(extensionValueTypes,            true) ;
        context.set(constantBNodeLabels,            true) ;
        context.set(generateToList,                 false) ;
        context.set(regexImpl,                      javaRegex) ;
    }
    
    // ----------------------------------
    
    /** The root package name for ARQ */   
    public static final String PATH         = "org.apache.jena.arq";
   
    static private String metadataLocation  = "org/apache/jena/arq/arq-properties.xml" ;

    static private Metadata metadata        = new Metadata(metadataLocation) ;
    
    /** The product name */   
    public static final String NAME         = "ARQ";
   
    /** The full name of the current ARQ version */   
    public static final String VERSION      = metadata.get(PATH+".version", "unknown") ;
   
    /** The date and time at which this release was built */   
    public static final String BUILD_DATE   = metadata.get(PATH+".build.datetime", "unset") ;
    
    // A correct way to manage without synchonized using the double checked locking pattern.
    //   http://en.wikipedia.org/wiki/Double-checked_locking
    //   http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html 
    private static volatile boolean initialized = false ;
    private static final Object initLock = new Object() ;

    private static Context globalContext = null ;

    /** Ensure things have started - applications do not need call this.
     * The method is public so any part of ARQ can call it.
     * Note the final static initializer call 
     */
    
    public static void init()
    { 
        if ( initialized )
            return ;
        synchronized(initLock)
        {
            if ( initialized )
                return ;
            initialized = true ;
            globalContext = defaultSettings() ;
            
            RIOT.init() ;
            StageBuilder.init() ;
            ARQMgt.init() ;         // After context and after PATH/NAME/VERSION/BUILD_DATE are set
            MappingRegistry.addPrefixMapping(ARQ.arqSymbolPrefix, ARQ.arqParamNS) ;
            
            // This is the pattern for any subsystem to register. 
            SystemInfo sysInfo = new SystemInfo(ARQ.arqIRI, ARQ.PATH, ARQ.VERSION, ARQ.BUILD_DATE) ;
            SystemARQ.registerSubSystem(sysInfo) ;

            // Register RIOT details here, not earlier, to avoid
            // initialization loops with RIOT.init() called directly.
            RIOT.register() ;
        }
    }
    
    // Force a call
    static { init() ; }
    
    /* Side effects */
    private static Context defaultSettings()
    {
        SystemARQ.StrictDateTimeFO      = false ;
        SystemARQ.ValueExtensions       = true ;
        SystemARQ.SameValueAsString     = true ;
        SystemARQ.EnableRomanNumerals   = false ; 

        Context context = new Context() ;
        context.unset(optimization) ;
        //context.set(hideNonDistiguishedVariables, true) ;
        context.set(strictSPARQL,                  false) ; 
        context.set(constantBNodeLabels,           true) ;
        context.set(enablePropertyFunctions,       true) ;
        context.set(regexImpl,                     javaRegex) ;

        return context ; 
    }

    public static Context getContext()
    { 
        //ARQ.init() ;
        return globalContext ;
    }
    
    // Convenience call-throughs
    public static void set(Symbol symbol, boolean value)  { getContext().set(symbol, value) ; }
    public static void setTrue(Symbol symbol)             { getContext().setTrue(symbol) ; }
    public static void setFalse(Symbol symbol)            { getContext().setFalse(symbol) ; }
    public static void unset(Symbol symbol)               { getContext().unset(symbol) ; }
    public static boolean isTrue(Symbol symbol)           { return getContext().isTrue(symbol) ; }
    public static boolean isFalse(Symbol symbol)          { return getContext().isFalse(symbol) ; }
    public static boolean isTrueOrUndef(Symbol symbol)    { return getContext().isTrueOrUndef(symbol) ; }
    public static boolean isFalseOrUndef(Symbol symbol)   { return getContext().isFalseOrUndef(symbol) ; }

}
