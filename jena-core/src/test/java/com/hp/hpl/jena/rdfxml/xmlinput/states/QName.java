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
import com.hp.hpl.jena.rdfxml.xmlinput.impl.Names ;

class QName implements Names {
    final String uri;
    final String localName;
    final String qName;

    public QName(String uri, String localName, String qname) {
        this.uri = uri;
        this.localName = localName;
        this.qName = qname;
    }
    static QName rdf(String name) {
        return new QName(rdfns,name,"RDF:"+name);
    }
    static QName xml(String name) {
        return new QName(xmlns,name,"xml:"+name);
    }
    static QName eg(String name) {
        return new QName("http://example.org/",name,"eg:"+name);
    }

}
