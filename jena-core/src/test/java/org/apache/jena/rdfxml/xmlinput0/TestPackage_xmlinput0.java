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

package org.apache.jena.rdfxml.xmlinput0;

import junit.framework.TestSuite;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.test.X_RDFReaderF;

public class TestPackage_xmlinput0 extends TestSuite
{
    static public TestSuite suite()
    {
        return new TestPackage_xmlinput0() ;

    }

    private TestPackage_xmlinput0()
    {
        super("ARP[Legacy]") ;
        RDFReaderFImpl.alternative(new X_RDFReaderF());
        addTest( org.apache.jena.rdfxml.xmlinput0.TestARPMain.suite());
        addTest( org.apache.jena.rdfxml.xmlinput0.ARPTests2.suite());
        addTest( org.apache.jena.rdfxml.xmlinput0.states.TestARPStates.suite());
        addTest( org.apache.jena.rdfxml.xmlinput0.URITests.suite());
        addTest( org.apache.jena.rdfxml.xmlinput0.TaintingTests.suite());
        addTest( org.apache.jena.rdfxml.xmlinput0.SAX2RDFTest.suite());
        addTest( org.apache.jena.rdfxml.xmlinput0.StAX2ModelTest.suite());
    }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }
}
