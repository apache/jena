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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.TestCase;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdfxml.xmlinput.SAX2Model ;
import com.hp.hpl.jena.rdfxml.xmlinput.SAX2RDF ;
import com.hp.hpl.jena.rdfxml.xmlinput.SAX2RDFTest.RDFEHArray ;

public class SAX2RDFMoreTests extends TestCase {
	public SAX2RDFMoreTests(String nm) {
		super(nm);
	}

	public void testLang() throws Exception {
	    Model m = ModelFactory.createDefaultModel();
	    Model m2 = ModelFactory.createDefaultModel();
	    RDFEHArray eh = new RDFEHArray();
	    try ( InputStream in = new FileInputStream("testing/wg/rdfms-xmllang/test004.rdf") ) {
	        RDFReader w = m.getReader();
	        w.setErrorHandler(eh);
	        w.read(m, in, "http://example.org/");
	    }

	    RDFEHArray eh2 = new RDFEHArray();

	    try ( InputStream in = new FileInputStream("testing/wg/rdfms-xmllang/test003.rdf") ) {
	        XMLReader saxParser = new SAXParser();
	        SAX2Model handler = SAX2Model.create("http://example.org/", m2,
	            "fr");
	        SAX2RDF.installHandlers(saxParser, handler);
	        handler.setErrorHandler(eh2);

	        InputSource ins = new InputSource(in);
	        ins.setSystemId("http://example.org/");
	        try {
	            try {
	                saxParser.parse(ins);
	            } finally {
	                handler.close();
	            }
	        } catch (SAXParseException e) {
	            // already reported, leave it be.
	        }
	    }
		/*
		 * System.out.println("Normal:"); m.write(System.out,"N-TRIPLE");
		 * 
		 * System.out.println("New:"); m2.write(System.out,"N-TRIPLE");
		 */
		if (eh.v.size() == 0)
			assertTrue("Not isomorphic", m.isIsomorphicWith(m2));
		/*
		 * if ( eh.v.size()!=eh2.v.size()) { for (int i=0; i <a.length;i++)
		 * System.err.println(eh.v.get(i)); }
		 */
		assertEquals("Different number of errors", eh.v.size(), eh2.v.size());

		Object a[] = eh.v.toArray();
		Object a2[] = eh2.v.toArray();
		Arrays.sort(a);
		Arrays.sort(a2);

		for (int i = 0; i < eh.v.size(); i++) {
			assertEquals("Error " + i + " different.", a[i], a2[i]);
		}

	}

}
