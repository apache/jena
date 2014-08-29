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

package com.hp.hpl.jena.rdfxml.xmlinput.states;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.* ;

public class WantPropertyElement extends Frame implements WantsObjectFrameI,
        HasSubjectFrameI {
    int liCounter = 1;

    ANode predicate;

    ANode object;

    ANode reify;

    boolean objectIsBlank = false;

    public WantPropertyElement(HasSubjectFrameI s, AbsXMLContext x) {
        super(s, x);
    }

    // These three are used as bitfields
    static final private int TYPEDLITERAL = 1;

    static final private int EMPTYWITHOBJ = 2;

    static final private int PARSETYPE = 4;

    @Override
    public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts) throws SAXParseException {
        clearObject();
        if (nonWhiteMsgGiven)
            taint.isTainted();
        nonWhiteMsgGiven = false;
        if (uri==null || uri.equals("")) {
            warning(WARN_UNQUALIFIED_ELEMENT,"Unqualified property elements are not allowed. Treated as a relative URI.");
        }
        ElementLexer el = new ElementLexer(taint, this, uri, localName,
                rawName, E_LI, CoreAndOldTerms | E_DESCRIPTION, false);
        // if (el.badMatch)
        // warning(ERR_SYNTAX_ERROR,"bad use of " + rawName);
        predicate = el.goodMatch ? (AResourceInternal) rdf_n(liCounter++)
                : URIReference.fromQName(this, uri, localName);
        if (taint.isTainted())
            predicate.taint();
        taint = new TaintImpl();

        AttributeLexer ap = new AttributeLexer(this,
        // xml:
                A_XMLLANG | A_XMLBASE | A_XML_OTHER
                // legal rdf:
                        | A_DATATYPE | A_ID | A_NODEID | A_PARSETYPE
                        | A_RESOURCE | A_TYPE,
                // bad rdf:
                A_BADATTRS);
        int cnt = ap.processSpecials(taint, atts);

        // These three states are intended as mutually
        // incompatible, but all three can occur
        // together. Any two of the three, or all
        // three is a syntax errror.
        // Having none of these is legal.
        final int nextStateCode = (ap.datatype == null ? 0 : TYPEDLITERAL)
                | (ap.parseType == null ? 0 : PARSETYPE)
                | (mustBeEmpty(ap, atts, cnt) ? EMPTYWITHOBJ : 0);

        if (this.badStateCode(nextStateCode)) {
            warning(errorNumber(nextStateCode), descriptionOfCases(ap,
                    nextStateCode, propertyAttributeDescription(atts, ap, cnt)));
        }

        AbsXMLContext x = ap.xml(xml);

        reify = ap.id == null ? null : URIReference.fromID(this, x, ap.id);
        if (taint.isTainted())
            predicate.taint();

        if (mustBeEmpty(ap, atts, cnt)) {
            if (ap.nodeID != null) {

                object = new ARPResource(arp, ap.nodeID);
                checkXMLName(object, ap.nodeID);
                objectIsBlank = true;
            }
            if (ap.resource != null) {
                if (object != null) {
                    if (!badStateCode(nextStateCode))
                        // otherwise warning already given
                        warning(ERR_SYNTAX_ERROR, 
                                "On a property element, only one of the attributes rdf:nodeID or rdf:resource is permitted.");
                } else
                    object = URIReference.resolve(this, x, ap.resource);
            }
            if (object == null) {
                object = new ARPResource(arp);
                objectIsBlank = true;
            }
            if (taint.isTainted())
                object.taint();
            processPropertyAttributes(ap, atts, x);
        }

        FrameI nextFrame = nextFrame(atts, ap, cnt, nextStateCode, x);
        if (object != null) {
            if (taint.isTainted())
                object.taint();
            theObject(object);
        }
        if (taint.isTainted())
            predicate.taint();
        return nextFrame;

    }

    private boolean mustBeEmpty(AttributeLexer ap, Attributes atts, int cnt) {
        return cnt < atts.getLength() || ap.type != null || ap.nodeID != null
                || ap.resource != null;
    }

    private FrameI nextFrame(Attributes atts, AttributeLexer ap, int cnt,
            int nextStateCode, AbsXMLContext x) throws SAXParseException {
        switch (nextStateCode) {
        case 0:
            return new WantLiteralValueOrDescription(this, x);
        case PARSETYPE | TYPEDLITERAL:
        case PARSETYPE | TYPEDLITERAL | EMPTYWITHOBJ:
        case PARSETYPE | EMPTYWITHOBJ:
        case PARSETYPE:
            return withParsetype(ap.parseType, x);
        case TYPEDLITERAL | EMPTYWITHOBJ:
        case TYPEDLITERAL:
            return new WantTypedLiteral(this, ap.datatype, x);
        case EMPTYWITHOBJ:
            return new WantEmpty(this, x);
        }
        throw new IllegalStateException("impossible");
    }

    private FrameI withParsetype(String pt, AbsXMLContext x)
            throws SAXParseException {
        if (pt.equals("Collection")) {
            return new RDFCollection(this, x);
        }
        if (pt.equals("Resource")) {
            if (object == null) {
                // in some error cases the object has already been set.
                object = new ARPResource(arp);
                objectIsBlank = true;
            }
            return new WantPropertyElement(this, x);
        }
        if (!pt.equals("Literal")) {
            warning(WARN_UNKNOWN_PARSETYPE, "Unknown rdf:parseType: '" + pt
                    + "' (treated as 'Literal'.");
        }
        return new OuterXMLLiteral(this, x, pt);
    }

    @Override
    String suggestParsetypeLiteral() {
        return (getParent() instanceof WantTopLevelDescription) ? "" : super
                .suggestParsetypeLiteral();
    }

    @Override
    public void aPredAndObj(ANode p, ANode o) {
        triple(object, p, o);
    }

    @Override
    public void makeSubjectReificationWith(ANode r) {
        triple(r, RDF_SUBJECT, object);
    }

    @Override
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

    @Override
    public void endElement() {
        clearObject();
    }

    @Override
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
        if (i >= _rdf_n.length) {
            int newLength = (i + 10) * 3 / 2;
            URIReference new_rdf_n[] = new URIReference[newLength];
            System.arraycopy(_rdf_n, 0, new_rdf_n, 0, _rdf_n.length);
            for (int j = _rdf_n.length; j < newLength; j++) {
                new_rdf_n[j] = URIReference.createNoChecks(rdfns + "_" + j);
            }
            _rdf_n = new_rdf_n;
        }
        return _rdf_n[i];
    }

    /***************************************************************************
     * 
     * ERROR HANDLING CODE
     * 
     **************************************************************************/

    // Error detection
    private boolean badStateCode(int nextStateCode) {
        switch (nextStateCode) {
        case PARSETYPE | TYPEDLITERAL:
        case PARSETYPE | TYPEDLITERAL | EMPTYWITHOBJ:
        case PARSETYPE | EMPTYWITHOBJ:
        case TYPEDLITERAL | EMPTYWITHOBJ:
            return true;
        case 0:
        case PARSETYPE:
        case TYPEDLITERAL:
        case EMPTYWITHOBJ:
            return false;
        }
        throw new IllegalStateException("impossible");
    }

    // Error classification

    private int errorNumber(int nextStateCode) {
        // TODO: not for 2.3. refine this error code.
        return ERR_SYNTAX_ERROR;
    }

    /***************************************************************************
     * 
     * ERROR MESSAGES
     * 
     **************************************************************************/
   private String descriptionOfCases(AttributeLexer ap, int nextStateCode,
            String propAttrs) {
        return ((propAttrs == null && ap.type == null)
                || (ap.nodeID == null && ap.resource == null && ap.type == null) || (ap.nodeID == null
                && ap.resource == null && propAttrs == null)) ? pairwiseIncompatibleErrorMessage(
                nextStateCode, ap, propAttrs)
                : complicatedErrorMessage(nextStateCode, ap, propAttrs);
    }

    private String pairwiseIncompatibleErrorMessage(int nextStateCode,
            AttributeLexer ap, String propAttrs) {
        ArrayList<String> cases = new ArrayList<>();
        if ((nextStateCode & PARSETYPE) != 0)
            cases.add("rdf:parseType");
        if ((nextStateCode & TYPEDLITERAL) != 0)
            cases.add("rdf:datatype");
        if (ap.nodeID != null)
            cases.add("rdf:nodeID");
        if (ap.resource != null)
            cases.add("rdf:resource");
        if (ap.type != null)
            cases.add("rdf:type");

        if (cases.size() == 1) {
            if (propAttrs == null)
                throw new IllegalStateException("Shouldn't happen.");
            return "The attribute " + cases.get(0) + " is not permitted with "
                    + propAttrs + " on a property element.";
        }
        String rslt = "On a property element, only one of the ";
        if (propAttrs == null)
            rslt += "attributes ";
        for (int i = 0; i < cases.size(); i++) {
            rslt += cases.get(i);
            switch (cases.size() - i) {
            case 1:
                break;
            case 2:
                rslt += " or ";
                break;
            default:
                rslt += ", ";
                break;
            }
        }
        if (propAttrs != null) {
            rslt += " attributes or " + propAttrs;
        }
        rslt += " is permitted.";
        return rslt;
    }

    private String complicatedErrorMessage(int nextStateCode,
            AttributeLexer ap, String propAttrs) {
        String subjectIs;

        if (ap.nodeID == null && ap.resource == null
                && (ap.type == null || propAttrs == null))
            throw new IllegalStateException("precondition failed.");

        switch (nextStateCode & (TYPEDLITERAL | PARSETYPE)) {
        case TYPEDLITERAL | PARSETYPE:
            subjectIs = "the mutually incompatible attributes rdf:datatype and rdf:parseType are";
            break;
        case TYPEDLITERAL:
            subjectIs = "the attribute rdf:datatype is";
            break;
        case PARSETYPE:
            subjectIs = "the attribute rdf:parseType is";
            break;
        default:
            throw new IllegalStateException("precondition failed");
        }

        String nodeIDResource = null;
        if (ap.nodeID != null && ap.resource != null) {
            nodeIDResource = "the mutually incompatible attributes rdf:nodeID and rdf:resource";
        } else if (ap.nodeID != null) {
            nodeIDResource = "the attribute rdf:nodeID";
        } else if (ap.resource != null) {
            nodeIDResource = "the attribute rdf:resource";
        }

        int otherAttCount = nodeIDResource == null ? 0 : 1;
        String otherAtts;
        if (ap.type != null)
            otherAttCount++;
        if (propAttrs != null)
            otherAttCount++;
        if (otherAttCount < 2)
            throw new IllegalStateException("logic error");
        otherAtts = otherAttCount == 2 ? "both " : "each of ";

        if (ap.type != null && propAttrs != null) {
            if (nodeIDResource == null)
                otherAtts += "the attribute rdf:type and the " + propAttrs;
            else 
                otherAtts += "the attribute rdf:type, the " + propAttrs;
        } else if (ap.type != null) {
            otherAtts += "the attribute rdf:type";
        } else {
            otherAtts = "the " + propAttrs;
        }
        
        if (nodeIDResource != null)
            otherAtts += " and "+nodeIDResource;

        return "On a property element, " + subjectIs + " incompatible with "
                + otherAtts +".";
    }

    private String propertyAttributeDescription(Attributes atts,
            AttributeLexer ap, int cnt) {
        String propAttrs = "";
        int propAttrCount = atts.getLength() - cnt;
        int found = 0;
        if (propAttrCount == 0)
            return null;
        switch (propAttrCount) {
        case 0:
            break;
        case 1:
        case 2:
        case 3:
            for (int i = 0; i < atts.getLength(); i++)
                if (!ap.done(i)) {
                    propAttrs += atts.getQName(i);
                    found++;
                    switch (propAttrCount - found) {
                    case 0:
                        break;
                    case 1:
                        propAttrs += " and ";
                        break;
                    default:
                        propAttrs += ", ";
                    }
                }
            break;
        default:
            if (propAttrCount < 0)
                throw new IllegalStateException("Shouldn't happen.");
            for (int i = 0; i < atts.getLength(); i++)
                if (!ap.done(i)) {
                    found++;
                    switch (found) {
                    case 1:
                        propAttrs += atts.getQName(i) + ", ";
                        break;
                    case 2:
                        propAttrs += atts.getQName(i) + ", ...";
                        break;
                    default:
                    // ignore
                    }
                }
        }
        return "property attributes (" + propAttrs + ")";
    }

}
