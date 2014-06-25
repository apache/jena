/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.n3 ;

import java.io.* ;
import junit.framework.* ;

import com.hp.hpl.jena.rdf.model.*;

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

	
	@Override
    protected void makeTest(String inputFile, String resultsFile)
	{
		String testName = inputFile ;

		if ( basedir != null )
			inputFile = basedir+"/"+inputFile ;

		if ( basedir != null && resultsFile != null && !resultsFile.equals("") )
			resultsFile = basedir + "/" + resultsFile ;
			
        // Run on each of the writers
		addTest(new Test(testName, inputFile, resultsFile,
                         N3JenaWriter.n3WriterPrettyPrinter)) ;
        addTest(new Test(testName, inputFile, resultsFile,
                         N3JenaWriter.n3WriterPlain)) ;
        addTest(new Test(testName, inputFile, resultsFile,
                         N3JenaWriter.n3WriterTriples)) ;
	}


	static class Test extends TestCase
	{
        String writerName = null ;
		String testName = null ;
		String basename = null ;
		String inputFile = null ;
		String resultsFile = null ;	
		Reader data = null ;
		
		
		Test(String _testName, String _inputFile, String _resultsFile, String wName)
		{
			super("N3 Jena Writer test: "+_testName+"-"+wName) ;
			testName = _testName ;
			inputFile = _inputFile ;
			resultsFile = _resultsFile ;
            writerName = wName ;
		}
		
		@Override
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
			
			Model model_1 = ModelFactory.createDefaultModel() ;
			model_1.read(data, uriBase, "N3") ;
            
			String tmpStr ;
			try ( StringWriter w = new StringWriter() ){
			    model_1.write(w, writerName, uriBase) ;
			    tmpStr = w.toString() ;
			}
			Model model_2 = ModelFactory.createDefaultModel() ;
			try ( StringReader r = new StringReader(tmpStr) ) {
			    model_2.read(r, uriBase, "N3") ;
			}
			if ( ! model_1.isIsomorphicWith(model_2) )
			{
				System.out.println("#### ---- "+testName+" ------------------------------") ;
                System.out.println("#### Model 1 ---- "+testName+" ------------------------------") ;
                model_1.write(System.out, "N3") ;
                System.out.println("#### Model 2 --- "+testName+" ------------------------------") ;
                model_2.write(System.out, "N3") ;
                assertTrue("Models don't match: "+testName, false) ;
			}
		}
	}
}
