/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql;

/** 
 * @author		Andy Seaborne
 * @version 	$Id: QueryResultsMem.java,v 1.2 2003-02-20 16:21:58 andy_seaborne Exp $
 */

import java.util.*;
import java.io.*;
import java.net.*;

import com.hp.hpl.jena.rdql.parser.ParsedLiteral; 
import com.hp.hpl.jena.rdql.* ;

import com.hp.hpl.jena.util.tuple.*;
import com.hp.hpl.jena.util.*;

/**
 * @author		Andy Seaborne
 * @version 	$Id: QueryResultsMem.java,v 1.2 2003-02-20 16:21:58 andy_seaborne Exp $
 */


public class QueryResultsMem implements QueryResults
{
	static boolean DEBUG = false;
	// In-memory structures for a results Iterator.

	List rows = new ArrayList();
	List varNames = null ;
	int rowNumber = 0 ;
	Iterator iterator ;
	boolean isEmpty = false ;
	
	// Duplicate
	public QueryResultsMem(QueryResultsMem imrs2)
	{
		this(imrs2, false) ;
	}


	public QueryResultsMem(QueryResultsMem imrs2, boolean takeCopy)
	{
		varNames = imrs2.varNames;
		if ( takeCopy )
		{
			for (Iterator iter = imrs2.rows.iterator(); iter.hasNext();)
			{
				rows.add((ResultBinding) iter.next());
			}
		}
		else
			// Share results (not the iterator.
			rows = imrs2.rows ;
		iterator = rows.iterator() ;
		reset() ;
	}

	/** Create an in-memory result set from any QueryResults object.
	 *  If the QueryResults is an in-memory one already, then no
	 *  copying is done - the necessary internal datastructures
	 *  are shared.
	 */

	public QueryResultsMem(QueryResults qr)
	{
		if ( qr instanceof QueryResultsMem )
		{
			QueryResultsMem qrm = (QueryResultsMem)qr ;
			this.rows = qrm.rows ;
		}
		else
			while(qr.hasNext()) 
			{
				ResultBinding rb = (ResultBinding)qr.next() ;
				rows.add(rb) ;
			}
			
		varNames = qr.getResultVars() ;
		qr.close() ;
		reset() ;
	}

	// Read in a result set in the mysterious dump format
	// ?var value ?var value ...
	public QueryResultsMem(Reader reader)
	{
		// Awaiting interface-ization of QueryResult?
		buildFromDumpFormat(reader);
	}

	public QueryResultsMem(String urlStr)
		throws java.io.FileNotFoundException
	{
		// Awaiting interface-ization of QueryResult?
		Reader reader = null;
		try
		{
			URL url = new URL(urlStr);
			reader =
				new BufferedReader(new InputStreamReader(url.openStream()));
		} catch (java.net.MalformedURLException e)
		{
			// Try as a file.
			String filename = urlStr;
			FileReader fr = new FileReader(filename);
			reader = new BufferedReader(fr);
		} catch (java.io.IOException ioEx)
		{
			Log.severe("IOException: " + ioEx, "QueryResultsUtils", "", ioEx);
			return;
		}
		buildFromDumpFormat(reader);
	}
	
	
   /**
     *  @throws UnsupportedOperationException Always thrown.
     */

	public void remove() throws java.lang.UnsupportedOperationException
	{
		throw new java.lang.UnsupportedOperationException(
			"QueryResultsMem: Attempt to remove an element");
	}

    /**
     * Is there another possibility?
     */
    public boolean hasNext() { return iterator.hasNext() ; }

    /** Moves onto the next result possibility.
     *  The returned object should be of class ResultBinding
     */
    
    public Object next() { rowNumber++ ; return iterator.next() ; }

	/** Close the results set.
	 *  Should be called on all QueryResults objects
	 */
	
    public void close() { return ; }

	public void reset() { iterator = rows.iterator() ; rowNumber = 0 ; }

