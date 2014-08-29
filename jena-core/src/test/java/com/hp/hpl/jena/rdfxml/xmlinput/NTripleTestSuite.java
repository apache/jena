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

package com.hp.hpl.jena.rdfxml.xmlinput;
import java.io.* ;
import java.util.HashSet ;
import java.util.Set ;

import junit.framework.TestSuite ;
import org.apache.jena.iri.IRI ;
import org.junit.Assert ;
import org.xml.sax.SAXException ;
import org.xml.sax.SAXParseException ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler ;
import com.hp.hpl.jena.rdfxml.xmlinput.ALiteral ;
import com.hp.hpl.jena.rdfxml.xmlinput.ARPEventHandler ;
import com.hp.hpl.jena.rdfxml.xmlinput.AResource ;
import com.hp.hpl.jena.rdfxml.xmlinput.NTriple ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.ARPResource ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.ARPSaxErrorHandler ;
import com.hp.hpl.jena.shared.wg.TestInputStreamFactory ;

/**
 * A version of the test suite which uses the
 * ARP internal N-triple writer, and not the
 * Jena N-triple writer.
 */
class NTripleTestSuite extends WGTestSuite {
	NTripleTestSuite(TestInputStreamFactory fact, String name, boolean b) {
		super(fact, name, b);
	}

	static TestSuite suite(IRI testDir, String d, String nm) {
		return new NTripleTestSuite(
			new TestInputStreamFactory(testDir, d),
			nm,
			true);
	}

	static TestSuite suite(IRI testDir, IRI d, String nm) {
		return new NTripleTestSuite(
			new TestInputStreamFactory(testDir, d),
			nm,
			true);
	}

