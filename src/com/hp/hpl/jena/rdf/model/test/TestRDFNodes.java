/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestRDFNodes.java,v 1.1 2003-05-20 10:10:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import java.util.*;

import junit.framework.*;

/**
 	@author kers
    This class tests various properties of RDFNodes, to start with the
    new Visitor stuff.
*/
public class TestRDFNodes extends ModelTestBase
    {
    public TestRDFNodes(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestRDFNodes.class ); }
        
    public void testRDFVisitor()
        {
        final List strings = new ArrayList();
        Model m = ModelFactory.createDefaultModel();
        final RDFNode S = m.createResource();
        final RDFNode P = m.createProperty( "eh:PP" );
        final RDFNode O = m.createLiteral( "LL" );
    /* */
        RDFVisitor rv = new RDFVisitor() 
            {
            public Object visitBlank( Resource R, AnonId id )
                { 
                strings.add( "blank" ); 
                assertTrue( "must visit correct node", R == S );
                assertEquals( "must have correct field", R.getId(), id );
                return "blank result"; 
                }
            public Object visitURI( Resource R, String uri )
                { 
                strings.add( "uri" ); 
                assertTrue( "must visit correct node", R == P );
                assertEquals( "must have correct field", R.getURI(), uri );
                return "uri result"; 
                }
            public Object visitLiteral( Literal L )
                { 
                strings.add( "literal" );
                assertTrue( "must visit correct node", L == O ); 
                return "literal result"; 
                }
            };
    /* */
        assertEquals( "blank result", S.visitWith( rv ) );
        assertEquals( "uri result", P.visitWith( rv ) );
        assertEquals( "literal result", O.visitWith( rv ) );
    /* */
        assertEquals( strings.get(0), "blank" );
        assertEquals( strings.get(1), "uri" );
        assertEquals( strings.get(2), "literal" );
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