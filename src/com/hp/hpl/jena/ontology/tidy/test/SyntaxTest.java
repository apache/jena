/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SyntaxTest.java,v 1.3 2003-09-17 16:41:18 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;

import junit.framework.TestCase;
import com.hp.hpl.jena.ontology.tidy.*;
import java.util.*;
import java.io.*;
import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.vocabulary.OWLTest;
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
		//	level = lvl;
	}
	public SyntaxTest(String nm) {
		super(nm);
	}

	Vector lvls = new Vector();
	Vector in = new Vector();
	Vector urls = new Vector();

	void add(InputStream in0, Resource r, String url) {
		lvls.add(r);
		in.add(in0);
		urls.add(url);
	}

	protected void runTest() {
		Iterator inI, lvlI, urlI;
		inI = in.iterator();
		lvlI = lvls.iterator();
		urlI = urls.iterator();

		while (inI.hasNext()) {
			Resource level = (Resource) lvlI.next();
			Checker chk = new Checker(level.equals(OWLTest.Lite));
			//(InputStream) inI.next(),
			chk.load( (String) urlI.next());
			String rslt = chk.getSubLanguage();
			if (!level.getURI().endsWith(rslt)) {
				if (level.equals(OWLTest.Lite) || rslt.equals("Full")) {
					// print out msgs
					Iterator it = chk.getProblems();
					while (it.hasNext()) {
						SyntaxProblem sp = (SyntaxProblem) it.next();
						System.err.println(sp.longDescription());
					}
				}
				int hash = level.getURI().lastIndexOf('#');
				String msg =
					getName()
						+ " is found as OWL "
						+ rslt
						+ " not "
						+ level.getURI().substring(hash + 1);
				System.err.println(msg);
				fail(msg);
			}
		}
	}

}

/*
	(c) Copyright 2003 Hewlett-Packard Development Company, LP
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