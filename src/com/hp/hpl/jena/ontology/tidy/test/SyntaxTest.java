/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: SyntaxTest.java,v 1.17 2005-01-11 10:08:30 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy.test;

import junit.framework.TestCase;
import com.hp.hpl.jena.ontology.tidy.*;
import com.hp.hpl.jena.ontology.tidy.impl.*;
import com.hp.hpl.jena.ontology.*;
import java.util.*;
import java.io.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.OWLTest;
import com.hp.hpl.jena.shared.wg.*;
import com.hp.hpl.jena.graph.*;
/**
 *  @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
*
*/
class SyntaxTest extends TestCase {
	static public boolean HP = false;
	static public Vector cnts = new Vector();
	static public Vector files = new Vector();
	static public Vector first = new Vector();

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
		super(name(u, nm));
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
			if (mdl == null) {
				mdl = ModelFactory.createDefaultModel();
				mdl.read(in, url);
				//	System.err.println("NO!!");
			}
		}
	}
	static private class DocMan extends OntDocumentManager {
		DocMan(TestInputStreamFactory f) {
			super("");
			fact = f;
		}
		protected boolean read(Model model, String uri, boolean warn) {
			DMEntry entry = (DMEntry) table.get(uri);
			if (entry == null) {
				model.read(fact.open(uri), uri);
			} else {
				entry.init();
				model.add(entry.mdl);
			}
			return true;
		}
		final Map table = new HashMap();
		final TestInputStreamFactory fact;
		void add(String url, DMEntry e) {
			table.put(url, e);
		}
	}

	void add(InputStream in0, Resource r, String url) {
		dm.add(url, new DMEntry(url, r, in0));
	}

    public void setUp() {
        // ensure the ont doc manager is in a consistent state
        OntDocumentManager.getInstance().reset( true );
    }
    
    
    protected void runTest() {
		Iterator i = dm.table.keySet().iterator();

		while (i.hasNext()) {
			String url = (String) i.next();
			DMEntry ent = (DMEntry) dm.table.get(url);
			Resource level = ent.lvl;

			CheckerImpl chk;
			if (HP) {
				StreamingChecker hpchk = new StreamingChecker(ent.lvl.equals(OWLTest.Lite));
				hpchk.getRedirect().add(
					"http://www.w3.org/2002/03owlt",
					"file:testing/wg");
				hpchk.load(url);
				chk = hpchk;
				cnts.add(new Integer(hpchk.getTripleCount()));
				first.add(hpchk.getRedirect().redirect(url));
				files.add(hpchk.getLoaded());
			} else {
				chk = new Checker(ent.lvl.equals(OWLTest.Lite));
				ent.init();
				OntModel om =
					ModelFactory.createOntologyModel(
						new OntModelSpec(
							null,
							dm,
							null,
							ProfileRegistry.OWL_LANG),
						ent.mdl);
				//	if (true)
				//	  return;
				//(InputStream) inI.next(),
				//om.read()
				chk.addRaw(om.getGraph());
			}

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
				WGTests.logResult(uri, false);
				fail(msg);
			}
			if (level.equals(OWLTest.Full)
			 //       && !HP
			        ) {
				Iterator it = chk.getProblems();
				while (it.hasNext()) {
					SyntaxProblem sp = (SyntaxProblem) it.next();
					if (sp.problemNode()!=null)
				        continue;
				    Graph g = sp.problemSubGraph();
				    Iterator ii = g.find(Node.ANY,Node.ANY,Node.ANY);
				    Set s = new HashSet();
				    while (ii.hasNext())
				        s.add(ii.next());
				    ii = s.iterator();
				    while (ii.hasNext()){
				        Triple t = (Triple)ii.next();
				        g.delete(t);
				        chk = new Checker(false);
				        chk.addRaw(g);
				        if (chk.getMonotoneLevel()==Levels.Full)
				            fail("Non-minimal solution");
				        g.add(t);
				    }
				    chk = new Checker(false);
				    chk.addRaw(g);
				    assertEquals(chk.getSubLanguage(),"Full");
				    
				}
			}
		}
		WGTests.logResult(uri, true);
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