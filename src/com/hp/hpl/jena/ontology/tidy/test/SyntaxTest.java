/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SyntaxTest.java,v 1.8 2003-11-24 19:40:12 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;

import junit.framework.TestCase;
import com.hp.hpl.jena.ontology.tidy.*;
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
class SyntaxTest extends TestCase {

	/**
	 * @param arg0
	 */
	static String name(String testURI, String nm) {
		int lastSl = testURI.lastIndexOf('/');
		int penUltimateSl = testURI.lastIndexOf('/', lastSl - 1);
		return testURI.substring(penUltimateSl + 1, lastSl) + "-" + nm;
	}
	//System.err.println(lastSl + " " + penUltimateSl + " " + hash);
	
	public SyntaxTest(String nm, TestInputStreamFactory factory, String u) {
		super(name(u,nm));
		dm = new DocMan(factory);
		uri = u;
	}

	
	final DocMan dm;
	final String uri;
	
	static private class DMEntry {
		Model mdl;
		final String url;
		final InputStream in;
		final Resource lvl;
		DMEntry(String u, Resource l, InputStream i) {
			mdl = null;
		  url = u;
		  lvl = l;
		  in = i;
		}
		void init() {
			if (mdl==null){
				mdl = ModelFactory.createDefaultModel();
				mdl.read(in,url);
			}
		}
	}
	static private class DocMan extends OntDocumentManager {
		DocMan(TestInputStreamFactory f) {
			super("");
			fact = f;
		}
	  protected boolean read( Model model, String uri, boolean warn ){
	  	DMEntry entry = (DMEntry)table.get(uri);
	  	if (entry == null) {
	  		model.read(fact.open(uri),uri);
	  	} else {
			entry.init();
			model.add(entry.mdl);
	  	}
	  	return  true;
	  }
	  final Map table = new HashMap();
	  final TestInputStreamFactory fact;
	  void add(String url, DMEntry e) {
	  	table.put(url,e);
	  }
	}

	void add(InputStream in0, Resource r, String url) {
		dm.add(url, new DMEntry(url,r,in0));
	}

	protected void runTest() {
		Iterator i = dm.table.keySet().iterator();

		while (i.hasNext()) {
			String url = (String)i.next();
			DMEntry ent = (DMEntry)dm.table.get(url);
			Resource level = ent.lvl;
			
		//	if (!url.equals("http://www.w3.org/2002/03owlt/Restriction/conclusions006"))
		//	  continue;
			Checker chk = new Checker(ent.lvl.equals(OWLTest.Lite));
			ent.init();
			OntModel om = ModelFactory.createOntologyModel( 
			new OntModelSpec(null,dm,null,ProfileRegistry.OWL_LANG)  ,
			ent.mdl);
		//	if (true)
		//	  return;
			//(InputStream) inI.next(),
			//om.read()
			chk.add(om.getGraph());
			
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
				WGTests.logResult(uri,false);
				fail(msg);
			}
		}
		WGTests.logResult(uri,true);
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