/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestRDFNodes.java,v 1.4 2003-08-27 13:05:52 andy_seaborne Exp $
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
        
    public void testRemoveAllRemoves()
        {
        String ps = "x P a; x P b", rest = "x Q c; y P a; y Q b";
        Model m = modelWithStatements( ps + "; " + rest );
        Resource r = resource( m, "x" );
        Resource r2 = r.removeAll( property( m, "P" ) );
        assertSame( "removeAll should deliver its receiver", r, r2 );
        assertIsoModels( "x's P-values should go", modelWithStatements( rest ), m );
        }
        
    public void testRemoveAllBoring()
        {
        Model m1 = modelWithStatements( "x P a; y Q b" );
        Model m2 = modelWithStatements( "x P a; y Q b" );
        resource( m2, "x" ).removeAll( property( m2, "Z" ) );
        assertIsoModels( "m2 should be unchanged", m1, m2 );
        }
        
    public void testInModel()
        {
        Model m1 = modelWithStatements( "" );
        Model m2 = modelWithStatements( "" );
        Resource r1 = resource( m1, "r1" );
        Resource r2 = resource( m1, "_r2" );
    /* */
        assertTrue( r1.getModel() == m1 );
        assertTrue( r2.getModel() == m1 );
        assertFalse( r1.isAnon() );
        assertTrue( r2.isAnon() );
    /* */
        assertTrue( ((Resource) r1.inModel( m2 )).getModel() == m2 );
        assertTrue( ((Resource) r2.inModel( m2 )).getModel() == m2 );
    /* */
        assertEquals( r1, r1.inModel( m2 ) );
        assertEquals( r2, r2.inModel( m2 ) );
        }
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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