/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.io.* ;
import java.util.* ;
import org.apache.log4j.Logger;


import com.hp.hpl.jena.rdql.parser.*;

import com.hp.hpl.jena.rdf.model.* ;

/** The data structure for a query.
 *  There are two ways of creating a query - use the parser to turn
 *  a string description of the query into the executable form, and
 *  the programmatic way (the parser is calling the programmatic
 *  operations driven by the quyery string).  The declarative approach
 *  of passing in a string is preferred.
 *
 * Once a query is built, it can be passed to a query engine.
 * @see QueryEngine
 * @see QueryResults
 * 
 * @author		Andy Seaborne
 * @version 	$Id: Query.java,v 1.3 2003-02-20 16:45:47 andy_seaborne Exp $
 */

public class Query
{
    static Logger logger = Logger.getLogger("com.hp.hpl.jena.rdql") ;
    
    // The names of variables wanted by the caller.
    protected List resultVars = new ArrayList() ;         // Type in list: String name
    protected List triplePatterns = new ArrayList() ;     // Type in list: TriplePatterns
    protected List constraints = new ArrayList() ;        // Type in list: Constraints
    
    protected Map prefixMap = new HashMap() ;
    protected static Map defaultPrefixMap = new HashMap() ;
    static
    {
		defaultPrefixMap.put("rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
		defaultPrefixMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#") ;
        defaultPrefixMap.put("xsd" , "http://www.w3.org/2001-2003/XMLSchema#") ; 
        defaultPrefixMap.put("owl" , "http://www.w3.org/2001-2003/07/owl#") ; 
    }

    // Turn logging on and off.
    // This is in addition to the log levels.
    boolean loggingOn = false ;

    Logger log = logger ;

    // If no model is provided explicitly, the query engine will load
    // a model from the URL.
    String sourceURL = null ;
    Model source = null ;

    // Statistics
    public long parseTime = -1 ;
    public long buildTime = -1 ;
    public long loadTime = -1 ;
    public long executeTime = -1 ;

    /** Create a query from the given string by calling the parser.
     *  After it has been created, an application should set the data source, then
     *  call the QueryEngine.
     *
     * @param String The query string
     * @throws QueryException Thrown when a parse error occurs
     */

    public Query(String s)
    {
        this() ;
        Q_Query query = null ;
        try {
            long initTime = 0;
            parseTime = 0;
            long startTime = 0;
            long stopTime = 0;

            ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes()) ;

            startTime = System.currentTimeMillis();
            RDQLParser parser = new RDQLParser(in) ;
            parser.CompilationUnit();
            parseTime = System.currentTimeMillis() - startTime;

            if ( loggingOn )
                log.debug("Query parse time: "+parseTime) ;

            query = (Q_Query)parser.top() ;
            // Post-parsing work on the query tree
            query.phase2(this) ;

            buildTime = System.currentTimeMillis() - parseTime - startTime ;
            if ( loggingOn )
                log.debug("Query parse and build time: "+buildTime) ;
        }
        catch (QueryException qEx) { throw qEx ; }
        catch (Exception e)
        {
            //e.printStackTrace(System.err) ;
            throw new QueryException("Parse error: "+e) ;
        }
    }

    /** Create a blank query.  The application is expected to complete the query parts needed
     *  by calling the various "add" operations later,
     */

    public Query()
    {
    }

    /** Convenience function to parse and execute a query.
     *
     *  @param     queryString      The query: should include FROM clause to provide the data
     *  @throws    QueryException   Runtime exception
     */
    public static QueryResults exec(String queryString)
    {
        Query q = new Query(queryString) ;
        QueryEngine qe = new QueryEngine(q) ;
        qe.init() ;
        return qe.exec() ;
    }

    /** Convenience function to parse and execute a query against an existing model.
     *
     *  @param     queryString      The query: the FROM clause will be overridden
     *  @param     model            The data
     *  @throws    QueryException   Runtime exception
     */
    public static QueryResults exec(String queryString, Model model)
    {
        Query q = new Query(queryString) ;
        if ( model != null )
            q.setSource(model);
        QueryEngine qe = new QueryEngine(q) ;
        qe.init() ;
        return qe.exec() ;
    }


    /** Convenience function to parse and execute a query against a remote model.
     *  The remote data is loaded into the local application.
     *
     *  @param     queryString      The query: the FROM clause will be overridden
     *  @param     datatURL         The remote data source
     *  @throws    QueryException   Runtime exception
     */
    public static QueryResults exec(String queryString, String dataURL)
    {
        Query q = new Query(queryString) ;
        q.setSourceURL(dataURL) ;
        QueryEngine qe = new QueryEngine(q) ;
        qe.init() ;
        return qe.exec() ;
    }


