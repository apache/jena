/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.test;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.MalformedURIException;
import com.hp.hpl.jena.rdf.arp.DOM2Model;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.*;

/**
 * @author Jeremy J. Carroll
 *
 */
class DOM2RDFTest extends SAX2RDFTest {

	/**
	 * @param dir
	 * @param base0
	 * @param file
	 */
	public DOM2RDFTest(String dir, String base0, String file) {
		super(dir, base0, file);
	}
	
	static private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // DOM must have namespace information inside it!
	static { factory.setNamespaceAware(true);}
	static private DocumentBuilder domParser;
	
	static {
		try {
		domParser = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException rte){
			throw new JenaException(rte);
		}
	}
	

	void loadXMLModel(Model m2, InputStream in, RDFEHArray eh2) throws MalformedURIException, SAXException, IOException {
		
		Document document = domParser
				.parse(in,base);
			
		// Make DOM into transformer input
		Source input = new DOMSource(document);
        DOM2Model d2m = new DOM2Model(base,m2);	

		d2m.setErrorHandler(eh2);
		
		try {
			try {
		        d2m.load(document);
			} finally {
				d2m.close();
			}
		} catch (SAXParseException e) {
			// already reported, leave it be.
		}
		

	}

}


/*
 *  (c) Copyright 2004 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
