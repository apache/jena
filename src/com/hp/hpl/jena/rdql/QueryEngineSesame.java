/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

import java.util.*;
import java.net.*;
import java.io.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.*;

import EDU.oswego.cs.dl.util.concurrent.*;

// Sesame
// Need to be explicit to avoid name clashes (e.g. Resource)
//import nl.aidministrator.rdf.client.* ;
//import nl.aidministrator.rdf.client.query.* ;
//import nl.aidministrator.rdf.client.model.* ;
//import nl.aidministrator.rdf.client.repositorylist.* ;
//import nl.aidministrator.rdf.client.admin.* ;

/** QueryExecution implemenetation that issues a query to a
 *  <a href="http://sesame.aidministrator.nl/">Sesame</a> repository
 *  and presents the Jena interface to queries.
 * 
 * @author		Andy Seaborne
 * @version 	$Id: QueryEngineSesame.java,v 1.4 2003-03-10 09:49:07 andy_seaborne Exp $
 */


public class QueryEngineSesame implements QueryExecution
{
	// Maps (server, repository) to bNodeMap.
	static Map repositoryBNodes = new HashMap() ;
    static final int bufferCapacity = 5 ;
    static {
    	if ( System.getProperty("org.xml.sax.driver") == null )
 			System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser") ;
    }

    
    
    Object endOfPipeMarker = new Object() ;
	
	nl.aidministrator.rdf.client.SesameClient sesameClient = null;
	String repository;
	String repositoryKey ;
	Query query;
	
	boolean initialized = false ;
	volatile boolean queryStop = false;
    long queryStartTime = -1 ;
    volatile long queryExecTime = -1 ;
    int idQueryExecution = 0 ;

	/** Construct a query execution instance that accesses a
	 * <a href="http://sesame.aidministrator.nl/">Sesame</a> server */

	public QueryEngineSesame(Query q, String sesameServerStr, String rep) throws MalformedURLException
	{
		this(q, new URL(sesameServerStr), rep);
	}

	/** Construct a query execution instance that accesses a
	 * <a href="http://sesame.aidministrator.nl/">Sesame</a> server */

	public QueryEngineSesame(Query q, URL sesameServer, String rep) throws MalformedURLException
	{
		sesameClient = new nl.aidministrator.rdf.client.SesameClient(sesameServer);
		repository = rep;
		// Force query to know its result variables even if "SELECT *" used
		query = q;
		repositoryKey = repositoryKey(sesameClient.getServerURL().toExternalForm(), repository) ;
		if ( ! repositoryBNodes.containsKey(repositoryKey) )
			repositoryBNodes.put(repositoryKey, new HashMap()) ;
	}


	// The key string is an illegal URI.
	private String repositoryKey(String server, String repos) { return server+"<"+repos+">" ; }

	// QueryExecution interface

	/** QueryExecution interface: initialise a query execution.  Should be called before exec. */
	public void init()
	{
		if ( ! initialized )
			initialized = true ;
	}

	//** QueryExecution interface: do it! */
	public QueryResults exec()
	{
		if (query.getResultVars().size() == 0)
		{
			// Either:
			// 1 - There are no variables in the query
			// 2 - The query had "SELECT *" and did not find the variables in the query 
			//     during parsing.
			//throw new RuntimeException("No variables in query") ;
			return null;
		}

       init() ;

        final BoundedBuffer pipe = new BoundedBuffer(bufferCapacity) ;
		queryStartTime = System.currentTimeMillis() ;
		
		idQueryExecution++ ;
		new Thread("Sesame-" + idQueryExecution)
		{
			public void run()
			{
				try
				{
					// Model for this query.
					Model model = new ModelMem();
					Map bNodes = (Map)repositoryBNodes.get(repositoryKey) ;
					sesameClient.evalRdqlQuery(
						query.toString(),
						repository,
						new QueryResultsBuilder(model, query.getResultVars(), pipe, bNodes));
				}
				catch (IOException ioEx)
				{
					System.err.println("Panic: Error in SesameClient.evalRdqlQuery") ;
					try { pipe.put(endOfPipeMarker) ; } catch (InterruptedException inteEx) {}
					return ;
				}
			}
		}
		.start();

        Iterator resultsIter = new ResultsIterator(pipe) ;
        return new QueryResultsStream(query, this, resultsIter) ;
	}

	/** QueryExecution interface: not supported
	 * @throws UnsupportedOperationException
	 */
	
	public QueryResults exec(ResultBinding rb)
	{
		throw new UnsupportedOperationException("QueryEngineSesame.exec(ResultBinding)");
	}

	/** QueryExecution interface: Try to stop in mid execution.
	 */

	public void abort()
	{
		queryStop = true;
	}

	/** QueryExecution interface: Normal end of use of this execution */
	public void close()
	{
		queryStop = true;
	}

	// This depends on the query knowing the variables used even if the query was
	// "SELECT * WHERE ..."  Currently, RDQL/Jena always builds this list
	// (in extractTripePatterns in Q_Query during phase2).
	
