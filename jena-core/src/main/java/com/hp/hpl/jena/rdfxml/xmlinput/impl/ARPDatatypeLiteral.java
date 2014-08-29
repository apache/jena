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

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.ALiteral ;
import com.hp.hpl.jena.rdfxml.xmlinput.states.Frame ;

public class ARPDatatypeLiteral extends TaintImpl implements ALiteral {

    final private String datatype;
    final private String lexForm;
    
    public ARPDatatypeLiteral(Frame f, String lexf,URIReference dt) throws SAXParseException{
       
        f.checkString(this,lexf);
       datatype = dt.getURI();
       lexForm = lexf;
       if (dt.isTainted())
           taint();
    }
    /**
     * @see com.hp.hpl.jena.rdfxml.xmlinput.ALiteral#isWellFormedXML()
     */
    @Override
    public boolean isWellFormedXML() {
        return false; //datatype.equals(ARPString.RDFXMLLiteral);
    }
    /**
     * @see com.hp.hpl.jena.rdfxml.xmlinput.ALiteral#getParseType()
     * @deprecated
     */
    @Override
    @Deprecated
    public String getParseType() {
        return null;
    }
    @Override
    public String toString() {
        return lexForm;
    }

    /**
     * @see com.hp.hpl.jena.rdfxml.xmlinput.ALiteral#getDatatypeURI()
     */
    @Override
    public String getDatatypeURI() {
        return datatype;
    }

    /**
     * @see com.hp.hpl.jena.rdfxml.xmlinput.ALiteral#getLang()
     */
    @Override
    public String getLang() {
        return ""; //lang;
    }

}
