/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: SyntaxTest.java,v 1.1 2003-04-18 20:36:00 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;

import junit.framework.TestCase;
import com.hp.hpl.jena.ontology.tidy.*;
import java.util.Iterator;
 /**
  *  @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
class SyntaxTest extends TestCase {
	

	/**
	 * @param arg0
	 */
	public SyntaxTest(String nm, String lvl) {
		super(nm);
		level = lvl;
	}
	
	String level;
	
	protected void runTest() {
		//System.err.println(getName() + " is " + level);
		Checker chk = new Checker(level.equals("Lite"));
		
		chk.load("file:testing/wg/"+getName()+".rdf");
		String rslt = chk.getSubLanguage();
		if ( !rslt.equals(level) ) {
		   if ( level.equals("Lite") || rslt.equals("Full") ) {
		   	// print out msgs
			Iterator it = chk.getProblems();
			while (it.hasNext()) {
				SyntaxProblem sp = (SyntaxProblem) it.next();
				System.err.println(sp.longDescription());
			}
		   }
		   String msg = getName() + " is found as OWL " + rslt + " not " + level;
		   System.err.println(msg);
		   fail(msg);
		}
		
	}

}

/*
	(c) Copyright Hewlett-Packard Company 2003
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