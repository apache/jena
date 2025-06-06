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

package org.apache.jena.rdfxml.xmlinput1;

import junit.framework.TestSuite;

public class TS3_xmlinput1 extends TestSuite
{
    static public TestSuite suite()
    {
        return new TS3_xmlinput1() ;
    }

    private TS3_xmlinput1()
    {
        super("RDF/XML Input ARP1") ;
        addTest( org.apache.jena.rdfxml.xmlinput1.TestURIs.suite());
        addTest( org.apache.jena.rdfxml.xmlinput1.TestSuiteWG_NTriples.suite());
        addTest( org.apache.jena.rdfxml.xmlinput1.TestSuiteWG_RDFXML.suite());
        addTest( org.apache.jena.rdfxml.xmlinput1.TestSuiteWG_RDFXML_ARP.suite());

        addTest( org.apache.jena.rdfxml.xmlinput1.TestsARP.suite());
        addTest( org.apache.jena.rdfxml.xmlinput1.states.TestARPStates.suite());

        addTest( org.apache.jena.rdfxml.xmlinput1.TestsTainting.suite());
        addTest( org.apache.jena.rdfxml.xmlinput1.TestsSAX2RDF.suite());
        addTest( org.apache.jena.rdfxml.xmlinput1.TestsStAX2Model.suite());
        addTest( org.apache.jena.rdfxml.xmlinput1.TestRDFXML_URI.suite());
    }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }
}
