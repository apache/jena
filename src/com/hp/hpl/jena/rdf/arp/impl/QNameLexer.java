/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.states.Frame;
import com.hp.hpl.jena.rdf.arp.*;

abstract class QNameLexer implements Names, ARPErrorNumbers {
    final int bad;
    final int select;
    final Frame frame;
    
    
    public QNameLexer(Frame f, int good, int bad) {
        bad &= ~good;
        this.bad = bad;
        this.select = good|bad;
        this.frame = f;
    }
    
    private int xml(String wanted, int fl) {
        return
             (fl &select)== fl
          && wanted.equals(getLocalName())
          && getUri().equals(xmlns)
          ? fl : 0;
    }
    abstract boolean isInRdfns() throws SAXParseException;
    abstract void error(int rslt) throws SAXParseException;
    abstract void deprecatedAttribute(int rslt) throws SAXParseException;
    

    abstract String getLocalName();
    abstract String getUri();
    abstract String getQName();
    
    private int rdf(String wanted, int fl) throws SAXParseException {
        if ((fl &select)== fl
          && wanted.equals(getLocalName())) {
            if (isInRdfns())
                return fl;
            // TODO: add test for this one
            frame.warning(WARN_NOT_RDF_NAMESPACE,"Maybe you wanted rdf:"+getLocalName());
        }
        return 0;
    }
    int lookup() throws SAXParseException {
        int rslt = lookupNoMsg();
        if ((rslt&bad)!=0) {
            if (rslt==A_DEPRECATED)
                deprecatedAttribute(rslt);
            else
                error(rslt);
        }
        return rslt;
    }
    private int lookupNoMsg() throws SAXParseException {
        char firstChar;
        try {
          firstChar = getLocalName().charAt(0);
        }
        catch (StringIndexOutOfBoundsException e) {
            // Yes this really happens with the DOM one.
            // How disgusting.
            // When xmlns="eg:a"
            // xmlns is the prefix ...
//            System.err.println(getUri());
            if (this.getUri().equals(xmlnsns)) 
                return A_XMLNS;
            throw e;
        }
        switch (firstChar) {
        case 'b': /* base bagID */
            switch (getLocalName().length()) {
            case 4:
                return xml("base",A_XMLBASE);
            case 5:
                return rdf("bagID",A_DEPRECATED);
            }
            break;
        case 'l': /* lang  li */
            switch (getLocalName().length()) {
            case 2:
                return rdf("li",E_LI);
            case 4:
                return xml("lang",A_XMLLANG);
            }
            break;
        case 's': /* space */
            return xml("space",A_XML_OTHER);
        case 'i': /* space */
            return xml("id",A_XML_OTHER);
        case 'I': /* ID */
            return rdf("ID",A_ID);
        case 'n': /* nodeID */
            return rdf("nodeID",A_NODEID);
        case 'a': /* about aboutEach aboutEachPrefix */
            switch (getLocalName().length()) {
            case 5:
                return rdf("about",A_ABOUT);
            case 9:
                return rdf("aboutEach",A_DEPRECATED);
            case 15:
                return rdf("aboutEachPrefix",A_DEPRECATED);
            }
            break;
        case 'r': /* resource */
            return rdf("resource",A_RESOURCE);
        case 'R': /* resource */
            return rdf("RDF",E_RDF);
        case 'd': /* datatype */
            return rdf("datatype",A_DATATYPE);
        case 't': /* type */
            return rdf("type",A_TYPE);
        case 'p': /* parseType */
            return rdf("parseType",A_PARSETYPE);
        case 'D': /* Description */
            return rdf("Description",E_DESCRIPTION);
        }
        return 0;
    }

    
//    static final Set rdfnames = new HashSet();
//    static {
//        rdfnames.add("Description");
//        rdfnames.add("RDF");
//        rdfnames.add("li");
//    }

    static final Set knownRDFProperties = new HashSet();

    static final Set knownRDFTypes = knownRDFProperties;
    static {
        knownRDFTypes.add("Bag");
        knownRDFTypes.add("Seq");
        knownRDFTypes.add("Alt");
        knownRDFTypes.add("List");
        knownRDFTypes.add("XMLLiteral");
        knownRDFTypes.add("Property");
        knownRDFProperties.add("type");
        knownRDFTypes.add("Statement");
        knownRDFProperties.add("subject");
        knownRDFProperties.add("predicate");
        knownRDFProperties.add("object");
        knownRDFProperties.add("value");
        knownRDFProperties.add("first");
        knownRDFProperties.add("rest");
        // not strictly true.
        knownRDFProperties.add("nil");
    }

//    static final Set knownBadRDFNames = new HashSet();
//    static {
//        knownBadRDFNames.add("ID");
//        knownBadRDFNames.add("about");
//        knownBadRDFNames.add("aboutEach");
//        knownBadRDFNames.add("aboutEachPrefix");
//        knownBadRDFNames.add("resource");
//        knownBadRDFNames.add("bagID");
//        knownBadRDFNames.add("parseType");
//        knownBadRDFNames.add("datatype");
//        knownBadRDFNames.add("li");
//        knownBadRDFNames.add("type");
//        knownBadRDFNames.add("Description");
//        knownBadRDFNames.add("nodeID");
//    }


    static private boolean isMemberProperty(String name) {
        if (name.startsWith("_")) {
            String number = name.substring(1);
            if (number.startsWith("-") || number.startsWith("0"))
                return false;
            try {
                Integer.parseInt(number);
                return true;
            } catch (NumberFormatException e) {
                try {
                    // It might be > Integer.MAX_VALUE
                    new java.math.BigInteger(number);
                    return true;
                } catch (NumberFormatException ee) {
                    return false;
                }
            }
        }
        return false;
    }

    static boolean isKnownRDFProperty(String name) {
        return knownRDFProperties.contains(name)
          || isMemberProperty(name);
    }

    

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
