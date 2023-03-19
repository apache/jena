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

package org.apache.jena.rdfxml.xmlinput0.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UTFDataFormatException;

import org.apache.jena.rdfxml.xmlinput0.FatalParsingErrorException;
import org.apache.jena.rdfxml.xmlinput0.SAX2RDF;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.CharEncoding ;
import org.apache.jena.util.JenaXMLInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * The main RDFXML parser, other variants of XMLHandler are for more specialized purposes.
 */
public class RDFXMLParser0 extends XMLHandler {

    private final XMLReader saxParser;

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
    private RDFXMLParser0(XMLReader rdr) {
        super();
        saxParser = rdr;
        try {
            SAX2RDF.installHandlers(rdr, this);
        } catch (SAXException e) {
            throw new RuntimeException("Supposedly impossible:", e);
        }
    }

    public XMLReader getSAXParser() {
        return saxParser;
    }

    public static RDFXMLParser0 create() {
        try {
            // JenaXMLInput : safe XMLReader
            XMLReader xmlreader = JenaXMLInput.createXMLReader();
            RDFXMLParser0 a = new RDFXMLParser0(xmlreader);
            // Default.
            a.setEncoding("UTF");
            return a;
        } catch (Exception ex) {
            throw new JenaException("Failed to create an RDFXMLParser", ex);
        }
    }

    public void parse(InputSource input) throws IOException, SAXException {
        parse(input, input.getSystemId());
    }

    synchronized public void parse(InputSource input, String base) throws IOException, SAXException {
        // Make sure we have a sane state for
        // Namespace processing.
        initParse(base,"");
        SAX2RDF.installHandlers(saxParser, this);
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
