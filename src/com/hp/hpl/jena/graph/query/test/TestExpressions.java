/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: TestExpressions.java,v 1.6 2004-07-22 10:11:47 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import junit.framework.*;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;

/**
 	@author hedgehog
*/
public class TestExpressions extends QueryTestBase
    {
    public TestExpressions( String name ) 
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestExpressions.class ); }
   
    public void testBooleanEquality()
        {
        assertEquals( Expression.TRUE, Expression.TRUE );
        assertEquals( Expression.FALSE, Expression.FALSE );
        assertDiffer( Expression.TRUE, Expression.FALSE );
        assertDiffer( Expression.FALSE, Expression.TRUE );
        }
    
    public void testDyadicEquality()
        {
        Expression A = lit( "Aaa" ), B = lit( "Bee" );
        assertDiffer( A, B );
        assertEquals( Rewrite.contains( A, "groo" ), Rewrite.contains( A, "groo" ) );
        assertEquals( Rewrite.contains( B, "oops" ), Rewrite.contains( B, "oops" ) );
        assertDiffer( Rewrite.contains( A, "groo" ), Rewrite.contains( A, "glue" ) );
        assertDiffer( Rewrite.contains( A, "groo" ), Rewrite.contains( B, "groo" ) );
        assertDiffer( Rewrite.contains( A, "blue" ), Rewrite.startsWith( A, "blue" ) );
        assertDiffer( Rewrite.contains( A, "blue" ), Rewrite.endsWith( A, "blue" ) );
        assertDiffer( Rewrite.endsWith( A, "blue" ), Rewrite.startsWith( A, "blue" ) );
        }

    public void testLiterals()
        {
        assertTrue( Expression.TRUE.isConstant() );
        assertTrue( Expression.FALSE.isConstant() );
        assertEquals( Boolean.TRUE, Expression.TRUE.getValue() );
        assertEquals( Boolean.FALSE, Expression.FALSE.getValue() );
        }
    
    public void testPrepareTRUE()
        {
        Valuator t = Expression.TRUE.prepare( emptyMapping );  
        assertEquals( true, t.evalBool( noIVs ) );    
        assertEquals( Boolean.TRUE, t.evalObject( noIVs ) );
        }
        
    public void testPrepareFALSE()
        {
        Valuator t = Expression.FALSE.prepare( emptyMapping );  
        assertEquals( false, t.evalBool( noIVs ) );    
        assertEquals( Boolean.FALSE, t.evalObject( noIVs ) );
        }
    
    public void testFixed()
        {
        testFixed( "hello" );
        testFixed( "goodbye" );
        testFixed( Boolean.TRUE );
        testFixed( new int[] {17, 27, 42} );
        }

    protected void testFixed( Object value )
        {
        Expression e = lit( value );
        assertEquals( value, e.getValue() );
        assertEquals( value, evalObject( e ) );
        }
    
    public void testDyadic()
        {
        Expression L = lit( "a" );
        Expression R = lit( "b" );
        Expression e = new Dyadic( L, "eh:op", R )
        	{
            public Object evalObject( Object x, Object y )
                { return "" + x + "--" + y; }
            };
        assertEquals( 2, e.argCount() );
        assertSame( L, e.getArg( 0 ) );
        assertSame( R, e.getArg( 1 ) );
        assertEquals( "eh:op", e.getFun() );
        assertEquals( "a--b", evalObject(e) );
        }

    public void testStartsWith()
        {
        assertEquals( true, evalBool( Rewrite.startsWith( lit( "hello" ), "h" ) ) );
        assertEquals( true, evalBool( Rewrite.startsWith( lit( "hello" ), "he" ) ) );
        assertEquals( true, evalBool( Rewrite.startsWith( lit( "hello" ), "hel" ) ) );
        assertEquals( false, evalBool( Rewrite.startsWith( lit( "hello" ), "e" ) ) );
        assertEquals( false, evalBool( Rewrite.startsWith( lit( "hello" ), "llo" ) ) );
        assertEquals( false, evalBool( Rewrite.startsWith( lit( "hello" ), "xhe" ) ) );
        }

    public void testContains()
        {
        assertEquals( true, evalBool( Rewrite.contains( lit( "hello" ), "h" ) ) );
        assertEquals( true, evalBool( Rewrite.contains( lit( "hello" ), "e" ) ) );
        assertEquals( true, evalBool( Rewrite.contains( lit( "hello" ), "ll" ) ) );
        assertEquals( false, evalBool( Rewrite.contains( lit( "hello" ), "x" ) ) );
        assertEquals( false, evalBool( Rewrite.contains( lit( "hello" ), "the" ) ) );
        assertEquals( false, evalBool( Rewrite.contains( lit( "hello" ), "lot" ) ) );
        }

    public void testEndsWith()
        {
        assertEquals( true, evalBool( Rewrite.endsWith( lit( "hello" ), "o" ) ) );
        assertEquals( true, evalBool( Rewrite.endsWith( lit( "hello" ), "lo" ) ) );
        assertEquals( true, evalBool( Rewrite.endsWith( lit( "hello" ), "hello" ) ) );
        assertEquals( false, evalBool( Rewrite.endsWith( lit( "hello" ), "ll" ) ) );
        assertEquals( false, evalBool( Rewrite.endsWith( lit( "hello" ), "hel" ) ) );
        assertEquals( false, evalBool( Rewrite.endsWith( lit( "hello" ), "quantum" ) ) );
        }

    private Object evalObject(Expression e)
        { return e.prepare( emptyMapping ).evalObject( noIVs ); }
    
    private boolean evalBool(Expression e)
        { return e.prepare( emptyMapping ).evalBool( noIVs ); }

    protected Expression lit( Object x )
        { return new Expression.Fixed( x ); }
    
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