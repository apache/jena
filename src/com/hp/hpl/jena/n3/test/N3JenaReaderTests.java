/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3.test;

import java.io.* ;
import com.hp.hpl.jena.n3.* ;
import junit.framework.* ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.util.FileManager;
/**
 * @author		Andy Seaborne
 * @version 	$Id: N3JenaReaderTests.java,v 1.9 2005-02-21 12:04:09 andy_seaborne Exp $
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
				dModel = ModelFactory.createDefaultModel() ;
				
				if ( resultsFile != null && !resultsFile.equals("") )
				{
					rModel = FileManager.get().loadModel(resultsFile, null) ;
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
				PrintWriter w = makeWriter(System.out) ;
				BufferedReader r = makeReader(new FileInputStream(n3File)) ;
				w.println("+++++++ "+this.getName()) ;
				for ( String s = r.readLine(); s != null ; s = r.readLine())
					w.println(s) ;
				w.println("+++++++") ;
				dModel.write(w, "N-TRIPLE") ;
				w.println("+++++++") ;
				w.flush() ;
			}
			if ( rModel != null )
			{
                if ( ! dModel.isIsomorphicWith(rModel) )
				{
                    
					PrintWriter w = makeWriter(System.out) ;
					w.println("+++++++ "+super.getName()) ;
					w.println("---- Created") ;
					dModel.write(w, "N-TRIPLE") ;
					w.println("---- Expected ") ;
					rModel.write(w, "N-TRIPLE") ;
					w.println("+++++++"+super.getName()) ;
					w.flush() ;
					assertTrue("Model compare failed: "+super.getName(), false) ;
                }
			}
				
		}
	}

}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
