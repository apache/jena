/*
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.io.* ;
import java.util.* ;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;

import com.hp.hpl.jena.rdf.model.* ;

// Don't import the package!  Conflict with graph.Node
import com.hp.hpl.jena.rdql.parser.Q_Query ;
import com.hp.hpl.jena.rdql.parser.RDQLParser ;

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
 * @version 	$Id: Query.java,v 1.13 2003-10-24 11:06:07 andy_seaborne Exp $
 */

public class Query
{
    static Logger logger = Logger.getLogger(Query.class) ;
    
    // The names of variables wanted by the caller.
    protected List resultVars = new ArrayList() ;         // Type in list: String name
    // List of all variabes used in the triple patterns 
    protected List patternVars = new ArrayList() ;        // Type in list: String name
    protected List triplePatterns = new ArrayList() ;     // Type in list: TriplePatterns
    protected List constraints = new ArrayList() ;        // Type in list: Constraints
    
    protected Map prefixMap = new HashMap() ;
    protected static Map defaultPrefixMap = new HashMap() ;
    static
    {
		defaultPrefixMap.put("rdf",  "http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
		defaultPrefixMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#") ;
        defaultPrefixMap.put("xsd" , "http://www.w3.org/2001/XMLSchema#") ; 
        defaultPrefixMap.put("owl" , "http://www.w3.org/2002/07/owl#") ; 
    }

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
     * @param s                The query string
     * @throws QueryException  Thrown when a parse error occurs
     */

    public Query(String s)
    {
        this() ;
        Q_Query query = null ;
        try {
            //long initTime = 0;
            parseTime = 0;
            long startTime = 0;
            //long stopTime = 0;

            ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes()) ;

            startTime = System.currentTimeMillis();
            RDQLParser parser = new RDQLParser(in) ;
            parser.CompilationUnit();
            parseTime = System.currentTimeMillis() - startTime;

            logger.debug("Query parse time: "+parseTime) ;

            query = (Q_Query)parser.top() ;
            // Post-parsing work on the query tree
            query.phase2(this) ;

            buildTime = System.currentTimeMillis() - parseTime - startTime ;
            logger.debug("Query parse and build time: "+buildTime) ;
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
        QueryExecution qe = new QueryEngine(q) ;
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
        QueryExecution qe = new QueryEngine(q) ;
        qe.init() ;
        return qe.exec() ;
    }


    /** Convenience function to parse and execute a query against a remote model.
     *  The remote data is loaded into the local application.
     *
     *  @param     queryString      The query: the FROM clause will be overridden
     *  @param     dataURL         The remote data source
     *  @throws    QueryException   Runtime exception
     */
    public static QueryResults exec(String queryString, String dataURL)
    {
        Query q = new Query(queryString) ;
        q.setSourceURL(dataURL) ;
        QueryExecution qe = new QueryEngine(q) ;
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
    public List getBoundVars() { return patternVars ; }
    
    /** Programmatic API operation */
    public void addBoundVar(String varName)
    {
        if ( !patternVars.contains(varName) )
            patternVars.add(varName);
    }

    /** Programmatic API operation */
    public void addConstraint(Constraint c)         { constraints.add(c) ; }

    /** Programmatic API operation */
    public void addTriplePattern(Triple t)  { triplePatterns.add(t) ; }
    public void addTriplePattern(Node s, Node p, Node o)
    {
        Triple t = new Triple(s, p, o) ;
        triplePatterns.add(t) ;
    }

    /** Programmatic API operation */
    public List getTriplePatterns()  { return triplePatterns ; }

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
        StringBuffer sb = new StringBuffer() ; 

        sb.append("SELECT  ") ;
        if ( resultVars.size() == 0 )
            sb.append("*") ;
        else
        {
            boolean first = true ;
            for ( Iterator iter = resultVars.iterator() ; iter.hasNext() ; )
            {
                String var = (String)iter.next() ;
                if ( ! first )
                    sb.append(", ") ;
                sb.append("?") ;
                sb.append(var) ;
                first = false ;
            }
        }
        sb.append("\n") ;

        // Source

        // Triple patterns
        if ( triplePatterns.size() > 0 )
        {
            sb.append("WHERE   ") ;
            boolean first = true ;
            for ( Iterator iter = triplePatterns.iterator() ; iter.hasNext() ; )
            {
                Triple tp = (Triple)iter.next() ;
                if ( ! first )
                {
                    sb.append(", \n") ;
                    sb.append("        ") ;
                   
                }
                sb.append(triplePatternToString(tp)) ;
                first = false ;
            }
            sb.append("\n") ;
        }

        // Constraints
        if ( constraints.size() > 0 )
        {
            for ( Iterator iter = constraints.iterator() ; iter.hasNext() ; )
            {
                Constraint c = (Constraint)iter.next() ;
                sb.append("AND     ") ;
                sb.append(c.toString()) ;
                sb.append("\n") ;
            }
        }

		if ( prefixMap.size() > 0 )
		{
			sb.append("USING\n") ;
            boolean first = true ;
			for ( Iterator iter = prefixMap.keySet().iterator() ; iter.hasNext() ; )
			{
                if ( ! first )
                    sb.append(" ,\n") ;
				String k = (String)iter.next() ;
				String v = (String)prefixMap.get(k) ;
				sb.append("    "+k+" FOR <"+v+">") ;
                first = false ;
			}
            sb.append("\n") ;
		}
        
        return sb.toString() ;
    }
    
    static private String triplePatternToString(Triple tp)
    {
        StringBuffer sb = new StringBuffer() ;
        sb.append("( ") ;
        sb.append(slotToString(tp.getSubject())) ;
        sb.append(", ") ;
        sb.append(slotToString(tp.getPredicate())) ;
        sb.append(", ") ;
        sb.append(slotToString(tp.getObject())) ;
        sb.append(" )") ;
        return sb.toString() ;
    }
    
    static private String slotToString(Node n)
    {
        if ( n instanceof Node_Variable)
            return n.toString() ;
        if ( n instanceof Node_URI)
            return "<"+n+">" ;
        if ( n instanceof Node_Literal)
        {
            LiteralLabel lit = ((Node_Literal)n).getLiteral() ;
            StringBuffer sb = new StringBuffer() ;
            sb.append('"') ;
            sb.append(lit.getLexicalForm()) ;
            sb.append('"') ;
            if ( lit.language() != null && lit.language().length() > 0 )
            {
                sb.append("@") ;
                sb.append(lit.language()) ;
            }
            if ( lit.getDatatypeURI() != null )
            {
                sb.append("^^<") ;
                sb.append(lit.getDatatypeURI()) ;
                sb.append(">") ;
            }
                            
            return sb.toString() ;
        }
        //if ( n instanceof Node_Blank)
        //    return "_:"+n.toString() ;
        if ( n instanceof Node_ANY )
            // Shouldn't happen!
            return "any:"+n ;
        return "unknown:"+n ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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
