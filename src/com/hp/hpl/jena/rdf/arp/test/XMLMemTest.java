/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP [See end of
 * file] $Id: XMLMemTest.java,v 1.4 2005-02-21 12:11:26 andy_seaborne Exp $
 */
package com.hp.hpl.jena.rdf.arp.test;

import org.xml.sax.*;
import org.xml.sax.ContentHandler;

import org.apache.xerces.parsers.*;
import java.net.*;

/**
 * The purpose of this class is to be a minimal example which exercises the
 * memory leak problems reported in "Streaming OWL DL" by Jeremy Carroll.
 * 
 * I don't believe it is though ...
 * 
 * @author jjc
 * 
 *  
 */
public class XMLMemTest implements ContentHandler {
	static private class DeliberateEnd extends RuntimeException {

	}
	final private int limit;
	private int seen = 0;
	private int chksum = 0;
	Locator locator;
	private XMLMemTest(int cnt) {
		limit = cnt;
	}
	/**
	 * @param args
	 *            The first arg is a URL of an XML file to read, the second is
	 *            optional, and the test will abort after that number of start
	 *            tags.
	 */

	public static void main(String[] args) {
		XMLMemTest test =
			new XMLMemTest(args.length > 1 ? Integer.parseInt(args[1]) : -1);
		SAXParser sax = new SAXParser();
		sax.setContentHandler(test);
		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		runtime.gc();
		long startMem = runtime.totalMemory() - runtime.freeMemory();
		try {
			for (int i=args.length > 2 ? Integer.parseInt(args[2]) : 1;i>0;i--){
			URL url = new URL(args[0]);

			InputSource inputS = new InputSource(url.openStream());
			inputS.setSystemId(args[0]);
			sax.parse(inputS);
			}
		} catch (DeliberateEnd e) {

		} catch (Exception e) {
			e.printStackTrace();
		}
		runtime.gc();
		runtime.gc();
		long endMem = runtime.totalMemory() - runtime.freeMemory();
		System.err.println("Start: "+startMem);
		System.err.println("End:   "+endMem);
		System.err.println("Used:  "+(endMem-startMem));
		System.err.println("Count: "+test.seen);
		System.err.println("Hash:  "+test.chksum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */

	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
	 *      java.lang.String)
	 */
	public void startPrefixMapping(String prefix, String uri)
		throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(String prefix) throws SAXException {
	}
/*
	private void c(String s) {
		if (s != null)
			chksum ^= s.hashCode();
	}
	*/
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes atts)
		throws SAXException {
		if (++seen == limit)
			throw new DeliberateEnd();
		locator.getLineNumber();
		locator.getColumnNumber();
	//	if (true)
	//		return;
	//	c(namespaceURI);
	//	c(localName);
	//	c(qName);
	//	for (int i = 0; i < atts.getLength(); i++) {
		//	c(atts.getLocalName(i));
		//	c(atts.getQName(i));
		//	c(atts.getURI(i));
		//	atts.getIndex(atts.getQName(i));
		//	atts.getIndex(atts.getURI(i),atts.getLocalName(i));
	//	}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName)
		throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
		throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
		throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 *      java.lang.String)
	 */
	public void processingInstruction(String target, String data)
		throws SAXException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(String name) throws SAXException {
	}
}

/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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