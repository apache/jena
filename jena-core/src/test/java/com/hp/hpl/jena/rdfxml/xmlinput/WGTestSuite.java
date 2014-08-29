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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xml.sax.SAXException;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.JenaReader ;
import com.hp.hpl.jena.rdfxml.xmlinput.ParseException ;
import com.hp.hpl.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import com.hp.hpl.jena.reasoner.test.WGReasonerTester;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.shared.wg.TestInputStreamFactory;
import com.hp.hpl.jena.vocabulary.OWLResults;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
class WGTestSuite extends TestSuite implements ARPErrorNumbers {
	static private Resource jena2;
	static private Model testResults;
	static private void initResults() {
		logging = true;
		testResults = ModelFactory.createDefaultModel();
		jena2 = testResults.createResource(BASE_RESULTS_URI + "#jena2");
		jena2.addProperty(RDFS.comment, 
			testResults.createLiteral(
				"<a xmlns=\"http://www.w3.org/1999/xhtml\" href=\"http://jena.sourceforce.net/\">Jena2</a> is a" +
				" Semantic Web framework in Java" +
				" available from <a xmlns=\"http://www.w3.org/1999/xhtml\" href=\"http://www.sourceforce.net/projects/jena\">" +
				"sourceforge</a> CVS.",
				true)
		);
		jena2.addProperty(RDFS.label, "Jena2");
		testResults.setNsPrefix("results", OWLResults.NS);
	}
	static void logResult(Resource test, int type) {
//		if (!logging) return;
//		Resource rslt;
//		switch (type) {
//			case WGReasonerTester.NOT_APPLICABLE:
//			return;
//			case WGReasonerTester.FAIL:
//			rslt = OWLResults.FailingRun;
//			break;
//			case WGReasonerTester.PASS:
//			rslt = OWLResults.PassingRun;
//			break;
//			case WGReasonerTester.INCOMPLETE:
//			  rslt = OWLResults.IncompleteRun;
//			  break;
//			default:
//			throw new BrokenException("Unknown result type");
//		}
//		Resource result =
//			testResults
//				.createResource()
//				.addProperty(RDF.type, OWLResults.TestRun)
//				.addProperty(RDF.type, rslt)
//				.addProperty(OWLResults.test, test )
//				.addProperty(OWLResults.system, jena2);
	}
	private static boolean logging = false;
	private static String BASE_RESULTS_URI = "http://jena.sourceforge.net/data/rdf-results.rdf";
    static public boolean checkMessages = false;
    static private boolean doSemanticTests() {
    	return ARPTests.internet;
    }
    static private boolean inDevelopment = false;
     Model loadRDF(InFactoryX in, RDFErrorHandler eh, String base)
        throws IOException {
        Model model = ModelFactory.createDefaultModel();
        JenaReader jr = new JenaReader();

        if (eh != null)
            jr.setErrorHandler(eh);
        jr.setProperty("error-mode", "strict");
        
        if ( base.contains( "/xmlns/" )
          || base.contains( "/comments/" ) )
              jr.setProperty("embedding","true");
        try ( InputStream inx = in.open() ) {
            jr.read(model, inx, base);
        }
        return model;
    }
    
