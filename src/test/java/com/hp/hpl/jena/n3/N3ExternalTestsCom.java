/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import java.io.* ;
import java.util.* ;
import junit.framework.* ;

import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.tuple.* ;
import com.hp.hpl.jena.util.FileUtils;


/**
 * @author		Andy Seaborne
 * @version 	$Id: N3ExternalTestsCom.java,v 1.2 2011-03-22 11:26:39 andy_seaborne Exp $
 */
public abstract class N3ExternalTestsCom extends TestSuite
{
	// List of places
	static protected final String dirbases[] = {".", "testN3",
                                                // Jena2: correct location
                                                "testing/Turtle/N3", } ;
	
	// Record where we find the file in the constructor
	protected String basedir = null ;
	protected String testFile ;
	
	public N3ExternalTestsCom(String testName, String filename)
	{
		super(testName) ;
		testFile = findFile(filename) ;
		if ( testFile == null )
			throw new JenaException("No such file: "+filename) ;
		TupleSet tests = null ;
		try {
			Reader r = new BufferedReader(new FileReader(testFile)) ;
			tests = new TupleSet(r) ;
		} catch (IOException ioEx)
		{
			System.err.println("IO exception: "+ioEx) ;
			return ;
		}
		
		for ( ; tests.hasNext() ; )
		{
			List<TupleItem> l = tests.next() ;
			if ( l.size() != 2 )
			{
				System.err.println("Error in N3 test configuration file: "+filename+": length of an entry is "+l.size()) ;
				return ;
			}
			String n3File = l.get(0).get() ;
			String resultsFile = l.get(1).get() ;

			makeTest(n3File, resultsFile) ;
		}
	}

	abstract protected void makeTest(String n3File, String resultsFile) ;
	
	protected String findFile(String fname)
	{
		for ( int i = 0 ; i < dirbases.length ; i++ )
		{
			String maybeFile = dirbases[i]+"/"+fname ;
			File f = new File(maybeFile) ;
			if ( f.exists() )
			{
				basedir = dirbases[i] ;
				return f.getAbsolutePath() ;
			}
		}
		return null ;
	}

	// Utilities.
		
	static protected PrintWriter makeWriter(OutputStream out)
	{
        return FileUtils.asPrintWriterUTF8(out) ;
	}

	static protected BufferedReader makeReader(InputStream in)
	{
	    return new BufferedReader(FileUtils.asUTF8(in)) ;
	}
}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
