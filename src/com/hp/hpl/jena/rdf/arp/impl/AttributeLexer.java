/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.util.BitSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.states.Frame;


public class AttributeLexer extends QNameLexer implements ARPErrorNumbers {

    public AttributeLexer(Frame f, int which, int bad) {
        super(f, which, bad);
    }

    String lang;

    String base;

    public String about;

    public String datatype;

    public String resource;

    public String nodeID;

    public String id;

    public String type;

    public String parseType;

    public BitSet done;

    public int count;

    int index;

    Attributes att;
    
    AbsXMLContext xml;

    public int processSpecials(Taint taintMe, Attributes a) throws SAXParseException {
        att = a;
        int sz = a.getLength();
        done = new BitSet(sz);
        count = 0;
        for (index = 0; index < sz; index++) {
            boolean matched = true;
            switch (lookup(taintMe)) {
            case A_XMLBASE:
                base = value();
                frame
                        .warning(null,IGN_XMLBASE_USED,
                                "Use of attribute xml:base is not envisaged in RDF Model&Syntax.");
                break;
            case A_DEPRECATED:
            case A_BAGID:
            case E_LI:
            case E_RDF:
            case E_DESCRIPTION:
                break;

            case A_XMLLANG:
                lang = value();
              
                break;
            case A_XML_OTHER:
            case A_XMLNS:
                break;
            case A_ID:
                id = value(taintMe,id);
//                frame.checkIdSymbol(id);
                break;
            case A_NODEID:
                nodeID = value(taintMe,nodeID);
                break;
            case A_ABOUT:
                about = value(taintMe,about);
                break;
            case A_RESOURCE:
                resource = value(taintMe,resource);
                break;
            case A_DATATYPE:
                datatype = value(taintMe,datatype);
                break;
            case A_TYPE:
                type = value(taintMe,type);
                break;
            case A_PARSETYPE:
                parseType = value(taintMe,parseType);
                break;
            case 0:
                if ((select & A_XML_OTHER) == A_XML_OTHER) {
                    String qn = getQName();
                    if ((qn.length() >= 3 && qn.substring(0, 3).toLowerCase()
                            .equals("xml"))
                            || xmlns.equals(getUri())) {
                  // Some tools, e.g. DOM, won't let us switch off
                  // namespaces. Hence, they fall through to here.
                        if (!xmlnsns.equals(getUri()))
                           frame.warning(null,
                                        WARN_UNKNOWN_XML_ATTRIBUTE,
                                        "XML attribute: "
                                                + getQName()
                                                + " is not known and is being discarded.");
                        break;
                    }
                }
                matched = false;
                break;
            default:
                throw new IllegalStateException("impossible");
            }
            if (matched) {
                done.set(index);
                count++;
            } 
        }
        xml = computeXml(frame.getXMLContext());
        return count;
    }

    public AbsXMLContext xml(AbsXMLContext in) throws SAXParseException {
        if (xml==null)
            xml = computeXml(in);
        return xml;
    }
    private AbsXMLContext computeXml(AbsXMLContext in) throws SAXParseException {
        if (base != null) {
            in = in.withBase(frame.arp,base);
        }
        if (lang != null)
            in = in.withLang(frame.arp,lang);
        return in;
    }

   

    @Override
    boolean isInRdfns(Taint taintMe) throws SAXParseException {
        String uri = getUri();
        if (rdfns.equals(uri))
            return true;
        if (uri.equals("")) {
            frame.warning(taintMe,WARN_UNQUALIFIED_ATTRIBUTE, "unqualified use of rdf:"
                    + getQName() + " is deprecated.");
            return true;
        }
        return false;
    }

    @Override
    void error(Taint taintMe, int r) throws SAXParseException {
//         TODO: not for 2.3. specialize ERR_SYNTAX_ERROR ?
        int e = ERR_SYNTAX_ERROR;
        switch (r) {
        case E_LI:
        case E_DESCRIPTION:
        case E_RDF:
        case A_DEPRECATED:
        case A_BAGID:
            e = ERR_BAD_RDF_ATTRIBUTE;
            break;
        
        }
        frame.warning(taintMe, e, getQName()
                + " not allowed as attribute"
                + (e == ERR_BAD_RDF_ATTRIBUTE?".":" here."));
        
    }

    @Override
    void deprecatedAttribute(Taint me,int r) throws SAXParseException {
        frame.warning(me,ERR_BAD_RDF_ATTRIBUTE, getQName()
                + " has been deprecated.");
    }

    @Override
    String getLocalName() {
        return att.getLocalName(index);
    }

    @Override
    String getUri() {
        return att.getURI(index);
    }

    private String value() {
        return att.getValue(index);
    }
    
    /**
        Answer the xml:base value, or null if there wasn't one.
        [Added by kers, in search of xml:base processing]
    */
    public String getXMLBase()
        { return base; }

    private String value(Taint taintMe,String prev) throws SAXParseException {
        if (prev != null) {
            frame.warning(taintMe, ERR_SYNTAX_ERROR, "Cannot use " + getQName()
                    + " in both qualified and unqualifed form on same element");
        }
        return att.getValue(index);
    }

    @Override
    String getQName() {
        return att.getQName(index);
    }
    
    public boolean done(int i) {
        return done.get(i);
    }

    @Override
    void bagIDAttribute(Taint taintMe, int rslt) throws SAXParseException  {
        deprecatedAttribute(null,rslt);
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP All rights
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