    /** The data target for the query as a Jena model.
     *  Applications must call this whther using the parser or not.
     */

    public void setSource(Model m)                  { source = m ; }
    /** Return the model that this query executes against. */
    public Model getSource()                        { return source ; }

    /** Location of the source for the data.  If the model is not set, then the QueryEngine
     *  will attempt to load the data from this URL.
     */
    public void setSourceURL(String s)              { sourceURL = s ; }
    public String getSourceURL()                    { return sourceURL ; }

    /** Programmatic API operation */
    public List getResultVars() { return resultVars ; }
    
    /** Programmatic API operation */
    public void addResultVar(String varName)
    {
        if ( !resultVars.contains(varName) )
            resultVars.add(varName);
    }

    /** Programmatic API operation */
    public void addConstraint(Constraint c)         { constraints.add(c) ; }

    /** Programmatic API operation */
    public void addTriplePattern(TriplePattern tp)  { triplePatterns.add(tp) ; }

    /** Programmatic API operation */
    public List getTriplePatterns()  { return triplePatterns ; }

    /** Set the log destination.  By default the log does to the Jena system log
     */
     public void setLog(Logger newLog) { log = newLog ; }

     /** Get the current log */
     public Logger getLog() { return log ; }

     /** Switch for logging.  This is in addition to log levels.  Default is off */
     public void setLogging(boolean loggingSwitch) { loggingOn = loggingSwitch ; }

     /** See if we are logging */
     public boolean getLogging()                      { return loggingOn  ; }


	/** Set a prefix for this query */
	public void setPrefix(String prefix, String expansion)
	{
		prefixMap.put(prefix, expansion) ;
	}	

	/** Lookup a prefix for this query, including the default prefixes */
    public String getPrefix(String prefix)
    {
    	String s = null ;
    	
    	if ( prefixMap != null )
    		s = (String)prefixMap.get(prefix) ;
    		
    	if ( s == null )
    		s = (String)defaultPrefixMap.get(prefix) ;
    		
    	return s ;
    }



    // Reverse of parsing : should produce a string that parses to the same query
    public String toString()
    {
        StringWriter stringWriter = new StringWriter(512) ;
        PrintWriter pw = new PrintWriter(stringWriter) ;

        pw.print("SELECT  ") ;
        if ( resultVars.size() == 0 )
        {
            pw.print("*") ;
            pw.println() ;
        }
        else
        {
            boolean first = true ;
            for ( Iterator iter = resultVars.iterator() ; iter.hasNext() ; )
            {
                String var = (String)iter.next() ;
                if ( ! first )
                    pw.print(", ") ;
                pw.print("?") ;
                pw.print(var) ;
                first = false ;
            }
            pw.println() ;
        }

        // Source

        // Triple patterns
        if ( triplePatterns.size() > 0 )
        {
            pw.print("WHERE   ") ;
            boolean first = true ;
            for ( Iterator iter = triplePatterns.iterator() ; iter.hasNext() ; )
            {
                TriplePattern tp = (TriplePattern)iter.next() ;
                if ( ! first )
                {
                    pw.print(", ") ;
                    pw.println() ;
                    pw.print("        ") ;
                }
                pw.print(tp.toString()) ;
                first = false ;
            }
            pw.println() ;
        }

        // Constraints
        if ( constraints.size() > 0 )
        {
            /* Old code - print all on one line, separated by commas
            boolean first = true ;
            for ( Iterator iter = constraints.iterator() ; iter.hasNext() ; )
            {
                Constraint c = (Constraint)iter.next() ;
                if ( ! first )
                    pw.print(", ") ;
                pw.print(c.toString()) ;
                first = false ;
            }
            pw.println() ;
            */
            for ( Iterator iter = constraints.iterator() ; iter.hasNext() ; )
            {
                Constraint c = (Constraint)iter.next() ;
                pw.print("AND     ") ;
                pw.println(c.toString()) ;
            }
        }

		if ( prefixMap.size() > 0 )
		{
			pw.println("USING") ;
            boolean first = true ;
			for ( Iterator iter = prefixMap.keySet().iterator() ; iter.hasNext() ; )
			{
                if ( ! first )
                    pw.println(" ,") ;
				String k = (String)iter.next() ;
				String v = (String)prefixMap.get(k) ;
				pw.print("    "+k+" FOR <"+v+">") ;
                first = false ;
			}
            pw.println() ;
		}
        
        pw.flush() ;
        pw.close() ;
        return stringWriter.getBuffer().toString() ;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