    static Model loadNT(InputStream in, String base) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        model.read(in, base, "N-TRIPLE");
        in.close();
        return model;
    }

	static private class DummyTest extends TestCase {
		DummyTest() {
			super("save results");
		}
		@Override
        public void runTest()  throws IOException {
			if (logging) {	    
			    RDFWriter w = testResults.getWriter("RDF/XML-ABBREV");
			    w.setProperty("xmlbase",BASE_RESULTS_URI );
			    try ( OutputStream out = new FileOutputStream("/tmp/rdf-results.rdf") ) {
			        w.write(testResults,out,BASE_RESULTS_URI);
			    }
			}
		}
	}
    static String testNS =
        "http://www.w3.org/2000/10/rdf-tests/rdfcore/testSchema#";
        
    static String jjcNS = "http://jcarroll.hpl.hp.com/testSchema#";
    
 //   static private String approved = "APPROVED";
    static private Property status;
    static private Property input;
    static private Property output;
    static private Property warning;
    static private Property errorCodes;
    
    static {
            status = new PropertyImpl(testNS, "status");
            input = new PropertyImpl(testNS, "inputDocument");
            output = new PropertyImpl(testNS, "outputDocument");
            warning = new PropertyImpl(testNS, "warning");
            errorCodes = new PropertyImpl(jjcNS, "error");
    }
    
    static private Resource rdfxml =
        new ResourceImpl(testNS, "RDF-XML-Document");
        
    static private Resource ntriple = new ResourceImpl(testNS, "NT-Document");
	//  static private Resource falseDoc = new ResourceImpl(testNS, "False-Document");

    private IRI testDir;
    
    private Act noop = new Act() {
        @Override
        public void act(Resource r) {
        }
    };
    
    private Act semTest = new Act() {
		  @Override
        public void act(Resource r) {
		  	if (doSemanticTests()){
//		  		addTest(r, new ReasoningTest(r));
		  	}
		  }
    };
    
    TestInputStreamFactory factory;
    
    static private Collection<String> misc =
        Arrays.asList(
            new String[] { "http://www.w3.org/2000/10/rdf-tests/rdfcore/rdfms-uri-substructure/error001" });
            
    private Map<ResourceImpl, Act> behaviours = new HashMap<>();
    
    {
        behaviours
            .put(new ResourceImpl(testNS + "PositiveParserTest"), new Act() {
            @Override
            public void act(Resource r)  {
                //		if (r.getProperty(status).getString().equals(approved))
                //  if (r.getURI().endsWith("rdfms-xmllang/test004"))
                if (r.hasProperty(warning)) {
                    addTest(r, new WarningTest(r));
                } else {
                    addTest(r, new PositiveTest(r));
                }
            }
        });
        behaviours
            .put(new ResourceImpl(testNS + "NegativeParserTest"), new Act() {
            @Override
            public void act(Resource r)  {
                //		if (r.getProperty(status).getString().equals(approved))
                addTest(r, new NegativeTest(r));
            }
        });
		    behaviours.put(new ResourceImpl(testNS + "False-Document"), noop);
        behaviours.put(new ResourceImpl(testNS + "RDF-XML-Document"), noop);
        behaviours.put(new ResourceImpl(testNS + "NT-Document"), noop);
        behaviours.put(
            new ResourceImpl(testNS + "PositiveEntailmentTest"),
            semTest);
        behaviours.put(
            new ResourceImpl(testNS + "NegativeEntailmentTest"),
		        semTest);
        behaviours
            .put(new ResourceImpl(testNS + "MiscellaneousTest"), new Act() {
            @Override
            public void act(Resource r) {
                String uri = r.getURI();
                if (!misc.contains(uri))
                    System.err.println(
                        "MiscellaneousTest: " + uri + " - ignored!");
            }
        });
    }

    private Model loadRDF(final TestInputStreamFactory fact, 
      final String file) {
        Model m = null;
        String base = fact.getBase().toString();
        if (!base.endsWith("/"))
            base = base + "/";

        try ( InputStream in = fact.fullyOpen(file) ) {
            if (in == null )
                return null;
            m = loadRDF(new InFactoryX(){
                @Override
                public InputStream open() throws IOException {
                    return fact.fullyOpen(file);
                } }, null, base + file);
        } catch (JenaException e) {
            //	System.out.println(e.getMessage());
            throw e;
        } catch (Exception e) {
            //	e.printStackTrace();
            if (file.equals("Manifest.rdf")) {
                System.err.println("Failed to open Manifest.rdf");
                e.printStackTrace();
            }
        }
        return m;
    }

    /** Creates new WGTestSuite
        This is a private snapshot of the RDF Test Cases Working Draft's
        data.
     */
    String createMe;
    
    WGTestSuite(TestInputStreamFactory fact, String name, boolean dynamic) {
        super(name);
        factory = fact;
        testDir = fact.getBase();
        if (dynamic)
            try {
//            	String wgDir = ARPTests.wgTestDir.toString();
            	System.err.println(testDir+", "+fact.getMapBase());
            	  wgReasoner = new WGReasonerTester("Manifest.rdf",
                          "testing/wg/");
//                          wgDir);
                createMe =
                    "new "
                        + this.getClass().getName()
                        + "("
                        + fact.getCreationJava()
                        + ", \""
                        + name
                        + "\", false )";
                Model m = loadRDF(fact, "Manifest.rdf");
                //	System.out.println("OK2");
                Model extra = loadRDF(fact, "Manifest-extra.rdf");
                //	System.out.println("OK3");
                Model wrong = loadRDF(fact, "Manifest-wrong.rdf");
                //	System.out.println("OK4");

                if (extra != null)
                    m = m.add(extra);
                if (wrong != null)
                    m = m.difference(wrong);
                //	if (m == null)
                //		System.out.println("uggh");
                StmtIterator si =
                    m.listStatements( null, RDF.type, (RDFNode) null );

                while (si.hasNext()) {
                    Statement st = si.nextStatement();
                    Act action = behaviours.get(st.getObject());
                    if (action == null) {
                        System.err.println(
                            "Unknown test class: "
                                + ((Resource) st.getObject()).getURI());
                    } else {
                        action.act(st.getSubject());
                    }
                }
                if ( ARPTests.internet) {
                	initResults();
                	addTest(new DummyTest());
                }
            } catch (RuntimeException re) {
                re.printStackTrace();
                throw re;
            } catch (Exception e) {
                e.printStackTrace();
                throw new JenaException( e );

            }
    }
    
   // private ZipFile zip;
    
    static TestSuite suite(IRI testDir, String d, String nm) {
        return new WGTestSuite(
            new TestInputStreamFactory(testDir, d),
            nm,
            true);
    }

    static TestSuite suite(IRI testDir, IRI d, String nm) {
        return new WGTestSuite(
            new TestInputStreamFactory(testDir, d),
            nm,
            true);
    }

    private Map<String, TestSuite> parts = new HashMap<>();
    
    private void addTest(Resource key, TestCase test)  {
        String keyName =
            key.hasProperty(status)
                ? key.getRequiredProperty(status).getString()
                : "no status";
        TestSuite sub = parts.get(keyName);
        if (sub == null) {
            if ( keyName.equals("OBSOLETED"))
              return;
			      if ( keyName.equals("OBSOLETE"))
			        return;
			      if ( keyName.equals("NOT_APPROVED"))
			        return;
            sub = new TestSuite();
            sub.setName(keyName);
            parts.put(keyName, sub);
            addTest(sub);
        }
        sub.addTest(test);
    }

    final static String errorLevelName[] =
        new String[] { "warning", "error", "fatal" };
        
    interface Act {
        void act(Resource r) ;
    }
    private WGReasonerTester wgReasoner;
    class ReasoningTest extends Test {
    	 ReasoningTest(Resource r) {
    	 	super(r);
    	 }
	 @Override
    protected void runTest() throws IOException {
	       int rslt = WGReasonerTester.FAIL;
	       try {
                   JenaParameters.enableWhitespaceCheckingOfTypedLiterals = true;
                    Resource config = ModelFactory.createDefaultModel().createResource()
                         .addProperty(ReasonerVocabulary.PROPsetRDFSLevel, "full");
	            rslt = wgReasoner.runTestDetailedResponse(testID.getURI(),
	            RDFSRuleReasonerFactory.theInstance(),this,config);
                }  finally {
                    logResult(testID,rslt);
	        }
			// assertTrue(rslt>=0);
	}
	/* (non-Javadoc)
         * @see com.hp.hpl.jena.rdf.arp.test.WGTestSuite.Test#createMe()
	 */
	@Override
    String createMe() {
		throw new UnsupportedOperationException();
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.test.WGTestSuite.Test#reallyRunTest()
	 */
	@Override
    void reallyRunTest() {
		throw new BrokenException("");
	}
    	 
    }
    
    abstract class Test extends TestCase implements RDFErrorHandler {
        Resource testID;
        String createURI() {
            return "\"" + testID.getURI() + "\"";
        }
        abstract String createMe();
        Test(Resource r) {
            super(
                WGTestSuite
                    .this
                    .testDir
                    .relativize(IRIFactory.iriImplementation().create(r.getURI()),
                            IRI.CHILD)
                    .toString());
            testID = r;
        }
        String create(Property p) {
            Resource file = testID.getRequiredProperty(p).getResource();
            Resource t = file.getRequiredProperty(RDF.type).getResource();
            if (ntriple.equals(t)) {
                return "\"" + file.getURI() + "\",false";
            } else if (rdfxml.equals(t)) {
                return "\"" + file.getURI() + "\",true";
            } else {
                return "Unrecognized file type: " + t;
            }
        }
        Model read(Property p) throws IOException {
            Resource file = testID.getRequiredProperty(p).getResource();
            Resource t = file.getRequiredProperty(RDF.type).getResource();
            final String uri = file.getURI();
            if (ntriple.equals(t)) {
                return loadNT(factory.open(uri),uri);
            } else if (rdfxml.equals(t)) {
                return loadRDF(
                new InFactoryX(){

					@Override
                    public InputStream open() throws IOException {
						return factory.open(uri);
					}
                }
                , this, uri);
            } else {
                fail("Unrecognized file type: " + t);
            }
            return null;
        }
        @Override
        protected void runTest()  throws IOException {
        	int rslt = WGReasonerTester.FAIL;
        	try {
        		reallyRunTest();
        		rslt = WGReasonerTester.PASS;
        	}
        	finally {
        		logResult(testID,rslt);
        	}
        }
        abstract void reallyRunTest();
        @Override
        public void warning(Exception e) {
            error(0, e);
        }
        @Override
        public void error(Exception e) {
            error(1, e);
        }
        @Override
        public void fatalError(Exception e) {
            error(2, e);
        }
        private void error(int level, Exception e) {
            //		println(e.getMessage());
            if (e instanceof ParseException) {
                int eCode = ((ParseException) e).getErrorNumber();
                if (eCode == ERR_SYNTAX_ERROR) {
                    String msg = e.getMessage();
                    if ( msg.contains( "Unusual" )
                        || msg.contains( "Internal" ) ) {
                        System.err.println(testID.getURI());
                        System.err.println(msg);
                        fail(msg);
                    }
                    if (checkMessages) {
                        System.err.println(testID.getURI());
                        System.err.println(msg);
                    }
                }
                onError(level, eCode);
            }
            /*else if (e instanceof SAXParseException) {
                onError(level, ARPErrorNumbers.WARN_BAD_XML);
            } */
            else if (e instanceof SAXException) {
                fail("Not expecting a SAXException: " + e.getMessage());
            } else {
                fail("Not expecting an Exception: " + e.getMessage());
            }
        }

        private void println(String m) {
            System.err.println(m);
        }
        void onError(int level, int num) {
            String msg =
                "Parser reports unexpected "
                    + errorLevelName[level]
                    + ": "
                    + ParseException.errorCodeName(num);
            println(msg);
            fail(msg);
        }
    }
    
    class PositiveTest extends NegativeTest {
        @Override
        String createMe() {
            return createURI() + "," + create(input) + "," + create(output);
        }
        PositiveTest(Resource nm)  {
            super(nm);
            expectedLevel = -1;
        }
        @Override
        protected void reallyRunTest() {
            try {
                Model m2 = read(output);
                super.reallyRunTest();
                if (!m1.equals(m2)) {
                    save(output);
                    assertTrue(m1.isIsomorphicWith( m2 ) );
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        @Override
        void initExpected()  {
            expected = new HashSet<>();
        }
    }
    
    class WarningTest extends PositiveTest {
        @Override
        String createMe() {
            return createURI()
                + ","
                + create(input)
                + ","
                + create(output)
                + ","
                + createExpected();
        }
        WarningTest(Resource nm)  {
            super(nm);
            expectedLevel = 0;
        }
        @Override
        void initExpected()  {
            initExpectedFromModel();
        }
    }
    
    class NegativeTest extends Test {
        Model m1;
        Set<Integer> expected;
        int expectedLevel = 1;
        private Set<Integer> found = new HashSet<>();
        private int errorCnt[] = new int[] { 0, 0, 0 };
        String createExpected() {
            String rslt = "new int[]{";
            if ( expected == null)
               return "null";
            Iterator<Integer> it = expected.iterator();
            while (it.hasNext())
                rslt += it.next() + ", ";
            return rslt + "}";
        }
        @Override
        String createMe() {
            return createURI() + "," + create(input) + "," + createExpected();
        }
        NegativeTest(Resource nm)  {
            super(nm);
            initExpected();
        }
        void save(Property p)  {
            if (factory.savable()) {
                String uri = testID.getRequiredProperty(p).getResource().getURI();
                int suffix = uri.lastIndexOf('.');
                String saveUri = uri.substring(0, suffix) + ".ntx";
                //   System.out.println("Saving to " + saveUri);
                try ( OutputStream w = factory.openOutput(saveUri) ) {
                    m1.write(w, "N-TRIPLE");
                } catch (IOException e) {
                    throw new JenaException( e );
                }
            }
        }
        void initExpectedFromModel()  {
            StmtIterator si = testID.listProperties(errorCodes);
            if (si.hasNext()) {
                expected = new HashSet<>();
                while (si.hasNext()) {
                    String uri = si.nextStatement().getResource().getURI();
                    String fieldName = uri.substring(uri.lastIndexOf('#') + 1);
                    expected.add(new Integer(ParseException.errorCode(fieldName)));
                }
            }
        }
        void initExpected()  {
            initExpectedFromModel();
        }
        @Override
        protected void reallyRunTest() {
            try {
                m1 = read(input);

                if (expectedLevel == 1
                    && expected == null
                    && errorCnt[2] == 0
                    && errorCnt[1] == 0)
                    save(input);
            } catch (JenaException re) {
                if (re.getCause() instanceof SAXException) {
                    // ignore.
                } else {
                    fail(re.getMessage());
                }
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
            if (expected != null && !expected.equals(found)) {
                Set<Integer> dup = new HashSet<>();
                dup.addAll(found);
                dup.removeAll(expected);
                expected.removeAll(found);
                Iterator<Integer> it = expected.iterator();
                while (it.hasNext()) {
                    int eCode = it.next().intValue();
                    String msg =
                        "Expected error  "
                            + ParseException.errorCodeName(eCode)
                            + ", was not detected.";
                    if (errorCnt[2] == 0)
                        fail(msg);
                    else if (
                        eCode == ERR_SYNTAX_ERROR
                            && getName().startsWith("rdf-nnn/67_")
                            && "1234".indexOf(
                                getName().charAt("rdf-nnn/67_".length()))
                                != -1) {
                        // ignore
                        //  System.err.println("Last message probably reflects a benign race condition on ARP teardown after.error error that can be ignored.");
                        //  System.err.println("It is known to happen with tests rdf-nnn/67_[1234] and ERR_SYNTAX_ERROR.");

                    } else {
                        System.err.println("Test: " + getName());
                        System.err.println(msg);
                    }
                }
                it = dup.iterator();
                while (it.hasNext())
                    fail(
                        "Detected error  "
                            + ParseException.errorCodeName(
                                it.next().intValue())
                            + ", was not expected.");
            }
            for (int j = 2; j >= 0; j--)
                if (j == expectedLevel) {
                    if (errorCnt[j] == 0 && (j != 1 || errorCnt[2] == 0))
                        fail(
                            "No "
                                + errorLevelName[expectedLevel]
                                + " in input file of class "
                                + getClass().getName());
                } else if (expected == null) {
                    if (errorCnt[j] != 0)
                        fail(
                            "Inappropriate "
                                + errorLevelName[j]
                                + " in input file of class "
                                + getClass().getName());
                }

        }
        @Override
        void onError(int level, int id) {
            Integer err = new Integer(id);
            found.add(err);
            errorCnt[level]++;
            if (expected != null) {
                if (!expected.contains(err))
                    super.onError(level, id);
            } else if (inDevelopment) {
                System.err.println(
                    "<rdf:Description rdf:about='"
                        + testID.getURI()
                        + "'>\n"
                        + "<jjc:error rdf:resource='"
                        + jjcNS
                        + ParseException.errorCodeName(id)
                        + "'/>\n</rdf:Description>");
            }
        }
    }
    class Test2 extends TestCase implements RDFErrorHandler {
        //Resource testID;
        Test2(String r) {
            super(
                WGTestSuite.this.testDir.relativize(r,
                        IRI.CHILD).toString());
            //   testID = r;
        }
        Model read(String file, boolean type) throws IOException {
            if (!type) {
                return loadNT(factory.open(file),file);
            } 
                final String uri = file;
                return loadRDF(
                new InFactoryX(){

					@Override
                    public InputStream open() throws IOException {
						return factory.open(uri);
					}
                }
                
                , this, uri);
            
        }
        
        @Override
        public void warning(Exception e) {
            error(0, e);
        }
        
        @Override
        public void error(Exception e) {
            error(1, e);
        }
        
        @Override
        public void fatalError(Exception e) {
            error(2, e);
        }
        
        private void error(int level, Exception e) {
            //      println(e.getMessage());
            if (e instanceof ParseException) {
                int eCode = ((ParseException) e).getErrorNumber();
                if (eCode == ERR_SYNTAX_ERROR) {
                    String msg = e.getMessage();
                    if ( msg.contains( "Unusual" )
                        || msg.contains( "Internal" ) ) {
                        System.err.println(getName());
                        System.err.println(msg);
                        fail(msg);
                    }
                    /*
                    if (checkMessages) {
                        System.err.println(testID.getURI());
                        System.err.println(msg);
                    }
                    */
                }
                onError(level, eCode);
            } /*else if (e instanceof SAXParseException) {
                onError(level, ARPErrorNumbers.WARN_BAD_XML);
            } */
            else if (e instanceof SAXException) {
                fail("Not expecting a SAXException: " + e.getMessage());
            } else {
                fail("Not expecting an Exception: " + e.getMessage());
            }
        }

        private void println(String m) {
            System.err.println(m);
        }
        void onError(int level, int num) {
            String msg =
                "Parser reports unexpected "
                    + errorLevelName[level]
                    + ": "
                    + ParseException.errorCodeName(num);
            println(msg);
            fail(msg);
        }
    }
    
    class PositiveTest2 extends NegativeTest2 {
        String out;
        boolean outtype;
        PositiveTest2(
            String uri,
            String in,
            boolean intype,
            String out,
            boolean outtype) {
            this(uri, in, intype, out, outtype, new int[] {
            });
        }

        PositiveTest2(
            String uri,
            String in,
            boolean intype,
            String out,
            boolean outtype,
            int errs[]) {
            super(uri, in, intype, errs);
                expectedLevel = -1;
            this.out = out;
            this.outtype = outtype;
        }
        @Override
        protected void runTest() {
            try {
                Model m2 = read(out, outtype);
                super.runTest();
                if (!m1.isIsomorphicWith(m2)) {
                    //  save(output);
                    System.err.println("=====");
                    m1.write(System.err,"N-TRIPLE");
                    System.err.println("=====");
                    m2.write(System.err,"N-TRIPLE");
                    System.err.println("=====");
                    fail("Models were not equal.");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
        void initExpected()  {
            expected = new HashSet<>();
        }
    }
    
    class WarningTest2 extends PositiveTest2 {
        WarningTest2(
            String uri,
            String in,
            boolean intype,
            String out,
            boolean outtype,
            int errs[]) {
            super(uri, in, intype, out, outtype, errs);
                expectedLevel = 0;
        }
    }
    
    class NegativeTest2 extends Test2 {
        Model m1;
        Set<Integer> expected;
        int expectedLevel = 1;
        String in;
        boolean intype;
        private Set<Integer> found = new HashSet<>();
        private int errorCnt[] = new int[] { 0, 0, 0 };
        NegativeTest2(String uri, String in, boolean intype, int errs[]) {
            super(uri);
            this.in = in;
            this.intype = intype;

            initExpected(errs);
        }
        /*
        void save(Property p)  {
            if (factory.savable()) {
                String uri = testID.getProperty(p).getResource().getURI();
                int suffix = uri.lastIndexOf('.');
                String saveUri = uri.substring(0, suffix) + ".ntx";
                //   System.out.println("Saving to " + saveUri);
                try {
                    OutputStream w = factory.openOutput(saveUri);
                    m1.write(w, "N-TRIPLE");
                    w.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        */
        void initExpected(int errs[]) {
            if ( errs == null )
               return;
            if (errs.length != 0)
                expected = new HashSet<>();
            for ( int err : errs )
            {

                expected.add( new Integer( err ) );
            }
        }
        @Override
        protected void runTest() {
            try {
                m1 = read(in, intype);
                /*
                                if (expectedLevel == 1
                                    && expected == null
                                    && errorCnt[2] == 0
                                    && errorCnt[1] == 0)
                                    save(input);
                                    */
            } catch (JenaException re) {
                //   System.out.println(re.toString());
                if (re.getCause() instanceof SAXException) {
                    // ignore.
                } else {
                    fail(re.getMessage());
                }
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
            // Tidy up this code a bit, I don't understand it.
            HashSet<Integer> ex2 = expected==null?null:new HashSet<>(expected);
            if (expected==null)
            for (int j = 2; j >= 0; j--)
                if (j != expectedLevel)  {
                    if (errorCnt[j] != 0)
                        ex2 = new HashSet<>();
                }
            if (ex2 != null && !ex2.equals(found)) {
                Set<Integer> dup = new HashSet<>();
                dup.addAll(found);
                dup.removeAll(ex2);
                ex2.removeAll(found);
                if (expected != null)
                    expected.removeAll(found);
                Iterator<Integer> it = ex2.iterator();
                while (it.hasNext()) {
                    int eCode = it.next().intValue();
                    String msg =
                        "Expected error  "
                            + ParseException.errorCodeName(eCode)
                            + ", was not detected.";
                    if (errorCnt[2] == 0) {
                        fail(msg);
                    } else {
                        System.err.println("Test: " + getName());
                        System.err.println(msg);
                    }
                }
                it = dup.iterator();
                while (it.hasNext())
                    fail(
                        "Detected error  "
                            + ParseException.errorCodeName(
                                it.next().intValue())
                            + ", was not expected.");
            }
            for (int j = 2; j >= 0; j--)
                if (j == expectedLevel) {
                    if (errorCnt[j] == 0 && (j != 1 || errorCnt[2] == 0))
                        fail(
                            "No "
                                + errorLevelName[expectedLevel]
                                + " in input file of class "
                                + getClass().getName());
                } else if (expected == null) {
                    if (errorCnt[j] != 0)
                        fail(
                            "Inappropriate "
                                + errorLevelName[j]
                                + " in input file of class "
                                + getClass().getName());
                }

        }
        @Override
        void onError(int level, int id) {
            Integer err = new Integer(id);
            found.add(err);
            errorCnt[level]++;
            if (expected != null) {
                if (!expected.contains(err))
                    super.onError(level, id);
            }
            /*else if ( inDevelopment ) {
                System.err.println(
                    "<rdf:Description rdf:about='"
                        + testID.getURI()
                        + "'>\n"
                        + "<jjc:error rdf:resource='"
                        + jjcNS
                        + JenaReader.errorCodeName(id)
                        + "'/>\n</rdf:Description>");
            }
            */
        }
    }
    TestCase createPositiveTest(
        String uri,
        String in,
        boolean intype,
        String out,
        boolean outtype) {
        return new PositiveTest2(uri, in, intype, out, outtype);
    }
    TestCase createWarningTest(
        String uri,
        String in,
        boolean intype,
        String out,
        boolean outtype,
        int e[]) {
        return new WarningTest2(uri, in, intype, out, outtype, e);
    }
    TestCase createNegativeTest(
        String uri,
        String in,
        boolean intype,
        int e[]) {
        return new NegativeTest2(uri, in, intype, e);
    }
}