	private class QueryResultsBuilder
		implements nl.aidministrator.rdf.client.query.QueryResultListener
	{
		boolean verbose = false;
		boolean firstInRow = true;

		ResultBinding currentRow ;
		int columnIndex = 0;

		BoundedBuffer output;
		Model model ;
		List resultVars ;
		Map bNodeMap ;

		QueryResultsBuilder(Model m, List rVars, BoundedBuffer pipe, Map bNodes)
		{
			model = m ;
			output = pipe ;
			resultVars = rVars ;
			bNodeMap = bNodes ;
		}

		public void startQueryResult()
		{
			if ( verbose )
				System.out.println("---- Start results of Sesame RDQL query") ;
		}
		
		public void endQueryResult()
		{
			if ( verbose )
			{
				System.out.println("---- End results of Sesame RDQL query") ;
				System.out.println() ;
			}
			
			try {
				output.put(endOfPipeMarker) ;
			} catch (InterruptedException intrEx)
			{
				QSys.unhandledException(intrEx, "QueryResultsBuilder", "endQueryResult") ;
				return ;
			}
		}

		public void startTuple()
		{
			firstInRow = true;
			columnIndex = 0;
			currentRow = new ResultBinding() ;
		}

		public void endTuple()
		{
			// Create the matching statements
			for ( Iterator iter = query.getTriplePatterns().iterator() ; iter.hasNext() ; )
			{
				TriplePattern tp = (TriplePattern)iter.next() ;
				Statement s = tp.asStatement(currentRow) ;
				QSys.assertTrue(s!=null, "QueryEngineSesame.QueryResultsBuilder", "endTuple", "Triple Pattern failed to grouind") ;
				currentRow.addTriple(s) ;
			}

			if (verbose)
				System.out.println();
			try {
				output.put(currentRow) ;
			} catch (InterruptedException intrEx)
			{
				QSys.unhandledException(intrEx, "QueryResultsBuilder", "endTuple") ;
				queryStop = true ;
			}
			currentRow = null ;
		}

		public void tupleValue(nl.aidministrator.rdf.client.model.Value value)
		{
			try
			{
				if (!firstInRow)
					if (verbose)
						System.out.print("  ");

				firstInRow = false;
				String varName = (String) query.getResultVars().get(columnIndex);

				RDFNode node = null;

				if (value instanceof nl.aidministrator.rdf.client.model.Resource)
				{
					if (verbose)
						System.out.print("<" + value + ">");

					nl.aidministrator.rdf.client.model.Resource resource =
						(nl.aidministrator.rdf.client.model.Resource) value;
					String uri = resource.getURI();
					
					

					if (uri == null || uri.equals(""))
					{
						System.err.print("Panic: empty URI <" + value + ">");
						return ;
					}
					
					// Nearly.  Unfortunately this is per-query execution.
					// Need global server+repository -> bNodeMap.
					// and same model for all server+repository queries
					
					if (uri.startsWith("_anon") )
					{
						if ( ! bNodeMap.containsKey(uri) )
							bNodeMap.put(uri, model.createResource()) ;
						node = (RDFNode)bNodeMap.get(uri) ;
					}
					else
						// Not a bNode
						node = model.createResource(resource.getURI());
				}
				else if (value instanceof nl.aidministrator.rdf.client.model.Literal)
				{
					if (verbose)
						System.out.print(value);

					nl.aidministrator.rdf.client.model.Literal lit =
						(nl.aidministrator.rdf.client.model.Literal) value;
					String tmp = lit.getValue();
					node = model.createLiteral(tmp);
				} else
				{
					if (verbose)
					{
						System.out.print("Unknown/") ;
						String tmp = value.getClass().getName();
						tmp = tmp.substring(tmp.lastIndexOf('.') + 1);
						System.out.print(tmp + "::" + value);
					}
				}


				columnIndex++;
				currentRow.add(varName, node);
				
				//currentRow.addTriple()
			}
			catch (RDFException rdfEx)
			{
				QSys.unhandledException(rdfEx, "QueryResultsBuilder", "tupleValue") ;
				return ;
			}
		}
		
	}

	// Convert from a BoundedBuffer to an iterator.

	private class ResultsIterator implements Iterator
	{
		BoundedBuffer pipe;
		Object nextThing;

		ResultsIterator(BoundedBuffer p)
		{
			pipe = p;
			nextThing = null;
		}

		public boolean hasNext()
		{
			try
			{
				if (queryStop)
					return false;

				// Implements "blocking poll"
				if (nextThing == null)
					nextThing = pipe.take();

				boolean isMore = (nextThing != endOfPipeMarker);
				
				if (!isMore && query.executeTime == -1)
					query.executeTime = System.currentTimeMillis() - queryStartTime;

				return isMore;
			}
			catch (InterruptedException e)
			{
				QSys.unhandledException(e, "QueryEngineSesame.ResultsIterator", "hasNext");
			}
			return false;
		}

		public Object next()
		{
			//if ( query.logging )
			if (!hasNext())
				return null;
			Object x = nextThing;
			nextThing = null;
			return x;
		}

		public void remove()
		{
			throw new java.lang.UnsupportedOperationException("QueryEngineSesame.ResultsIterator.remove");
		}
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
