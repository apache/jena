package com.hp.hpl.jena.rdf.arp;

import org.xml.sax.SAXParseException;
/**
 * @author jjc
 *
 */
class SaxExceptionToken extends Token implements RDFParserConstants {

    final SAXParseException value;
    final int errorCode;
    
    SaxExceptionToken(int errCode,Location where,SAXParseException v) {
        super(X_SAX_EX,where);
        value = v;
        errorCode = errCode;
    }
    public String toString() {
        return value.getMessage();
    }

}