	/** Return the "row" number for the current iterator item
	 */
    public int getRowNumber() { return rowNumber ; }
    
	/** Return the number of rows
	 */
    public int size() { return rows.size() ; }
    
    /** Get the variable names for the projection
     */
    public List getResultVars() { return varNames ; }

    /** Convenience function to consume a query.
     *  Returns a list of {@link ResultBinding}s.
     *
     *  @return List
     *  @deprecated   Old QueryResults operation
     */

    public List getAll() { return rows ; }
    

	private void buildFromDumpFormat(Reader reader)
	{
		varNames = new ArrayList() ;
		TupleSet ts = new TupleSet(reader);
		// The first row is special - it records the variables.
		// If there is none, then there were no variables in the query 
		// and none specified in the SELECT.
		
		// First row.
		if ( ! ts.hasNext() )
		{
			// Empty
			varNames = new ArrayList() ;
			isEmpty = true ;
			return ;
		}
				
		List firstRow = (List)ts.next() ;
		if ( firstRow.size() == 0 )
		{
			Log.severe("No variable names yet result set is not empty") ;
			return ;
		}
		
		for ( Iterator iter1 = firstRow.iterator() ; iter1.hasNext() ; )
		{
			// Trim the ?
			TupleItem tmp = (TupleItem)iter1.next() ;
			String v = tmp.get().substring(1);
			varNames.add(v) ;
		}
		
		// Data		

		for (; ts.hasNext();)
		{
			List row = (List) ts.next();
			//System.err.println("New row") ;

			ResultBinding thisRow = new ResultBinding();
			for (Iterator iter = row.iterator(); iter.hasNext();)
			{
				TupleItem varName = (TupleItem) iter.next();
				if (!iter.hasNext())
				{
					Log.severe(
						"Odd number of items in dumped result set",
						"QueryResult2",
						"");
					return;
				}
				TupleItem value = (TupleItem) iter.next();
				if (!varName.get().startsWith("?"))
				{
					Log.severe(
						"Variable name does not start with a ?",
						"QueryResult2",
						"");
					return;
				}

				String var = varName.get().substring(1);
				
				if (!varNames.contains(var))
				{
					Log.severe("Variable " + var + " is unknown");
					return;
				}
				// Maybe should try to create "real" Jena objects
				// Query literal - not a Jena RDf Literal
				ParsedLiteral l = null;
				if (value.isURI())
					l = ParsedLiteral.makeURI(value.get());
				else
					l = ParsedLiteral.makeString(value.get());
				thisRow.add(var, l);
				//System.err.println("This row: "+thisRow) ;
			}
			rows.add(thisRow);
		}
		// Set internal structures
		reset() ;
	}

	// Not efficient - could "sort" each result set then compare.
	// Must be clear and correct because it is used in testing.
	// COPY (or DESTRUCTIVE).  Which is why its is not the "equal" operation

