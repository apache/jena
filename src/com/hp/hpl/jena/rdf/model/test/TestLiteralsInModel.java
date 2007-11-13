/*
 	(c) Copyright 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: TestLiteralsInModel.java,v 1.3 2007-11-13 16:05:56 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;

public class TestLiteralsInModel extends ModelTestBase
    {
    public TestLiteralsInModel( String name )
        { super( name ); }

    protected final Model m = getModel();
    
    protected Model getModel()
        { return ModelFactory.createDefaultModel(); }
    
    static final Resource X = resource( "X" );
    
    static final Property P = property( "P" );
    
    public void testAddWithFloatObject()
        {
        m.addTyped( X, P, 14.0f );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 14.0f ) ) );
        assertTrue( m.containsTyped( X, P, 14.0f ) );
        }
    
    public void testAddWithDoubleObject()
        {
        m.addTyped( X, P, 14.0d );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 14.0d ) ) );
        assertTrue( m.containsTyped( X, P, 14.0d ) );
        }
    
    public void testAddWithBooleanObject()
        {
        m.addTyped( X, P, true );
        assertTrue( m.contains( X, P, m.createTypedLiteral( true ) ) );
        assertTrue( m.containsTyped( X, P, true ) );
        }
    
    public void testAddWithCharObject()
        {
        m.addTyped( X, P, 'x' );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 'x' ) ) );
        assertTrue( m.containsTyped( X, P, 'x' ) );
        }
    
    public void testAddWithLongObject()
        {
        m.addTyped( X, P, 99L );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 99L ) ) );
        assertTrue( m.containsTyped( X, P, 99L ) );
        }
    
    public void testAddWithIntObject()
        {
        m.addTyped( X, P, 99 );
        assertTrue( m.contains( X, P, m.createTypedLiteral( 99 ) ) );
        assertTrue( m.containsTyped( X, P, 99 ) );
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/