/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestVocabRDF.java,v 1.3 2003-07-18 09:33:33 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary.test;

import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.vocabulary.*;
import junit.framework.*;

/**
 	@author kers
*/
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
        assertEquals( RDF.Alt.getNode(), RDF.Nodes.Alt );
        assertEquals( RDF.Bag.getNode(), RDF.Nodes.Bag );
        assertEquals( RDF.Property.getNode(), RDF.Nodes.Property );
        assertEquals( RDF.Seq.getNode(), RDF.Nodes. Seq );
        assertEquals( RDF.Statement.getNode(), RDF.Nodes. Statement );
        assertEquals( RDF.List.getNode(), RDF.Nodes. List );
        assertEquals( RDF.nil.getNode(), RDF.Nodes. nil );
        assertEquals( RDF.type.getNode(), RDF.Nodes. type );
        assertEquals( RDF.rest.getNode(), RDF.Nodes. rest );
        assertEquals( RDF.first.getNode(), RDF.Nodes. first );
        assertEquals( RDF.subject.getNode(), RDF.Nodes. subject );
        assertEquals( RDF.predicate.getNode(), RDF.Nodes. predicate );
        assertEquals( RDF.object.getNode(), RDF.Nodes. object );
        assertEquals( RDF.value.getNode(), RDF.Nodes. value );
        }
    }


/*
    (c) Copyright Hewlett-Packard Company 2003
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
