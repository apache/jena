/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestLiteralImpl.java,v 1.1 2003-09-08 15:05:23 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;

/**
	TestLiteralImpl - minimal, this is the first time an extra test has been needed above
    the regression testing.

	@author kers
*/
public class TestLiteralImpl extends ModelTestBase 
    {
    public TestLiteralImpl( String name )
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestLiteralImpl.class ); }

    /**
        Test that a non-literal node cannot be as'ed into a literal
    */
    public void testCannotAsNonLiteral()
        { Model m = ModelFactory.createDefaultModel();  
        try
            { resource( m, "plumPie" ).as( Literal.class ); 
            fail( "non-literal cannot be converted to literal" ); }
        catch (LiteralRequiredException l)
            { pass(); } }
    
    /**
        Test that a literal node can be as'ed into a literal.
    */    
    public void testAsLiteral()
        { Model m = ModelFactory.createDefaultModel();  
        literal( m, "17" ).as( Literal.class );  }
    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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