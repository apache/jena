/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.rdf.arp.SAX2Model;
import com.hp.hpl.jena.rdf.arp.SAX2RDF;
import com.hp.hpl.jena.rdf.arp.test.SAX2RDFTest.RDFEHArray;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;

import junit.framework.TestCase;

/**
 * @author Jeremy J. Carroll
 *  
 */
public class SAX2RDFMoreTests extends TestCase {
	public SAX2RDFMoreTests(String nm) {
		super(nm);
	}

	public void testLang() throws Exception {
		Model m = ModelFactory.createDefaultModel();
		Model m2 = ModelFactory.createDefaultModel();
		InputStream in = new FileInputStream(
				"testing/wg/rdfms-xmllang/test004.rdf");
		RDFEHArray eh = new RDFEHArray();
		RDFReader w = m.getReader();
		w.setErrorHandler(eh);
		w.read(m, in, "http://example.org/");
		in.close();
		in = new FileInputStream("testing/wg/rdfms-xmllang/test003.rdf");

		RDFEHArray eh2 = new RDFEHArray();

		XMLReader saxParser = new SAXParser();
		SAX2Model handler = SAX2Model.newInstance("http://example.org/", m2,
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

		in.close();
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

/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

