/*
 *  (c) Copyright 2001  Hewlett-Packard Development Company, LP
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
 
 * * $Id: XMLContext.java,v 1.3 2003-08-27 13:05:52 andy_seaborne Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * XMLContext.java
 *
 * Created on July 10, 2001, 2:35 AM
 */

package com.hp.hpl.jena.rdf.arp;
import java.util.*;


/**
 *
 * @author  jjc
 
 */
class XMLContext extends Object {
    final private String base;
    final private String lang;
    final private URI uri;
    final private Map namespaces;
    XMLContext document;
    /** Creates new XMLContext */
    XMLContext(String base) throws MalformedURIException {
        this(base,new URI(base));
    }
	XMLContext(String base,URI uri) {
		this(null,uri,base,"",ParserSupport.xmlNameSpace());
		document = this;
	}
    
    /*
   private XMLContext(XMLContext document,String base,String lang,Map namespaces) throws MalformedURIException {
        this(document,new URI(base),base,lang,namespaces);
    }
    */
    XMLContext(XMLContext document,URI uri,String base,String lang,Map namespaces)  {
        this.base=base;
        this.lang=lang;
        this.uri = uri;
        this.document = document;
        this.namespaces = namespaces;
    }
    XMLContext withBase(String b)  throws MalformedURIException {
        return new XMLContext(document,new URI(b),b,lang,namespaces);
    }
    XMLContext revertToDocument() {
        return document.withLang(lang);
    }
    XMLContext withLang(String l) {
        return clone(document,uri,base,l,namespaces);
    }
    String getLang() {
        return lang;
    }
    
    String getBase() {
        return base;
    }
    Map getNamespaces() {
    	return namespaces;
    }
    XMLContext addNamespace(Token prefix, Token ur) {
    	Map newns = new HashMap(namespaces);
    	newns.put(((StrToken)prefix).value,((StrToken)ur).value);
        return clone(document,uri,base,lang,newns);	
    }
    URI getURI() {
        return uri;
    }
    boolean isSameAsDocument() {
        return this==document || uri.equals(document.uri);
    }
    XMLContext getDocument() {
        return document;
    }
    XMLContext clone(XMLContext document,URI uri,String base,String lang,Map namespaces) {
    	return new XMLContext(document,uri,base,lang,namespaces);
    }
    String resolve(Location l, String uri) throws MalformedURIException, ParseException {
	 return new URI(getURI(),uri).getURIString();
    }
	String resolveSameDocRef(Location l, String sameDoc) throws ParseException {
	 return base + sameDoc;
	}
}
