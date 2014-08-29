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

/*
 * StringToken.java
 *
 * Created on June 22, 2001, 9:44 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.ALiteral ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.Frame ;

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
