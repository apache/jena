/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 
 * * $Id: ARPString.java,v 1.1 2009-06-29 08:55:38 castagna Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * StringToken.java
 *
 * Created on June 22, 2001, 9:44 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.states.Frame;


/**
 *
 * @author  jjc
 */
public class ARPString extends TaintImpl implements ALiteral {

    final static String RDFXMLLiteral =
    "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
    /** Creates new StringToken */
    public ARPString(Frame f,String value,String parseType)
      throws SAXParseException 
      {
        f.checkString(this,value);
        this.value = value;
        this.lang = "";
        this.isWellFormedXML = true;
        this.parseType = parseType;
    }
    public ARPString(Frame f, String value,AbsXMLContext forXMLLang) throws SAXParseException {
        f.checkString(this,value);
        this.value = value;
        this.lang = forXMLLang.getLang(this);
        this.isWellFormedXML = false;
    }
    
   
    private String value;
    private String lang;
    private boolean isWellFormedXML;
    private String parseType;
    
    @Override
    public String toString() {
        return value;
    }
    
//    public ARPString concatenate(ARPString s2) {
//        if (lang.equals(s2.lang)) {
//            return new ARPString(value+s2.value,lang);
//        }
//        throw 
//          new IllegalArgumentException("ARPStrings can only be concatenated if they have the same xml:lang attribute");
//    }
//    ARPString quickConcatenate(ARPString s2) {
//        return new ARPString(value+s2.value,s2.lang);
//    }

    @Override
    public boolean isWellFormedXML() {
        return this.isWellFormedXML;
    }
    
    /**
     * @deprecated
     */
    @Override
    @Deprecated
    public String getParseType() {
        return parseType;
    }
    
    @Override
    public String getLang() {
        return lang;
    }
    @Override
    public String getDatatypeURI() {
        return isWellFormedXML?RDFXMLLiteral:null;
    }
    
}
