/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.test ;

import com.hp.hpl.jena.n3.test.N3ExternalTestsCom;

import java.io.* ;
import java.util.* ;

import junit.framework.* ;

import com.hp.hpl.jena.n3.* ;
import com.hp.hpl.jena.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.mem.*;

//import com.hp.hpl.mesa.rdf.jena.vocabulary.*;
//import com.hp.hpl.jena.vocabulary.*;


/**
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaWriterTests.java,v 1.1.1.1 2002-12-19 19:14:47 bwm Exp $
 */
public class N3JenaWriterTests extends N3ExternalTestsCom
{
	/* JUnit swingUI needed this */
    static public TestSuite suite() {
        return new N3JenaWriterTests() ;
    }
	
		static final String uriBase = "http://host/base/" ;
	
	public N3JenaWriterTests()
	{
		this("n3-writer-tests") ;
	}
	
	public N3JenaWriterTests(String filename)
	{
		super("N3 Jena Writer tests", filename) ;
	}

	
	protected void makeTest(String inputFile, String resultsFile)
	{
		String testName = inputFile ;

		if ( basedir != null )
			inputFile = basedir+"/"+inputFile ;

		if ( basedir != null && resultsFile != null && !resultsFile.equals("") )
			resultsFile = basedir + "/" + resultsFile ;
			
		addTest(new Test(testName, inputFile, resultsFile)) ; 
	}


	static class Test extends TestCase
	{
		String testName = null ;
		String basename = null ;
		String inputFile = null ;
		String resultsFile = null ;	
		Reader data = null ;
		
		
		Test(String _testName, String _inputFile, String _resultsFile)
		{
			super("N3 Jena Writer test: "+_testName) ;
			testName = _testName ;
			inputFile = _inputFile ;
			resultsFile = _resultsFile ;
		}
		
		protected void runTest() throws Throwable
		{
			try {
				data = makeReader(new FileInputStream(inputFile)) ;
			} catch (IOException ioEx)
			{
				fail("File does not exist: "+inputFile) ;
				return ;
			}

			// Test: write model to a string, read it again and see if same/isomorphic
			
			Model model_1 = new ModelMem() ;
			model_1.read(data, uriBase, "N3") ;
			
			StringWriter w = new StringWriter() ;
			N3JenaWriter out = new N3JenaWriter() ;
			out.write(model_1, w, uriBase) ;
			//model1.write(w, "N3", uriBase) ;
			w.close() ;
			
			StringReader r = new StringReader(w.toString()) ;
			Model model_2 = new ModelMem() ;
			model_2.read(r, uriBase, "N3") ;
			
			if ( ! ModelMatcher.equals(model_1, model_2) )
			{
				//System.out.println("#### ---- "+testName+" ------------------------------") ;
				//System.out.println(w.toString()) ;
				//System.out.flush() ;
				fail("Models don't match: "+testName) ;
			}
		}
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
