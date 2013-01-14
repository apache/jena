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

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

public class TestVocabRDF extends ModelTestBase
    {
    public TestVocabRDF(String name)
        { super(name); }

     public static TestSuite suite()
        { return new TestSuite( TestVocabRDF.class ); }

    /**
        The correct namespace for RDF. It is *important* that this be a literal
        string, not a reference to RDF.getURI(), because we're testing that the
        RDF vocabulary is correct, so this here string is the gold standard.
    */
    static final String RDFns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    /**
        Tests that the RDF vocabulary identifiers are what they're supposed to be.
        TODO arrange that we detect if there are any other identifiers in the class.
    */
    public void testRDFVocabulary()
        {
        String ns = RDFns;
        assertEquals( ns, RDF.getURI() );
        assertEquals( ns + "Alt", RDF.Alt.getURI() );
        assertEquals( ns + "Bag", RDF.Bag.getURI() );
        assertEquals( ns + "Property", RDF.Property.getURI() );
        assertEquals( ns + "Seq", RDF. Seq.getURI() );
        assertEquals( ns + "Statement", RDF. Statement.getURI() );
        assertEquals( ns + "List", RDF. List.getURI() );
        assertEquals( ns + "nil", RDF. nil.getURI() );
        assertEquals( ns + "type", RDF. type.getURI() );
        assertEquals( ns + "rest", RDF. rest.getURI() );
        assertEquals( ns + "first", RDF. first.getURI() );
        assertEquals( ns + "subject", RDF. subject.getURI() );
        assertEquals( ns + "predicate", RDF. predicate.getURI() );
        assertEquals( ns + "object", RDF. object.getURI() );
        assertEquals( ns + "value", RDF. value.getURI() );
        }

    /**
        Test that the RDF.li() method generates the correct strings for a few
        plausible test cases.
    */
    public void testLI()
        {
        String ns = RDFns;
        assertEquals( ns + "_1", RDF.li(1).getURI() );
        assertEquals( ns + "_1", RDF.li(1).getURI() );
        assertEquals( ns + "_10", RDF.li(10).getURI() );
        assertEquals( ns + "_11", RDF.li(11).getURI() );
        assertEquals( ns + "_100", RDF.li(100).getURI() );
        assertEquals( ns + "_123", RDF.li(123).getURI() );
        assertEquals( ns + "_32768", RDF.li(32768).getURI() );
        }

    public void testNodes()
        {
        assertEquals( RDF.Alt.asNode(), RDF.Nodes.Alt );
        assertEquals( RDF.Bag.asNode(), RDF.Nodes.Bag );
        assertEquals( RDF.Property.asNode(), RDF.Nodes.Property );
        assertEquals( RDF.Seq.asNode(), RDF.Nodes. Seq );
        assertEquals( RDF.Statement.asNode(), RDF.Nodes. Statement );
        assertEquals( RDF.List.asNode(), RDF.Nodes. List );
        assertEquals( RDF.nil.asNode(), RDF.Nodes. nil );
        assertEquals( RDF.type.asNode(), RDF.Nodes. type );
        assertEquals( RDF.rest.asNode(), RDF.Nodes. rest );
        assertEquals( RDF.first.asNode(), RDF.Nodes. first );
        assertEquals( RDF.subject.asNode(), RDF.Nodes. subject );
        assertEquals( RDF.predicate.asNode(), RDF.Nodes. predicate );
        assertEquals( RDF.object.asNode(), RDF.Nodes. object );
        assertEquals( RDF.value.asNode(), RDF.Nodes. value );
        }
    }
