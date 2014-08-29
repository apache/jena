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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdfxml.xmlinput.ARPErrorNumbers ;
import com.hp.hpl.jena.rdfxml.xmlinput.NTriple ;
import com.hp.hpl.jena.rdfxml.xmlinput.ParseException ;

public class TaintingTests extends TestCase implements ErrorHandler,
		ARPErrorNumbers {
	 
    static String promoteWarnings[] =
    {
        "WARN_UNQUALIFIED_ATTRIBUTE", 
        "WARN_UNKNOWN_RDF_ATTRIBUTE",
        "WARN_REDEFINITION_OF_ID",
        "WARN_UNKNOWN_PARSETYPE",
        "WARN_MALFORMED_URI",
        "WARN_BAD_NAME",
        "WARN_RELATIVE_URI",
        "WARN_BAD_XMLLANG",
    };
    static String files[] = {
        
        "testing/arp/syntax-errors/error001.rdf",
        "testing/arp/syntax-errors/error002.rdf",
        "testing/arp/syntax-errors/error003.rdf",
        "testing/wg/rdf-charmod-literals/error001.rdf",
        "testing/wg/rdf-charmod-literals/error002.rdf",
        "testing/wg/rdf-charmod-uris/error001.rdf",
        "testing/wg/rdf-containers-syntax-vs-schema/error001.rdf",
        "testing/wg/rdf-containers-syntax-vs-schema/error002.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0001.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0002.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0003.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0004.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0005.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0006.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0007.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0008.rdf",
        "testing/wg/rdf-ns-prefix-confusion/error0009.rdf",
        
        "testing/wg/rdfms-abouteach/error001.rdf",
        "testing/wg/rdfms-abouteach/error002.rdf",
        "testing/wg/rdfms-difference-between-ID-and-about/error1.rdf",
        "testing/wg/rdfms-empty-property-elements/error001.rdf",
        "testing/wg/rdfms-empty-property-elements/error002.rdf",
        "testing/wg/rdfms-empty-property-elements/error003.rdf",
        "testing/wg/rdfms-parseType/error001.rdf",
        "testing/wg/rdfms-parseType/error002.rdf",
        "testing/wg/rdfms-parseType/error003.rdf",
        "testing/wg/rdfms-rdf-id/error001.rdf",
        "testing/wg/rdfms-rdf-id/error002.rdf",
        "testing/wg/rdfms-rdf-id/error003.rdf",
        "testing/wg/rdfms-rdf-id/error004.rdf",
        "testing/wg/rdfms-rdf-id/error005.rdf",
        "testing/wg/rdfms-rdf-id/error006.rdf",
        "testing/wg/rdfms-rdf-id/error007.rdf",
        "testing/wg/rdfms-rdf-names-use/error-001.rdf",
        "testing/wg/rdfms-rdf-names-use/error-002.rdf",
        "testing/wg/rdfms-rdf-names-use/error-003.rdf",
        "testing/wg/rdfms-rdf-names-use/error-004.rdf",
        "testing/wg/rdfms-rdf-names-use/error-005.rdf",
        "testing/wg/rdfms-rdf-names-use/error-006.rdf",
        "testing/wg/rdfms-rdf-names-use/error-007.rdf",
        "testing/wg/rdfms-rdf-names-use/error-008.rdf",
        "testing/wg/rdfms-rdf-names-use/error-009.rdf",
        "testing/wg/rdfms-rdf-names-use/error-010.rdf",
        "testing/wg/rdfms-rdf-names-use/error-011.rdf",
        "testing/wg/rdfms-rdf-names-use/error-012.rdf",
        "testing/wg/rdfms-rdf-names-use/error-013.rdf",
        "testing/wg/rdfms-rdf-names-use/error-014.rdf",
        "testing/wg/rdfms-rdf-names-use/error-015.rdf",
        "testing/wg/rdfms-rdf-names-use/error-016.rdf",
        "testing/wg/rdfms-rdf-names-use/error-017.rdf",
        "testing/wg/rdfms-rdf-names-use/error-018.rdf",
        "testing/wg/rdfms-rdf-names-use/error-019.rdf",
        "testing/wg/rdfms-rdf-names-use/error-020.rdf",
        "testing/wg/rdfms-syntax-incomplete/error001.rdf",
        "testing/wg/rdfms-syntax-incomplete/error002.rdf",
        "testing/wg/rdfms-syntax-incomplete/error003.rdf",
        "testing/wg/rdfms-syntax-incomplete/error004.rdf",
        "testing/wg/rdfms-syntax-incomplete/error005.rdf",
        "testing/wg/rdfms-syntax-incomplete/error006.rdf",
        "testing/wg/xmlbase/error001.rdf",
        
        "testing/arp/tainting/base.rdf",
        
        "testing/arp/tainting/lang.rdf",
        "testing/arp/tainting/ptUnknown.rdf",
        "testing/arp/tainting/typedLiteral.rdf",
        
        "testing/arp/tainting/typedNode.rdf",
        "testing/arp/tainting/property.rdf",
        
        "testing/arp/tainting/collection.rdf",
        

        "testing/arp/tainting/propValueA.rdf",
        "testing/arp/tainting/propValueB.rdf",
        "testing/arp/tainting/propValueC.rdf",
        "testing/arp/tainting/propValueD.rdf",
        "testing/arp/tainting/propValueE.rdf",
        

    };

	static public Test suite() {
		TestSuite suite = new TestSuite("ARP Tainting");
        for ( String file : files )
        {
            suite.addTest( new TaintingTests( file ) );
        }
        suite.addTest(new TaintingTests("testing/arp/tainting/base.rdf",
                badBase,
                "testing/arp/tainting/base-with-bad-base-good.nt",
                "testing/arp/tainting/base-with-bad-base-bad.nt"));
		return suite;
	}
    final String fileName;
    final String base;
    final String goodTriples;
    final String badTriples;
    static final String badBase = "http://ww^w/";
    public TaintingTests(String s) {
        this(s,"http://example.org/",makeGood(s),makeBad(s));
    }
    static String makeGood(String s) {
        return s.substring(0,s.length()-4)+"-good.nt";
    }
    static String makeBad(String s) {
        return s.substring(0,s.length()-4)+"-bad.nt";
    }
	public TaintingTests(String s, String b, String good, String bad) {
		super(s.substring(8));
        fileName = s;
        base = b;
        goodTriples = good;
        badTriples = bad;
	}

	protected Model createMemModel() {
		return ModelFactory.createDefaultModel();
	}

	@Override
    public void setUp() {
		// ensure the ont doc manager is in a consistent state
		OntDocumentManager.getInstance().reset(true);
	}

    @Override
    public void runTest() throws IOException {


        ByteArrayOutputStream goodBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream badBytes = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try (PrintStream out = new PrintStream(goodBytes); PrintStream err = new PrintStream(badBytes); ) {
            System.setOut(out);
            System.setErr(err);
            NTriple.mainEh(new String[]{"-e","102,136,105,103,108,107,116,106,004,131",
                "-E","-b",base,fileName},this,null);
        }
        finally {
            System.setErr(oldErr);
            System.setOut(oldOut);
        }
        InputStream good = new ByteArrayInputStream(goodBytes.toByteArray());
        InputStream bad = new ByteArrayInputStream(badBytes.toByteArray());
        compare(good,goodTriples);
        compare(bad,badTriples);
    }

    private void compare(InputStream in, String filen) throws IOException {
        Model m1 = this.createMemModel();
        Model m2 = this.createMemModel();
        m1.read(in,"","N-TRIPLES");
        m2.read(new FileInputStream(filen),"","N-TRIPLES");
        boolean isomorphicWith = m1.isIsomorphicWith(m2);
        if (!isomorphicWith) {
            System.err.println("Found Triples:");
            System.err.println("===");
            m1.write(System.err,"N-TRIPLES");
            System.err.println("===");
            System.err.println("Expected Triples:");
            System.err.println("===");
            m2.write(System.err,"N-TRIPLES");
            System.err.println("===");
        }
        assertTrue("Triples were not as expected.",isomorphicWith);
    }
	
	
    static public boolean seen[] = new boolean[400];
	@Override
    public void warning(SAXParseException e) {
        int eNo =((ParseException)e).getErrorNumber();
        if (!seen[eNo]) {
        seen[((ParseException)e).getErrorNumber()] = true;
        System.out.print(eNo+", ");
        System.err.println(e.getMessage());
        }
	}

	@Override
    public void error(SAXParseException e) {
	}

	@Override
    public void fatalError(SAXParseException e) {
	}

}
