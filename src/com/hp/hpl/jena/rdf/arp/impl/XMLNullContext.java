/*
 (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: XMLNullContext.java,v 1.3 2005-09-14 15:31:12 jeremy_carroll Exp $
 */
package com.hp.hpl.jena.rdf.arp.impl;



import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.iri.RDFURIReference;
import com.hp.hpl.jena.rdf.arp.ARPErrorNumbers;
import com.hp.hpl.jena.rdf.arp.states.Frame;

/**
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 * 
 */
class XMLNullContext extends AbsXMLContext implements ARPErrorNumbers {
    
    final int errno;

    final String errmsg;

    XMLNullContext(XMLHandler f, int eno)  {
        super(true,null, f.iriFactory().create(""), new TaintImpl(), "", new TaintImpl());
        errno = eno;
        errmsg = eno == ERR_RESOLVING_URI_AGAINST_NULL_BASE ? "Base URI is null, but there are relative URIs to resolve."
                : "Base URI is \"\", relative URIs left as relative.";
    }

    private XMLNullContext(AbsXMLContext document, RDFURIReference uri, Taint baseT,String lang,Taint langT,
            XMLNullContext parent) {
        super(true,document, uri, baseT, lang, langT);
        errno = parent.errno;
        errmsg = parent.errmsg;
    }
    AbsXMLContext clone(RDFURIReference uri,Taint baseT,String lang, Taint langT) {
        return new XMLNullContext(document,uri,baseT,lang,langT,this);
    }

   

    boolean keepDocument(XMLHandler forErrors) {
        return !forErrors.ignoring(IGN_XMLBASE_SIGNIFICANT);
    }

    
    
    boolean isSameAsDocument() {
        return this == document;
    }

    

    void baseUsed(XMLHandler forErrors, Taint taintMe, String relUri, String string) throws SAXParseException {
        forErrors.warning(
                taintMe,
                errno,
                errmsg + ": <"+relUri+">");
        
    }
    

}

/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP All
 * rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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