/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.test;

import java.io.* ;
import com.hp.hpl.jena.n3.* ;
import junit.framework.* ;

import com.hp.hpl.jena.rdf.model.* ;
//import com.hp.hpl.jena.rdf.model.impl.* ;
import com.hp.hpl.jena.mem.* ;

import com.hp.hpl.jena.util.ModelLoader;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaReaderTests.java,v 1.3 2003-02-20 16:48:57 andy_seaborne Exp $
 */
public class N3JenaReaderTests extends N3ExternalTestsCom
{
	static public boolean VERBOSE = false ;

	public N3JenaReaderTests()
	{
		this("n3-reader-tests") ;
	}
	
	public N3JenaReaderTests(String filename)
	{
		super("N3 Jena Reader tests", filename) ;
	}

	protected void makeTest(String n3File, String resultsFile)
	{
		String testName = n3File ;

		if ( basedir != null )
			n3File = basedir+"/"+n3File ;

		if ( basedir != null && resultsFile != null && !resultsFile.equals("") )
			resultsFile = basedir + "/" + resultsFile ;
			
		addTest(new Test(testName, n3File, resultsFile)) ; 
	}

	static class Test extends TestCase
	{
		RDFReader reader = null ;
		String basename = null ;
		String n3File = null ;
		String resultsFile = null ;	
		Model dModel = null ;	
		Model rModel = null ;
		Reader rData = null ;
		
		Test(String testName, String _n3File, String _resultsFile)
		{
			super("N3 Jena Reader test: "+testName) ;
			n3File = _n3File ;
			resultsFile = _resultsFile ;
			try {
                
				rData = makeReader(new FileInputStream(n3File)) ;

				// Check the files exist
				dModel = new ModelMem() ;
				
				if ( resultsFile != null && !resultsFile.equals("") )
				{
					rModel = ModelLoader.loadModel(resultsFile, null) ;
					if ( rModel == null )
						System.err.println("Failed to find results file "+resultsFile) ;
				}
				
				// Manually create a jena reader while not fully integrated into Jena
				reader = new N3JenaReader() ;
				int ind = n3File.lastIndexOf(File.pathSeparatorChar) ;
				if ( ind == -1 )
					// Not found: maybe UNIX syntax on windows.
					ind = n3File.lastIndexOf('/') ;
				String x = n3File.substring(ind+1) ;
				// Use a fake basename to make this more portable.
				// The tests results data knows this basename.
				basename = "file:///base/"+x ;
				//basename = basename.replace('\\', '/') ;
			} catch (IOException ioEx)
			{
				System.err.println("IO Exception: "+ioEx) ;
			}
		}
	
		
		protected void runTest() throws Throwable
		{
			reader.read(dModel, rData, basename) ;
			if ( VERBOSE )
			{
				Writer w = makeWriter(System.out) ;
				BufferedReader r = makeReader(new FileInputStream(n3File)) ;
				println(w, "+++++++ "+this.getName()) ;
				for ( String s = r.readLine(); s != null ; s = r.readLine())
					println(w,s) ;
				println(w,"+++++++") ;
				dModel.write(w, "N-TRIPLE") ;
				println(w,"+++++++") ;
				flush(w) ;
			}
			if ( rModel != null )
			{
                if ( ! dModel.isIsomorphicWith(rModel) )
				{
                    
					Writer w = makeWriter(System.out) ;
					println(w, "+++++++ "+super.getName()) ;
					println(w, "---- Created") ;
					dModel.write(w, "N-TRIPLE") ;
					println(w, "---- Expected ") ;
					rModel.write(w, "N-TRIPLE") ;
					println(w, "+++++++"+super.getName()) ;
					flush(w) ;
					assertTrue("Model compare failed: "+super.getName(), false) ;
                }
			}
				
		}
	}
	
	static final String NL = System.getProperty("line.separator","\n") ;
	static private void print(Writer out, String s) { try { out.write(s) ; } catch (java.io.IOException ex) {} }
	static private void println(Writer out, String s) { try { out.write(s) ; out.write(NL) ;}catch (java.io.IOException ex) {} }
	static private void println(Writer out) { try { out.write(NL) ;} catch (java.io.IOException ex) {} }
	static private void flush(Writer out) { try { out.flush() ; } catch (java.io.IOException ex) {} }
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
