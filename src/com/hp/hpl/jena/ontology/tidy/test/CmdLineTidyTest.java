/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: CmdLineTidyTest.java,v 1.1 2004-12-13 17:17:55 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;
 
import junit.framework.*;
import jena.*;
import com.hp.hpl.jena.ontology.tidy.*;
import com.hp.hpl.jena.ontology.tidy.impl.*;
import com.hp.hpl.jena.ontology.*;
import java.util.*;
import java.io.*;

import com.hp.hpl.jena.rdf.model.*;
//import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.OWLTest;
import com.hp.hpl.jena.shared.wg.*;
/**
 *  @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
*
*/
public class CmdLineTidyTest extends TestCase {

	static final String NL = System.getProperty("line.separator", "\n");

	public CmdLineTidyTest(String nm) {
		super(nm);
	}
	
	private void cmdLine(String rslt, String args){
		PrintStream oldOut = System.out;
		PrintStream oldErr = System.err;
		try {
          PrintStream
		out = new PrintStream(new OutputStream() {

			public void write(int b) throws IOException {
			   throw new RuntimeException("unexpected output");
			}
		});
          System.setOut(out);
          System.setErr(out);
		assertEquals(rslt+NL,owlsyntax.mainStr(("-q " + args).split(" ")));
		}
		finally {
			System.setErr(oldErr);
			System.setOut(oldOut);
		}
    }
	public void testN3Tidy() {
		cmdLine("DL","-L N3 file:testing/ontology/tidy/nothing.n3");
	}
	
	public void testRDFXMLTidy() {
		cmdLine("DL","file:testing/wg/Nothing/conclusions002.rdf");
	}

	public void testRDFXMLDocManTidy() {
		cmdLine("DL","-m file:testing/ontology/tidy/ont-policy-syntax-test.rdf http://example.org/tidyTest");
	}

	public void testN3DocManTidy() {
		cmdLine("DL","-L N3 -m file:testing/ontology/tidy/ont-policy-syntax-test.rdf http://example.org/tidyN3Test");		
	}

	static public Test suite() {
			TestSuite s = new TestSuite(CmdLineTidyTest.class);
			s.setName("Cmd Line OWL syntax");
			return s;
	   }
    

}

/*
	(c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/