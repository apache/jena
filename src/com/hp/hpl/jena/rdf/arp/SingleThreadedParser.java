/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UTFDataFormatException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.util.EncodingMap;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
import org.xml.sax.*;


/**
 * 
 * This parser uses the Xerces pull parser configuration,
 * and runs in a single thread. Hence it is preferred over
 * the SAX2RDF parser, which needs two threads.
 * @author Jeremy J. Carroll
 *
 */
class SingleThreadedParser extends XMLHandler {

	private XMLPullParserConfiguration pullParser;
	private SAXParser saxParser;
	private String readerXMLEncoding = null;
	private String xmlEncoding = null;
	private SingleThreadedParser(SAXParser rdr, XMLPullParserConfiguration config) {
		super();
		pullParser = config;
		saxParser = rdr;
		try {
		SAX2RDF.installHandlers(rdr,this);
		}
		catch (SAXException e){
			throw new RuntimeException(e);
		}
	//	setErrorHandler(new DefaultErrorHandler());
	}
	
	SAXParser getSAXParser() {
		return saxParser;
	}

	static private class MySAXParser extends SAXParser {
		MySAXParser(StandardParserConfiguration c) {
			super(c);
			try {
			setFeature("http://xml.org/sax/features/string-interning",false);
			}
			catch (SAXException e){
				// Not supported - aggh
				// TODO ask on xerces list why not?
			//	e.printStackTrace();
			}
		}
		SingleThreadedParser a;
		public void xmlDecl(
			String version,
			String encoding,
			String standalone,
			Augmentations augs)  {
			a.setEncoding(encoding == null ? "UTF" : encoding);
			super.xmlDecl(version, encoding, standalone, augs);
		}
		/*
		 * public void startDocument(XMLLocator locator, java.lang.String
		 * encoding, NamespaceContext namespaceContext, Augmentations augs) {
		 * a.setEncoding(encoding);
		 * super.startDocument(locator,encoding,namespaceContext,augs); }
		 *  
		 */
	}
	static SingleThreadedParser create() {
		StandardParserConfiguration c = new StandardParserConfiguration();
		MySAXParser msp = new MySAXParser(c);
		SingleThreadedParser a = new SingleThreadedParser(msp, c);
		msp.a = a;
		return a;
	}
	boolean parseSome() {
		try {
			return pullParser.parse(false);
		} catch (UTFDataFormatException e) {
			try {
				generalError(ERR_UTF_ENCODING, e);
			} catch (SAXParseException e1) {
				// e1.printStackTrace();
			}
			return false;
		} catch (IOException e) {
			try {
				generalError(ERR_GENERIC_IO, e);
			} catch (SAXParseException e1) {
				// e1.printStackTrace();
			}
			return false;
		} catch (FatalParsingErrorException e) {
			return false;
		}
	}

	synchronized public void parse(InputSource input)
		throws IOException, SAXException {
		parse(input, input.getSystemId());
	}
	synchronized public void parse(InputSource input, String base)
		throws IOException, SAXException {
		// Make sure we have a sane state for
		// Namespace processing.
		initParse(base);
		// Start the RDFParser
		pipe = new PullingTokenPipe(this);
		pullParser.setInputSource(convert(input));

		SAX2RDF.installHandlers(saxParser,this);
		saxParser.reset();

		// initEncodingChecks();
		try {
			try {
				RDFParser p = new RDFParser(pipe, SingleThreadedParser.this);
				if (getOptions().getEmbedding())
					p.embeddedFile(documentContext);
				else
					p.rdfFile(documentContext);
			} catch (WrappedException wrapped) {
				wrapped.throwMe();
			} catch (ParseException parse) {
				// This has already been reported.
				// Don't overdo it.
				//throw parse.rootCause();
				
			}
		} finally {
			endBnodeScope();
		}

	}

	XMLInputSource convert(InputSource in) {
		Reader rdr = in.getCharacterStream();
		InputStream str = in.getByteStream();
		String publicID = in.getPublicId();
		String systemID = in.getSystemId();
		readerXMLEncoding = null;
		encodingProblems = false;
		if (rdr == null && str == null) {
			return new XMLInputSource(publicID, systemID, systemID);
		} else if (rdr == null) {
			return new XMLInputSource(publicID, systemID, systemID, str, null);
		} else if (str == null) {
			if (rdr instanceof InputStreamReader) {
				String enc = ((InputStreamReader) rdr).getEncoding();
				readerXMLEncoding = EncodingMap.getJava2IANAMapping(enc);
				if (readerXMLEncoding == null)
					readerXMLEncoding = enc;
				//     System.err.println("readerXMLEncoding = " +
				// readerXMLEncoding);
			}
			return new XMLInputSource(publicID, systemID, systemID, rdr, null);
		}
		return null;
	}

	void setEncoding(String e){
		e = e.toUpperCase();
		//  System.err.println("xmlEncoding = " + e);
		if (e != null && xmlEncoding == null) {
			// special case UTF-8 or UTF-16?
			if (e.equals("UTF")
				&& readerXMLEncoding != null
				&& readerXMLEncoding.startsWith("UTF")) {
				xmlEncoding = readerXMLEncoding;
				return;
			}
			xmlEncoding = e;
			if (readerXMLEncoding != null && !readerXMLEncoding.equals(e)) {
				try {
					this.putWarning(
						WARN_ENCODING_MISMATCH,
						new Location(locator),
						"Encoding on InputStreamReader or FileReader does not match that of XML document. Use FileInputStream. ["
							+ readerXMLEncoding
							+ " != "
							+ e
							+ "]");
				} catch (SAXParseException e1) {
					//e1.printStackTrace();
				}
				encodingProblems = true;
				/*
				 * if ((readerXMLEncoding.indexOf("IBM") != -1) !=
				 * (xmlEncoding.indexOf("IBM") != -1)) { this.putWarning(
				 * ERR_ENCODING_MISMATCH, new Location(locator), "IBM encodings
				 * may be wholly incompatible with non-IBM encodings."); }
				 */
			}
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
 
