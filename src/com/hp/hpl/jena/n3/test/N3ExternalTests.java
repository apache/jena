/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.test;

import java.io.* ;
import com.hp.hpl.jena.n3.* ;
import junit.framework.* ;

import com.hp.hpl.jena.rdf.model.* ;
//import com.hp.hpl.jena.common.* ;
//import com.hp.hpl.jena.mem.* ;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3ExternalTests.java,v 1.3 2003-02-20 16:48:54 andy_seaborne Exp $
 */
public class N3ExternalTests extends N3ExternalTestsCom
{
	static public boolean VERBOSE = false ;
	public N3ExternalTests()
	{
		this("n3-parser-tests") ;
	}
	
	public N3ExternalTests(String filename)
	{
		super("N3 Parser tests", filename) ;
	}

	protected void makeTest(String n3File, String resultsFile)
	{
		String testName = n3File + "::" + resultsFile ;
		
		if ( basedir != null )
			n3File = basedir+"/"+n3File ;

		if ( basedir != null && resultsFile != null && !resultsFile.equals("") )
			resultsFile = basedir + "/" + resultsFile ;
			
		addTest(new Test(testName, n3File, basedir+"/"+resultsFile)) ; 
	}

	static class Test extends TestCase
	{
		N3Parser parser = null ;
		String n3File = null ;
		String resultsFile = null ;	
		Reader rData = null ;
		
		Test(String testName, String _n3File, String _resultsFile)
		{
			super("N3 Parser test: "+testName) ;
			n3File = _n3File ;
			resultsFile = _resultsFile ;
			try {
				rData = new FileReader(n3File) ;
				parser = new N3Parser(new BufferedReader(rData), new NullN3EventHandler()) ;
			} catch (IOException ioEx)
			{
				System.err.println("IO Exception: "+ioEx) ;
			}
		}
	
		
		protected void runTest() throws Throwable
		{
			try {
				parser.parse() ;
				if ( VERBOSE )
				{
					PrintWriter pw = new PrintWriter(System.out) ;
	
					BufferedReader r = new BufferedReader(new FileReader(n3File)) ;
					pw.println("+++++++ "+this.getName()) ;
					for ( String s = r.readLine(); s != null ; s = r.readLine())
						pw.println(s) ;
					pw.println("+++++++") ;
					pw.flush() ;
				}

			} catch (Exception ex)
			{
				// @@CLEANUP
				throw new RDFException(ex) ;
			}
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
 */
