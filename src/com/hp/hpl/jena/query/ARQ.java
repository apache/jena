/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query;

import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;

/** ARQ - miscellaneous settings
 * 
 * @author Andy Seaborne
 * @version $Id: ARQ.java,v 1.46 2007/02/06 18:21:51 andy_seaborne Exp $
 */

public class ARQ
{
    /** IRI for ARQ */  
    public static final String arqIRI = "http://jena.hpl.hp.com/#arq" ;

    /** Root of ARQ-defined parameter names */  
    public static final String arqNS = "http://jena.hpl.hp.com/ARQ#" ;
    
    /** Stick exactly to the spec.
     */
    public static final Symbol strictSPARQL =
        ARQConstants.allocSymbol("strictSPARQL") ;
    
    /** Controls bNode labels as &lt;_:...&gt; or not -
     * that is a pseudo URIs.
     * This does not affect [] or _:a bNodes a variables in queries. 
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
        Boolean b = val ? Boolean.TRUE : Boolean.FALSE ;
        globalContext.set(inputGraphBNodeLabels, b) ;
        globalContext.set(outputGraphBNodeLabels, b) ;
    }

    /**
     * Context symbol controlling Roman Numerals in Filters.
     * This is on a global basis - it can't be set per-query or per-query-execution basis.
     */
    public static final Symbol enableRomanNumerals = ARQConstants.allocSymbol("romanNumerals") ;

    /**
     * Context symbol controlling Roman Numerals in Filters.
     * This is on a global basis - it can't be set per-query or per-query-execution basis.
     */
    public static final Symbol enableXSDdate = ARQConstants.allocSymbol("xsdDate") ;

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
     * Use a simple (and non-scalable) graph implementation that does no
     * value testing.  Needed for DAWG tests where matching is exact
     * and results compared by graph-equivalence.
     */
    public static final Symbol strictGraph =
        ARQConstants.allocSymbol("strictGraph") ;
    
    /** Set global strict mode */
    
    public static void setStrictMode() { setStrictMode(ARQ.getContext()) ; }
    
    /** Set strict mode for a given Context */
    
    public static void setStrictMode(Context context)
    {
        context.set(ARQ.hideNonDistiguishedVariables, true) ;
        context.set(strictGraph,            true) ;
        context.set(strictSPARQL,           true) ;
        context.set(constantBNodeLabels,    false) ;
        context.set(regexImpl,              xercesRegex) ;
    }
    
    private static Context globalContext = null ;
    
    private static boolean initialized = false ;
    // Force a call
    static { init() ; }
    
    /** Ensure things have started - applications do not need call this.
     * The method is public so any part of ARQ can call it.
     */
    
    
    public static synchronized void init()
    { 
        if ( initialized )
            return ;
        initialized = true ;
        
        globalContext = new Context() ;  
        globalContext.setIfUndef(strictSPARQL,                  "false") ; 
        globalContext.setIfUndef(constantBNodeLabels,           "true") ;
        globalContext.setIfUndef(enablePropertyFunctions,       "true") ;
        globalContext.setIfUndef(strictGraph,                   "false") ;
        
        //globalContext.setIfUndef(useSAX,                        "false") ;
        globalContext.setIfUndef(enableRomanNumerals,           "false") ;
        globalContext.setIfUndef(enableXSDdate,                 "true") ;
        // enableBlankNodeLabels() ;
        globalContext.setIfUndef(regexImpl,                     javaRegex) ;
//        if (  getContext().isTrue(romanNumeralsAsFirstClassDatatypes) )
//            RomanNumeralDatatype.enableAsFirstClassDatatype() ; // Wires into the TypeMapper.
        return ;
    }
    
    public static Context getContext() { return globalContext ; }
//    
    // Convenience call-throughs
    public static void set(Symbol symbol, boolean value)  { getContext().set(symbol, value) ; }
    public static void setTrue(Symbol symbol)             { getContext().setTrue(symbol) ; }
    public static void setFalse(Symbol symbol)            { getContext().setFalse(symbol) ; }
    public static void unset(Symbol symbol)               { getContext().unset(symbol) ; }
    public static boolean isTrue(Symbol symbol)           { return getContext().isTrue(symbol) ; }
    public static boolean isFalse(Symbol symbol)          { return getContext().isFalse(symbol) ; }
    public static boolean isTrueOrUndef(Symbol symbol)    { return getContext().isTrueOrUndef(symbol) ; }
    public static boolean isFalseOrUndef(Symbol symbol)   { return getContext().isFalseOrUndef(symbol) ; }
//    
    // ----------------------------------
    
    /** The root package name for ARQ */   
    public static final String PATH = "com.hp.hpl.jena.query";
   
    /** The product name */   
    public static final String NAME = "@name@";
   
    /** The ARQ web site : see also http://jena.sourceforge.net*/   
    public static final String WEBSITE = "@website@";
   
    /** The full name of the current ARQ version */   
    public static final String VERSION = "@version@";
   
    /** The major version number for this release of ARQ (ie '2' for ARQ 2.0) */
    public static final String MAJOR_VERSION = "@version-major@";
   
    /** The minor version number for this release of ARQ (ie '0' for ARQ 2.0) */
    public static final String MINOR_VERSION = "@version-minor@";
   
    /** The version status for this release of ARQ (eg '-beta1' or the empty string) */
    public static final String VERSION_STATUS = "@version-status@";
   
    /** The date and time at which this release was built */   
    public static final String BUILD_DATE = "@build-time@";
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