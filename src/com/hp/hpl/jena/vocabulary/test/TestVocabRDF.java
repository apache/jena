/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestVocabRDF.java,v 1.1 2003-06-23 11:01:37 chris-dollin Exp $
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
        Tests that the RDF vocabulary identifiers are what they're supposed to be. 
        TODO arrange that we detect if there are any other identifiers in the class.
    */
    public void testA()
        {
        String ns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
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