	static public boolean equivalent(
		QueryResultsMem irs1,
		QueryResultsMem irs2)
	{
		if (irs1.rows.size() != irs2.rows.size())
		{
			if ( DEBUG )
				System.err.println("Not equivalent: different row sizes: ("+
								   irs1.rows.size()+", "+irs2.rows.size()+")") ;
			return false;
		}

		if (irs1.varNames.size() != irs2.varNames.size())
		{
			if ( DEBUG )
				System.err.println("Not equivalent: different numbers of variables: ("+
								   irs1.varNames.size()+", "+irs2.varNames.size()+")") ;
			return false;
		}

		List varNames = irs1.varNames;

		// Compare var names
		for (Iterator vIter1 = irs1.varNames.iterator(); vIter1.hasNext();)
		{
			String vn1 = (String) vIter1.next();
			if (!irs2.varNames.contains(vn1))
			{
				return false;
			}
		}

		// Copy for destructive testing
		irs2 = new QueryResultsMem(irs2, true);

		// For every row, find a match.
		// Note we remove the match each time from irs2.  DESTRUCTIVE.
		for (Iterator iter1 = irs1.rows.iterator(); iter1.hasNext();)
		{
			if (DEBUG)
			{
				System.err.println("Set 1");
				int i = 0;
				for (Iterator iter = irs1.rows.iterator(); iter.hasNext();)
				{
					i++;
					ResultBinding rb = (ResultBinding) iter.next();
					System.err.println(i + " " + rb);
				}

				System.err.println("Set 2");
				i = 0;
				for (Iterator iter = irs2.rows.iterator(); iter.hasNext();)
				{
					i++;
					ResultBinding rb = (ResultBinding) iter.next();
					System.err.println(i + " " + rb);
				}
			}

			ResultBinding rb1 = (ResultBinding) iter1.next();

			// Can we find a match to the row rb1?
			boolean foundRowMatch = false;
			for (Iterator iter2 = irs2.rows.iterator(); iter2.hasNext();)
			{
				ResultBinding rb2 = (ResultBinding) iter2.next();
				if (sameRow(varNames, rb1, rb2))
				{
					// Match!
					foundRowMatch = true;
					iter2.remove();
					break;
				}
			}
			if (!foundRowMatch)
				return false;
		}
		if (irs2.rows.size() != 0)
		{
			System.err.println("Warning: still got some rows left");
			System.err.println(irs2.toString());
		}
		// Couldn't find a difference so must be the same.
		return true;

	}


	// Only checks that mentioned variables are the same.
	// There may be working variables in the rows but these are ignored.
	private static boolean sameRow(
		List varNames,
		ResultBinding row1,
		ResultBinding row2)
	{
		Map bNodeMap = new HashMap() ;
		
		if (DEBUG)
		{
			System.err.println("Row1 = " + row1.toString());
			System.err.println("Row2 = " + row2.toString());
		}

		// Note: we have already checked the declared variable names agree.

		for (Iterator i1 = varNames.iterator(); i1.hasNext();)
		{
			String vname = (String) i1.next();
			Object obj1 = row1.get(vname) ;
			Object obj2 = row2.get(vname) ;
			Value v1 = row1.getValue(vname);
			Value v2 = row2.getValue(vname);

			if (DEBUG)
			{
				System.err.println("Compare: ?"+vname);
				System.err.println("    " + v1.asQuotedString());
				System.err.println("    " + v2.asQuotedString());
			}

			// See if the two things are bNodes.
			// We test their string forms because result sets can be read in from a dump file.
//			if ( obj1 instanceof Resource && obj2 instanceof Resource &&
//				 ((Resource)obj1).isAnon() && ((Resource)obj2).isAnon() )
			
			if ( v1.asQuotedString().startsWith("<anon:") &&
	 			 v2.asQuotedString().startsWith("<anon:") )
			{
				String bNode1 = v1.asQuotedString() ;
				String bNode2 = v2.asQuotedString() ;
				if ( bNodeMap.containsKey(bNode1) )
				{
					if ( ! bNodeMap.get(bNode1).equals(bNode2) )
						return false ;
					if ( DEBUG )
						System.err.println("BNodes the same") ;
					// Do match
					continue ;
				}
				// New in this row.  Assume matches.
				bNodeMap.put(bNode1, bNode2) ;
 				continue ;
			}
			if (!v1.asQuotedString().equals(v2.asQuotedString()))
				return false;
		}
		
		return true;
	}

	/** Print out the result set in dump format.
	 */

	public void list(PrintWriter pw)
	{
		QueryResultsMem qr2 = new QueryResultsMem(this) ;
		QueryResultsFormatter fmt = new QueryResultsFormatter(this) ;
		fmt.dump(pw, false) ;
		qr2.close() ;
	}
}


/*
 *  (c) Copyright Hewlett-Packard Company 2002
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
