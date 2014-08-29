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

import com.hp.hpl.jena.rdfxml.xmlinput.states.Frame ;

public class ElementLexer extends QNameLexer  {
    
    final private String uri;
    final private String localName;
    final private String qname;
    public final boolean goodMatch;
    public final boolean badMatch;
    public ElementLexer(Taint t,Frame f, String uri,
                 String localName,
                 String qname, int good, int bad, boolean report_1)  throws SAXParseException {
        super(f,good,bad);
        this.uri = uri;
        this.localName = localName;
        this.qname = qname;
        int match = lookup(t);
        goodMatch = (good&match) != 0;
        // Note: this.bad excludes good.
        badMatch = (this.bad&match) != 0;
        
        if ((!(goodMatch||badMatch))&&(this.bad&E_RDF)==E_RDF) {
            if (rdfns.equals(uri)) {
                if (isMemberProperty(localName)){
                    if (report_1)
                    frame.warning(t,WARN_RDF_NN_AS_TYPE,
                            qname + " is being used on a typed node.");
                } else if (!isKnownNonMemberRDFProperty(localName)) {
                frame.warning(t,WARN_UNKNOWN_RDF_ELEMENT,
                        qname + " is not a recognized RDF property or type.");
                
            }
            }
        }
    }
    @Override
    boolean isInRdfns(Taint me) {
        return rdfns.equals(getUri());
    }
    @Override
    void error(Taint me, int r) throws SAXParseException {
        frame.warning(me,
                r==E_LI?ERR_LI_AS_TYPE:
                ERR_BAD_RDF_ELEMENT,
                getQName() + " is not allowed as an element tag here.");
        
    }
    @Override
    void deprecatedAttribute(Taint me,int r) throws SAXParseException {
        error(me,r);
    }
    @Override
    String getLocalName() {
        return localName;
    }
    @Override
    String getUri() {
        return uri;
    }
    @Override
    String getQName() {
        return qname;
    }
    @Override
    void bagIDAttribute(Taint taintMe, int rslt) throws SAXParseException {
        error(taintMe,rslt);
        
    }

}