	static class SimulatedException extends RuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = -4804213791508445759L;
	}
	static class TestHandler
		extends ARPSaxErrorHandler
		implements ARPEventHandler, org.xml.sax.ErrorHandler {
		TestHandler(RDFErrorHandler eh) {
			this(eh, 0);
		}
		TestHandler(RDFErrorHandler eh, int cnt) {
			super(eh);
			countDown = cnt;
			xCountDown = cnt;
		}
		final int xCountDown;
		Set<AResource> anon = new HashSet<>();
		Set<AResource> oldAnon = new HashSet<>();
		int state = 1; // 1 begin, 2 in RDF, 3 after RDF, 4 at end-of-file.
		int countDown;
		@Override
        public void statement(AResource subj, AResource pred, AResource obj) {
			Assert.assertEquals(state, 2);
			seeing(subj);
			seeing(obj);
			if (--countDown == 0)
				throw new SimulatedException();
		}

		/**
		 * @param subj
		 */
		private void seeing(AResource subj) {
			if (subj.isAnonymous())
				anon.add(subj);
			Assert.assertFalse("bnode reuse?", oldAnon.contains(subj));
		}
		/**
		* @param subj
		*/
		private void seen(AResource subj) {
			if (!anon.contains(subj)) {
                if (ARPResource.DEBUG) {
                    ((RuntimeException)subj.getUserData()).printStackTrace();
                }
                Assert.assertFalse(
                        "end-scope called twice for a bnode: "
                            + subj.getAnonymousID(),
                        oldAnon.contains(subj));
				Assert.assertTrue(
					"end-scope for a bnode that had not been used "
						+ subj.getAnonymousID(),
					anon.contains(subj));
            }
			anon.remove(subj);
			oldAnon.add(subj);
		}

		@Override
        public void statement(AResource subj, AResource pred, ALiteral lit) {
			Assert.assertEquals("no start RDF seen", state, 2);
			seeing(subj);
			if (--countDown == 0)
				throw new SimulatedException();
		}

		@Override
        public void endBNodeScope(AResource bnode) {
			Assert.assertTrue(bnode.isAnonymous());
			switch (state) {
				case 1 :
					Assert.fail("Missing startRDF"); return ;
				case 2 :
					Assert.assertFalse(bnode.hasNodeID());
					seen(bnode);
					break;
				case 3 :
				case 4 :
					Assert.assertTrue(bnode.hasNodeID());
					seen(bnode);
					state = 4;
					break;
				default :
					Assert.fail("impossible - test logic error");
			}

		}

		@Override
        public void startRDF() {
			switch (state) {
				case 2 :
				case 4 :
					Assert.fail("Bad state for startRDF " + state);
			}
			state = 2;
		}

		@Override
        public void endRDF() {
			Assert.assertEquals(state, 2);
			state = 3;
		}

		@Override
        public void startPrefixMapping(String prefix, String uri) {

		}

		@Override
        public void endPrefixMapping(String prefix) {

		}

		/**
		 * 
		 */
		public void atEndOfFile() {
			if (!anon.isEmpty()) {
                for ( AResource a : anon )
                {
                    System.err.print( a.getAnonymousID() + ", " );
                    if ( ARPResource.DEBUG )
                    {
                        RuntimeException rte = (RuntimeException) a.getUserData();
//                        throw rte;
                        rte.printStackTrace();
                    }
                }
			}
			Assert.assertTrue("("+xCountDown+") some bnode still in scope ", //hasErrors||
			anon.isEmpty());
			switch (state) {
				case 1 :
					Assert.fail("end-of-file before anything"); return ;
				case 2 :
					Assert.fail("did not see endRDF"); return ;
				case 3 :
				case 4 :
					break;
				default :
					Assert.fail("impossible logic error in test");
			}
		}
		boolean hasErrors = false;

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
		 */
		@Override
        public void error(SAXParseException exception) throws SAXException {
			hasErrors = true;
			super.error(exception);

		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		@Override
        public void fatalError(SAXParseException exception)
			throws SAXException {
			hasErrors = true;
			super.fatalError(exception);

		}
		/**
		 * 
		 */
		public int getCount() {
			return -countDown;
		}
		/* (non-Javadoc)
		 * @see com.hp.hpl.jena.rdf.arp.ExtendedHandler#discardNodesWithNodeID()
		 */
		@Override
        public boolean discardNodesWithNodeID() {
			return false;
		}

	}

	@Override
    Model loadRDF(InFactoryX in, RDFErrorHandler eh, String base)
		throws IOException {
		return loadRDFx(in, eh, base, true, 0);
	}
	
    @SuppressWarnings("resource")
    static Model loadRDFx(
		InFactoryX in,
		RDFErrorHandler eh,
		String base,
		boolean wantModel,
		int cnt)
		throws IOException {
		InputStream oldIn = System.in;
		InputStream ntIn = null;
		File ntriples = null;

		PrintStream out;
		TestHandler th;
		if (wantModel) {
			ntriples = File.createTempFile("arp", ".nt");
			out = new PrintStream(new FileOutputStream(ntriples));
			th = new TestHandler(eh);
		} else {
			out = new PrintStream(new OutputStream() {

				@Override
                public void write(int b) throws IOException {
				}
			});
			th = new TestHandler(eh, cnt);
		}
		PrintStream oldOut = System.out;
		try {
			System.setIn(in.open());
			System.setOut(out);
			try {
				NTriple.mainEh(new String[] { "-b", base, "-s" }, th, th);
			} catch (SimulatedException e) {
				if (wantModel)
					throw e;
			}
			out.close();
			th.atEndOfFile();

			if (cnt == 0) {
				// retry with sudden death
				for (int i = th.getCount(); i >= 1; i--)
					loadRDFx(in, TestScope.suppress, base, false, i);
			}
			if (wantModel) {
				ntIn = new FileInputStream(ntriples);
				return loadNT(ntIn, base);
			} 
		    return null;
		} finally {
			System.in.close();
			System.setIn(oldIn);
			System.setOut(oldOut);
			if (ntIn != null)
				ntIn.close();
			if (ntriples != null)
				ntriples.delete();
		}
	}

}
