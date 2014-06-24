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

package com.hp.hpl.jena.n3;

import java.io.* ;
import java.util.List ;

import junit.framework.TestSuite ;

import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.util.FileUtils ;


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
        for ( String dirbase : dirbases )
        {
            String maybeFile = dirbase + "/" + fname;
            File f = new File( maybeFile );
            if ( f.exists() )
            {
                basedir = dirbase;
                return f.getAbsolutePath();
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
