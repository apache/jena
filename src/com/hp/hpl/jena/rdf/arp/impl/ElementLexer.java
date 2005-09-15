/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.impl;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.states.Frame;

public class ElementLexer extends QNameLexer  {
    
    final private String uri;
    final private String localName;
    final private String qname;
    public final boolean goodMatch;
    public final boolean badMatch;
    public ElementLexer(Taint t,Frame f, String uri,
                 String localName,
                 String qname, int good, int bad)  throws SAXParseException {
        super(f,good,bad);
        this.uri = uri;
        this.localName = localName;
        this.qname = qname;
        int match = lookup(t);
        goodMatch = (good&match) != 0;
        // Note: this.bad excludes good.
        badMatch = (this.bad&match) != 0;
        
        if ((!(goodMatch||badMatch))&&(this.bad&E_RDF)==E_RDF) {
            if (rdfns.equals(uri) && !isKnownRDFProperty(localName)) {
                frame.warning(t,WARN_UNKNOWN_RDF_ELEMENT,
                        qname + " is not a recognized RDF property or type.");
                
            }
        }
    }
    boolean isInRdfns(Taint me) {
        return rdfns.equals(getUri());
    }
    void error(Taint me, int r) throws SAXParseException {
        frame.warning(me,
                r==E_LI?ERR_LI_AS_TYPE:
                ERR_BAD_RDF_ELEMENT,
                getQName() + " is not allowed as an element tag here.");
        
    }
    void deprecatedAttribute(Taint me,int r) throws SAXParseException {
        error(me,r);
    }
    String getLocalName() {
        return localName;
    }
    String getUri() {
        return uri;
    }
    String getQName() {
        return qname;
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
 
