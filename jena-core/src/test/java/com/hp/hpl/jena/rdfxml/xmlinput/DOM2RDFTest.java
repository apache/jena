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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdfxml.xmlinput.DOM2Model ;
import com.hp.hpl.jena.shared.JenaException;

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
	

	@Override
    void loadXMLModel(Model m2, InputStream in, RDFEHArray eh2) throws SAXException, IOException {
		
		Document document = domParser
				.parse(in,base);
			
		// Make DOM into transformer input
//		Source input = new DOMSource(document);
        DOM2Model d2m = DOM2Model.createD2M(base,m2);	

		d2m.setErrorHandler(eh2);
		
//		try {
			try {
		        d2m.load(document);
			} finally {
				d2m.close();
			}
//		} catch (SAXParseException e) {
//			// already reported, leave it be.
//		}
		

	}

}
