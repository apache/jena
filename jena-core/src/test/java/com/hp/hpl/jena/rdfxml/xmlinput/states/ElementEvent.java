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

import com.hp.hpl.jena.rdfxml.xmlinput.states.FrameI ;

import org.xml.sax.Attributes ;
import org.xml.sax.SAXParseException ;

class ElementEvent extends Event {

    final QName q;
    public ElementEvent(QName qn) {
        this(qn.localName.substring(0,1),qn);
    }

    public ElementEvent(String oneChar, QName qn) {
        super(oneChar,"<"+qn.qName+">");
        q = qn;
    }

    @Override
    boolean isAttribute() {
        return false;
    }

    @Override
    boolean isElement() {
        return true;
    }

    @Override
    FrameI apply(FrameI from, Attributes atts) throws SAXParseException {
        return from.startElement(q.uri,q.localName,q.qName,atts);
    }

}
