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

package com.hp.hpl.jena.graph.query.test;

import junit.framework.*;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;

/**
 	@author hedgehog
*/
public class TestExpressions extends QueryTestBase
    {
    public TestExpressions( String name ) 
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestExpressions.class ); }
   
    public void testExpressionPatternLanguages()
        {
        assertEquals( "http://jena.hpl.hp.com/2003/07/query/RDQL", PatternLiteral.rdql );
        }
    
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
        assertEquals( contains( A, "groo" ), contains( A, "groo" ) );
        assertEquals( contains( B, "oops" ), contains( B, "oops" ) );
        assertDiffer( contains( A, "groo" ), contains( A, "glue" ) );
        assertDiffer( contains( A, "groo" ), contains( B, "groo" ) );
        assertDiffer( contains( A, "blue" ), startsWith( A, "blue" ) );
        assertDiffer( contains( A, "blue" ), endsWith( A, "blue" ) );
        assertDiffer( endsWith( A, "blue" ), startsWith( A, "blue" ) );
        }

    public Expression contains( Expression L, PatternLiteral R )
        { return Rewrite.contains( L, R.getPatternString(), R.getPatternModifiers() ); }
    
    public Expression contains( Expression L, String R )
        { return Rewrite.contains( L, R, "" ); }
    
    public Expression endsWith( Expression L, String R )
        { return Rewrite.endsWith( L, R, "" ); }
    
    public Expression endsWith( Expression L, PatternLiteral R )
        { return Rewrite.endsWith( L, R.getPatternString(), R.getPatternModifiers() ); }
    
    public Expression startsWith( Expression L, String R )
        { return Rewrite.startsWith( L, R, "" ); }
    
    public Expression startsWith( Expression L, PatternLiteral R )
        { return Rewrite.startsWith( L, R.getPatternString(), R.getPatternModifiers() ); }
    
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
            @Override
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
        assertEquals( true, evalBool( startsWith( lit( "hello" ), "h" ) ) );
        assertEquals( true, evalBool( startsWith( lit( "hello" ), "he" ) ) );
        assertEquals( true, evalBool( startsWith( lit( "hello" ), "hel" ) ) );
        assertEquals( false, evalBool( startsWith( lit( "HELLO" ), "hel" ) ) );
        assertEquals( false, evalBool( startsWith( lit( "hello" ), "HEL" ) ) );
        assertEquals( false, evalBool( startsWith( lit( "hello" ), "e" ) ) );
        assertEquals( false, evalBool( startsWith( lit( "hello" ), "llo" ) ) );
        assertEquals( false, evalBool( startsWith( lit( "hello" ), "xhe" ) ) );
        }

    public void testStartsInsensitiveWith()
        {
        assertEquals( true, evalBool( startsWith( lit( "hello" ), pli( "H" ) ) ) );
        assertEquals( true, evalBool( startsWith( lit( "hEllo" ), pli( "he" ) ) ) );
        assertEquals( true, evalBool( startsWith( lit( "heLlo" ), pli( "hEl" ) ) ) );
        assertEquals( true, evalBool( startsWith( lit( "HELLO" ), pli( "hel" ) ) ) );
        assertEquals( true, evalBool( startsWith( lit( "hello" ), pli( "HEL" ) ) ) );
        assertEquals( false, evalBool( startsWith( lit( "hello" ), pli( "e" ) ) ) );
        assertEquals( false, evalBool( startsWith( lit( "hello" ), pli( "llo" ) ) ) );
        assertEquals( false, evalBool( startsWith( lit( "hello" ), pli( "xhe" ) ) ) );
        }

    public void testIsContains()
        {
        assertEquals( true, Rewrite.isContains( pl( "ambulance" ) ) );
        assertEquals( true, Rewrite.isContains( pl( "tendonitis", "i" ) ) );
        assertEquals( false, Rewrite.isContains( pl( "finishing", "z" ) ) );
        }
    
    public void testSensitiveContains()
        {
        assertEquals( true, evalBool( contains( lit( "hello" ), "h" ) ) );
        assertEquals( true, evalBool( contains( lit( "hello" ), "e" ) ) );
        assertEquals( true, evalBool( contains( lit( "hello" ), "ll" ) ) );
        assertEquals( false, evalBool( contains( lit( "heLLo" ), "ll" ) ) );
        assertEquals( false, evalBool( contains( lit( "hello" ), "LL" ) ) );
        assertEquals( false, evalBool( contains( lit( "hello" ), "x" ) ) );
        assertEquals( false, evalBool( contains( lit( "hello" ), "the" ) ) );
        assertEquals( false, evalBool( contains( lit( "hello" ), "lot" ) ) );
        }        

    public void testInsensitiveContains()
        {
        assertEquals( true, evalBool( contains( lit( "Hello" ), pli( "h" ) ) ) );
        assertEquals( true, evalBool( contains( lit( "hello" ), pli( "E" ) ) ) );
        assertEquals( true, evalBool( contains( lit( "heLlo" ), pli( "lL" ) ) ) );
        assertEquals( false, evalBool( contains( lit( "hello" ), pli( "X" ) ) ) );
        assertEquals( false, evalBool( contains( lit( "hello" ), pli( "the" ) ) ) );
        assertEquals( false, evalBool( contains( lit( "hello" ), pli( "lot" ) ) ) );
        }

    public void testLangedLiteralsEndsWith()
        {
        assertEquals( true, evalBool( endsWith( litString( "'spoo'en" ), "o" ) ) );
        assertEquals( true, evalBool( endsWith( litString( "'spoo'go" ), "o" ) ) );
        assertEquals( false, evalBool( endsWith( litString( "'spot'go" ), "o" ) ) );
        assertEquals( false, evalBool( endsWith( litString( "'spot'en" ), "o" ) ) );
        }

    public void testTypedLiteralsEndsWith()
        {
        assertEquals( true, evalBool( endsWith( litString( "'spoo'xsd:mint" ), "o" ) ) );
        assertEquals( true, evalBool( endsWith( litString( "'spoo'xsd:gloo" ), "o" ) ) );
        assertEquals( false, evalBool( endsWith( litString( "'spot'xsd:slat" ), "o" ) ) );
        assertEquals( false, evalBool( endsWith( litString( "'spot'xsd:do" ), "o" ) ) );
        }
    
    public void testLangedLiteralsContains()
        {
        assertEquals( true, evalBool( contains( litString( "'spoo'en" ), "po" ) ) );
        assertEquals( true, evalBool( contains( litString( "'spoo'go" ), "sp" ) ) );
        assertEquals( false, evalBool( contains( litString( "'spot'go" ), "go" ) ) );
        assertEquals( false, evalBool( contains( litString( "'spot'en" ), "en" ) ) );
        }

    public void testTypedLiteralsContains()
        {
        assertEquals( true, evalBool( contains( litString( "'spoo'xsd:mint" ), "sp" ) ) );
        assertEquals( true, evalBool( contains( litString( "'spoo'xsd:gloo" ), "po" ) ) );
        assertEquals( false, evalBool( contains( litString( "'spot'xsd:slat" ), "sl" ) ) );
        assertEquals( false, evalBool( contains( litString( "'spot'xsd:do" ), "do" ) ) );
        }
    
    public void testEndsWith()
        {
        assertEquals( true, evalBool( endsWith( lit( "hello" ), "o" ) ) );
        assertEquals( true, evalBool( endsWith( lit( "hello" ), "lo" ) ) );
        assertEquals( true, evalBool( endsWith( lit( "hello" ), "hello" ) ) );
        assertEquals( false, evalBool( endsWith( lit( "HELLO" ), "hello" ) ) );
        assertEquals( false, evalBool( endsWith( lit( "hello" ), "HELLO" ) ) );
        assertEquals( false, evalBool( endsWith( lit( "hello" ), "ll" ) ) );
        assertEquals( false, evalBool( endsWith( lit( "hello" ), "hel" ) ) );
        assertEquals( false, evalBool( endsWith( lit( "hello" ), "quantum" ) ) );
        }

    public void testInsensitiveEndsWith()
        {
        assertEquals( true, evalBool( endsWith( lit( "hellO" ), pli( "o" ) ) ) );
        assertEquals( true, evalBool( endsWith( lit( "hello" ), pli( "lO" ) ) ) );
        assertEquals( true, evalBool( endsWith( lit( "HeLLo" ), pli( "HELlo" ) ) ) );
        assertEquals( false, evalBool( endsWith( lit( "hello" ), pli( "ll" ) ) ) );
        assertEquals( false, evalBool( endsWith( lit( "hello" ), pli( "hel" ) ) ) );
        assertEquals( false, evalBool( endsWith( lit( "hello" ), pli( "quantum" ) ) ) );
        }

    private Object evalObject(Expression e)
        { return e.prepare( emptyMapping ).evalObject( noIVs ); }
    
    private boolean evalBool(Expression e)
        { return e.prepare( emptyMapping ).evalBool( noIVs ); }
    
    protected Expression litString( String s )
        { return lit( NodeCreateUtils.create( s ) ); }  

    protected Expression lit( Object x )
        { return new Expression.Fixed( x ); }
    
    protected PatternLiteral pl( final String c )
        { return pl( c, "" ); }
        
    protected PatternLiteral pli( String c )
        { return pl( c, "i" ); }
    
    protected PatternLiteral pl( final String c, final String m )
        {
        return new PatternLiteral()
            {
            @Override
            public String getPatternString()
                { return c; }

            @Override
            public String getPatternModifiers()
                { return m; }

            @Override
            public String getPatternLanguage()
                { return rdql; }
            };
        }
    }
