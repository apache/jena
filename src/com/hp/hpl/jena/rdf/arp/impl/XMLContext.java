/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 
 * * $Id: XMLContext.java,v 1.2 2005-08-09 03:30:19 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * XMLContext.java
 *
 * Created on July 10, 2001, 2:35 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import com.hp.hpl.jena.iri.*;

import java.net.URISyntaxException;

import org.xml.sax.SAXParseException;


/**
 *
 * @author  jjc
 
 */
public class XMLContext  {
//    final private String base;
    
    static private String truncateXMLBase(String rslt) {
        if (rslt ==null) return null;
        int hash = rslt.indexOf('#');
        if (hash != -1) {
            return rslt.substring(0, hash);
        }
        return rslt;
    }
    
    final private String lang;
    final private RDFURIReference uri;
    final XMLContext document;
    /** Creates new XMLContext */
    public XMLContext(XMLHandler h, String base) {
        this(h.iriFactory().create(truncateXMLBase(base)));
    }

    XMLContext(RDFURIReference uri) {
		this(null,uri,"");

	}
    
    protected XMLContext(XMLContext document,RDFURIReference uri,String lang)  {
//        this.base=base;
        this.lang=lang;
        this.uri = uri;
        this.document = document==null?this:document;
    }
    XMLContext withBase(String b) throws SAXParseException  {
        return new XMLContext(document,resolveAsURI(truncateXMLBase(b)),lang);
    }
    XMLContext revertToDocument() {
        return document.withLang(lang);
    }
    XMLContext withLang(String l) {
        return clone(document,uri,l);
    }
    public String getLang() {
        return lang;
    }
    RDFURIReference getURI() {
        return uri;
    }
    boolean isSameAsDocument() {
        return this==document || uri.equals(document.uri);
    }
    XMLContext getDocument() {
        return document;
    }
    XMLContext clone(XMLContext document,RDFURIReference uri,String lang) {
    	return new XMLContext(document,uri,lang);
    }
    RDFURIReference resolveAsURI(String relUri) throws SAXParseException{
        return uri.resolve(relUri);
    }

    public String resolve(String uri) throws SAXParseException {
	 return resolveAsURI(uri).toString();
    }
    /**
     * Get the base, for constructing a sameDocRef ...
     * will trigger warning if base malformed.
     * @return uri
     * @throws SAXParseException If warning on malformed base was disliked by user.
     */
    String getBase() throws SAXParseException {
        return uri.toString();
    }
	
}
