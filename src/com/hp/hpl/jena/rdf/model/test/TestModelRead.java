/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelRead.java,v 1.1 2004-11-22 12:24:26 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

import junit.framework.TestSuite;

/**
     TestModelRead - test that the new model.read operation(s) exist.
     @author kers
 */
public class TestModelRead extends ModelTestBase
    {
    public TestModelRead( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestModelRead.class ); }
    
    public void testReturnsSelf()
        {
        Model m = ModelFactory.createDefaultModel();
        assertSame( m, m.read( "file:testing/modelReading/empty.n3", "base", "N3" ) );
        assertTrue( m.isEmpty() );
        }
    
    public void testLoadsSimpleModel()
        {
        Model expected = ModelFactory.createDefaultModel();
        Model m = ModelFactory.createDefaultModel();
        expected.read( "file:testing/modelReading/simple.n3", "N3" );
        assertSame( m, m.read( "file:testing/modelReading/simple.n3", "base", "N3" ) );
        assertIsoModels( expected, m );
        }    
    
    public void testSimpleLoadImplictBase()
        {
        Model mBasedImplicit = ModelFactory.createDefaultModel();
        mBasedImplicit.read( "file:testing/modelReading/based.n3", "N3" );
        assertIsoModels( modelWithStatements( "file:testing/modelReading/based.n3 jms:predicate jms:object" ), mBasedImplicit );
        }
    
    public void testSimpleLoadExplicitBase()
        {
        Model mBasedExplicit = ModelFactory.createDefaultModel();
        mBasedExplicit.read( "file:testing/modelReading/based.n3", "base:", "N3" );
        assertIsoModels( modelWithStatements( "base: jms:predicate jms:object" ), mBasedExplicit );
        }
    
    public void testDefaultLangXML()
        {
        Model m = ModelFactory.createDefaultModel();
        m.read( "file:testing/modelReading/plain.rdf", null, null );
        }
    }


/*
	(c) Copyright 2004, Hewlett-Packard Development Company, LP
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