/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;


import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.ANode;
import com.hp.hpl.jena.rdf.arp.impl.ARPResource;
import com.hp.hpl.jena.rdf.arp.impl.AResourceInternal;
import com.hp.hpl.jena.rdf.arp.impl.AttributeLexer;
import com.hp.hpl.jena.rdf.arp.impl.ElementLexer;
import com.hp.hpl.jena.rdf.arp.impl.URIReference;
import com.hp.hpl.jena.rdf.arp.impl.XMLContext;

public class WantPropertyElement extends Frame implements
        WantsObjectFrameI, HasSubjectFrameI {
    int liCounter = 1;

    ANode predicate;

    ANode object;

    ANode reify;

    boolean objectIsBlank = false;
    boolean nonWhiteMsgGiven = false;

    public WantPropertyElement(HasSubjectFrameI s, XMLContext x) {
        super(s, x);
    }

    static final private int TYPEDLITERAL = 1;

    static final private int EMPTYWITHOBJ = 2;

    static final private int PTLITERAL = 3;

    static final private int PTRESOURCE = 4;

    static final private int PTRDFCOLLECTION = 5;

    static final private int PTDAMLCOLLECTION = 6;

    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts)  throws SAXParseException {
        clearObject();
        nonWhiteMsgGiven = false;
        ElementLexer el = new ElementLexer(this, uri, localName, rawName, E_LI,
                CoreAndOldTerms | E_DESCRIPTION);
//        if (el.badMatch)
//            warning(ERR_SYNTAX_ERROR,"bad use of " + rawName);
        predicate = el.goodMatch ? 
                (AResourceInternal)rdf_n(liCounter++)
                : URIReference.fromQName(this,uri,localName);

        AttributeLexer ap = new AttributeLexer(this,
        // xml:
                A_XMLLANG | A_XMLBASE | A_XML_OTHER
                // legal rdf:
                        | A_DATATYPE | A_ID | A_NODEID | A_PARSETYPE
                        | A_RESOURCE | A_TYPE,
                // bad rdf:
                A_BADATTRS);
        int cnt = ap.processSpecials(atts);
        int nextStateCode = 0;
        XMLContext x = ap.xml(xml);

        reify = ap.id == null ? null : 
            URIReference.fromID(this,x,ap.id);
        if (cnt < atts.getLength() || ap.type != null || ap.nodeID != null
                || ap.resource != null) {
            if (ap.nodeID != null) {
                object = new ARPResource(arp, ap.nodeID);
               objectIsBlank = true;
            }
            if (ap.resource != null) {
                if (object != null)
                    warning(ERR_SYNTAX_ERROR,"Both rdf:nodeID and rdf:resource attributes cannot be specified on a property element.");
                else
                    object = URIReference.resolve(this,x,ap.resource);
            }
            if (object == null) {
                object = new ARPResource(arp);
                objectIsBlank = true;
            }
            // ((HasSubjectFrameI) getParent()).aPredAndObj(predicate, object);

            processPropertyAttributes(ap, atts, x);
            nextStateCode = check(nextStateCode, EMPTYWITHOBJ);
        }
        if (ap.datatype != null)
            nextStateCode = check(nextStateCode, TYPEDLITERAL);
        if (ap.parseType != null) {
            if (ap.parseType.equals("Collection")) {
                nextStateCode = check(nextStateCode, PTRDFCOLLECTION);
            } else if (ap.parseType.equals("daml:collection")) {
                warning(IGN_DAML_COLLECTION,"'daml:collection' is not really a legal value for rdf:parseType");
                nextStateCode = check(nextStateCode, PTDAMLCOLLECTION);
            } else if (ap.parseType.equals("Resource")) {
                object = new ARPResource(arp);
                objectIsBlank = true;
                nextStateCode = check(nextStateCode, PTRESOURCE);
            } else if (ap.parseType.equals("Literal")) {
                nextStateCode = check(nextStateCode, PTLITERAL);
            } else {
                warning(WARN_UNKNOWN_PARSETYPE,"Unknown rdf:parseType: '"+ap.parseType+ "' (treated as 'Literal'.");
                nextStateCode = check(nextStateCode, PTLITERAL);
            }
        }

        if (object!=null)
            theObject(object);
        
        switch (nextStateCode) {
        case 0:
            return new WantLiteralValueOrDescription(this, x);
        case PTLITERAL:
            return new OuterXMLLiteral(this, x);
        case PTRESOURCE:
            return new WantPropertyElement(this, x);
        case PTRDFCOLLECTION:
            return new RDFCollection(this, x);
        case PTDAMLCOLLECTION:
            return new DAMLCollection(this, x);
        case TYPEDLITERAL:
            return new WantTypedLiteral(this, resolve(x, ap.datatype), x);
        case EMPTYWITHOBJ:

            return new WantEmpty(this, x);
        }
        throw new IllegalStateException("impossible");

    }

    private int check(int oldV, int newV) throws SAXParseException {
        if (oldV != 0)
            // TODO: make this msg more precise
            warning(ERR_SYNTAX_ERROR,"Illegal attribute combination on property element");
        return newV;
    }

    // TODO: generalize this, check all instances of isWhite
    public void characters(char[] ch, int start, int length)  throws SAXParseException{
        if ((!nonWhiteMsgGiven) && !isWhite(ch, start, length)) {
            nonWhiteMsgGiven = true;
            warning(ERR_NOT_WHITESPACE,
              "Expecting propertyElement(s). String data \"" +
              new String(ch,start,length)+
              "\" not allowed: maybe you wanted to use rdf:parseType='Literal' for XML content.");
        }
    }

    public void aPredAndObj(ANode p, ANode o) {
        triple(object, p, o);
    }

    public void makeSubjectReificationWith(ANode r) {
        triple(r, RDF_SUBJECT, object);
    }

    public void theObject(ANode o) {
        HasSubjectFrameI p = (HasSubjectFrameI) getParent();
        p.aPredAndObj(predicate, o);
        if (reify != null) {
            triple(reify, RDF_TYPE, RDF_STATEMENT);
            triple(reify, RDF_OBJECT, o);
            triple(reify, RDF_PREDICATE, predicate);
            p.makeSubjectReificationWith(reify);
        }
    }

    public void endElement() {
        clearObject();
    }
    public void abort() {
        clearObject();
    }
    private void clearObject() {
        if (objectIsBlank)
            arp.endLocalScope(object);
        objectIsBlank = false;
        object = null;
    }
    
   static private URIReference _rdf_n[] = new URIReference[0];
    
    static private URIReference rdf_n(int i) {
        if (i>=_rdf_n.length) {
            int newLength = (i+10)*3/2;
            URIReference new_rdf_n[] = new URIReference[newLength];
            System.arraycopy(_rdf_n,0,new_rdf_n,0,_rdf_n.length);
            for (int j=_rdf_n.length;j<newLength;j++) {
                new_rdf_n[j] = URIReference.createNoChecks(rdfns+"_"+j);
            }
            _rdf_n = new_rdf_n;
        }
        return _rdf_n[i];
    }
 
}

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
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

