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

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UTFDataFormatException;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.xni.Augmentations;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.FatalParsingErrorException ;
import com.hp.hpl.jena.rdfxml.xmlinput.SAX2RDF ;
import com.hp.hpl.jena.util.CharEncoding;

/**
 * 
 * The main parser, other variants of XMLHandler are for more specialized purposes.
 */
public class RDFXMLParser extends XMLHandler {

    private SAXParser saxParser;

    private String readerXMLEncoding = null;

    private String xmlEncoding = null;

    /**
     * This is protected rather than private to allow subclassing,
     * however, reimplementors should be aware that the default configuration
     * via {@link #create()} includes functionality that is not simply
     * included. The most important is to do with character encoding checking.
     * A common user error is to not have correct XML encoding, or to open
     * files with the wrong encodings on their reader. The {@link #setEncoding(String)}
     * method does what it can to try and detect these user errors, and is worth the effort.
     * Consider using {@link SAXParserWithEncodingCheck}
     * @param rdr
     */
    protected RDFXMLParser(SAXParser rdr) {
        super();
        saxParser = rdr;
        try {
            SAX2RDF.installHandlers(rdr, this);
        } catch (SAXException e) {
            throw new RuntimeException("Supposedly impossible:", e);
        }
    }

    public SAXParser getSAXParser() {
        return saxParser;
    }

    /**
     * This works with an {@link RDFXMLParser} and catches and reports several
     * common errors to do with character encoding.
     *
     */
    static protected class SAXParserWithEncodingCheck extends SAXParser {
        protected SAXParserWithEncodingCheck(StandardParserConfiguration c) {
            super(c);
//            try {
//                setFeature("http://xml.org/sax/features/string-interning",
//                        false);
//            } catch (SAXException e) {
//                // Not supported - aggh
//                // TO DO ask on xerces list why not?
//                // e.printStackTrace();
//            }
        }

        private RDFXMLParser rdfXmlParser;

        @Override
        public void xmlDecl(String version, String encoding, String standalone,
                Augmentations augs) {
            try {
                getRdfXmlParser().setEncoding(encoding == null ? "UTF" : encoding);
            } catch (SAXParseException e) {
                throw new WrappedException(e);
            }
            super.xmlDecl(version, encoding, standalone, augs);

        }

		/**
		 * This must be called as part of the initialization process.
		 * @param rdfXmlParser the rdfXmlParser to set
		 */
		public void setRdfXmlParser(RDFXMLParser rdfXmlParser) {
			this.rdfXmlParser = rdfXmlParser;
		}

		/**
		 * @return the rdfXmlParser
		 */
		public RDFXMLParser getRdfXmlParser() {
			if (rdfXmlParser == null) {
				throw new IllegalStateException("setRdfXmlParser must be called as part of the initialization process");
			}
			return rdfXmlParser;
		}
    }

    public static RDFXMLParser create() {
        StandardParserConfiguration c = new StandardParserConfiguration();
        SAXParserWithEncodingCheck msp = new SAXParserWithEncodingCheck(c);
        RDFXMLParser a = new RDFXMLParser(msp);
        msp.setRdfXmlParser(a);
        return a;
    }


    public void parse(InputSource input) throws IOException, SAXException {
        parse(input, input.getSystemId());
    }


    synchronized public void parse(InputSource input, String base)
            throws IOException, SAXException {
        // Make sure we have a sane state for
        // Namespace processing.

        initParse(base,"");
        SAX2RDF.installHandlers(saxParser, this);
        saxParser.reset();

        initEncodingChecks(input);
        try {

            saxParser.parse(input);

        } 
        catch (UTFDataFormatException e) {
                generalError(ERR_UTF_ENCODING, e);
        } 
        catch (IOException e) {
                generalError(ERR_GENERIC_IO, e);
        } 
        catch (WrappedException wrapped) {
            wrapped.throwMe();
        }
        catch (FatalParsingErrorException e) {
            // ignore this.
        }
        finally {
            afterParse();
        }

    }

    private void initEncodingChecks(InputSource in) {
        Reader rdr = in.getCharacterStream();
        readerXMLEncoding = null;
        encodingProblems = false;
        if (rdr != null && rdr instanceof InputStreamReader) {
            String javaEnc = ((InputStreamReader) rdr).getEncoding();
            readerXMLEncoding = CharEncoding.create(javaEnc).name();
        }
    }

    protected void setEncoding(String original) throws SAXParseException {

        CharEncoding encodingInfo = CharEncoding.create(original);
        String e = encodingInfo.name();
        if (xmlEncoding == null) {
            // special case UTF-8 or UTF-16?
            if (e.equals("UTF") && readerXMLEncoding != null
                    && readerXMLEncoding.startsWith("UTF")) {
                xmlEncoding = readerXMLEncoding;
                return;
            }
            xmlEncoding = e;
            if (readerXMLEncoding != null
                    && !readerXMLEncoding.equalsIgnoreCase(e)) {
                warning(null,
                        WARN_ENCODING_MISMATCH,
                        "Encoding on InputStreamReader or FileReader does not match that of XML document. Use FileInputStream. ["
                                + readerXMLEncoding + " != " + e + "]");
                encodingProblems = true;
            }

            if (e.equals("UTF"))
                return;

            if (!encodingInfo.isIANA()) {
                warning(null,encodingInfo.isInNIO() ? WARN_NON_IANA_ENCODING
                        : WARN_UNSUPPORTED_ENCODING, encodingInfo
                        .warningMessage());
            } else if (!original.equalsIgnoreCase(e)) {
                warning(null,WARN_NONCANONICAL_IANA_NAME, "The encoding \""
                        + original
                        + "\" is not the canonical name at IANA, suggest \""
                        + e + "\" would give more interoperability.");

            }
        }
    }

}
