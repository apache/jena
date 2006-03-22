/*
 * (c) Copyright 2004, 2005, 2006 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;

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

import com.hp.hpl.jena.rdf.arp.FatalParsingErrorException;
import com.hp.hpl.jena.rdf.arp.SAX2RDF;
import com.hp.hpl.jena.util.CharEncoding;

/**
 * 
 * The main parser, other variants of XMLHandler are for more specialized purposes.
 * 
 * @author Jeremy J. Carroll
 * 
 */
public class RDFXMLParser extends XMLHandler {

    private SAXParser saxParser;

    private String readerXMLEncoding = null;

    private String xmlEncoding = null;

    private RDFXMLParser(SAXParser rdr) {
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

    static private class MySAXParser extends SAXParser {
        MySAXParser(StandardParserConfiguration c) {
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

        RDFXMLParser a;

        public void xmlDecl(String version, String encoding, String standalone,
                Augmentations augs) {
            try {
                a.setEncoding(encoding == null ? "UTF" : encoding);
            } catch (SAXParseException e) {
                throw new WrappedException(e);
            }
            super.xmlDecl(version, encoding, standalone, augs);

        }
    }

    public static RDFXMLParser create() {
        StandardParserConfiguration c = new StandardParserConfiguration();
        MySAXParser msp = new MySAXParser(c);
        RDFXMLParser a = new RDFXMLParser(msp);
        msp.a = a;
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

    void setEncoding(String original) throws SAXParseException {

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

/*
 * (c) Copyright 2004, 2005, 2006 Hewlett-Packard Development Company, LP All rights
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

