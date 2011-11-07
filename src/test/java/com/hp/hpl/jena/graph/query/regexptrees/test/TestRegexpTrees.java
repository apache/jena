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

package com.hp.hpl.jena.graph.query.regexptrees.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.test.GraphTestBase;

import com.hp.hpl.jena.graph.query.regexptrees.*;
/**
     Test useful properties of regexp trees.
     @author hedgehog
*/
public class TestRegexpTrees extends GraphTestBase
    {
    public TestRegexpTrees( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestRegexpTrees.class ); }
    
    protected Object [][] equalities =
        {
            { new EndOfLine(), "EOL" },
            { new EndOfLine(), "EOL" },
            { new StartOfLine(), "SOL" },
            { new StartOfLine(), "SOL" },
            { new AnySingle(), "ANY" },
            { new AnySingle(), "ANY" },
            { new Paren( new AnySingle() ), "(ANY)" },
            { new Paren( new EndOfLine() ), "(EOL)" },
            { Text.create( "hello" ), "hello" },
            { Text.create( "goodbye" ), "goodbye" },
            { new AnyOf( "abcde" ), "any[abcde]" },
            { new AnyOf( "defgh" ), "any[defgh]" },
            { new NoneOf( "pqrst" ), "none[pqrst]" },
            { new NoneOf( "12345" ), "none[12345]" },
            { new BackReference( 1 ), "back(1)" },
            { new BackReference( 2 ), "back(2)" }
        };
    
    public void testEqualities()
        {
        for (int i = 0; i < equalities.length; i += 1)
            for (int j = 0; j < equalities.length; j += 1)
                {
                Object [] A = equalities[i], B = equalities[j];
                boolean equal = A[1].equals( B[1] );
                if (A[0].equals( B[0] ) != equal )
                    fail( A[0] + " should be " + (equal ? "equal to " : "different from ") + B[0] );
                }
        }
    
    public void testConstantsDefinition()
        { 
        assertEquals( RegexpTree.EOL, new EndOfLine() );
        assertEquals( RegexpTree.SOL, new StartOfLine() );
        assertEquals( RegexpTree.ANY, new AnySingle() );
        }
    
    public void testExtractOperandFromOneOrMore()
        {
        testExtractFromOneOrMore( RegexpTree.EOL );
        testExtractFromOneOrMore( RegexpTree.SOL );
        testExtractFromOneOrMore( RegexpTree.ANY );
        }
    
    public void testExtractOperandFromZeroOrMore()
        {
        testExtractFromZeroOrMore( RegexpTree.EOL );
        testExtractFromZeroOrMore( RegexpTree.SOL );
        testExtractFromZeroOrMore( RegexpTree.ANY );
        }
    
    public void testExtractOperandFromOptional()
        {
        testExtractFromOptional( RegexpTree.EOL );
        testExtractFromOptional( RegexpTree.SOL );
        testExtractFromOptional( RegexpTree.ANY );
        }
    
    public void testLiteralContents()
        { assertEquals( "hello", Text.create( "hello" ).getString() ); }
    
    public void testParenOperand()
        { assertSame( RegexpTree.EOL, new Paren( RegexpTree.EOL ).getOperand() );  }
    
    public void testParenIndex()
        { assertEquals( 0, new Paren( RegexpTree.EOL ).getIndex() ); 
        assertEquals( 1, new Paren( RegexpTree.EOL, 1 ).getIndex() );  
        assertEquals( 17, new Paren( RegexpTree.NON, 17 ).getIndex() ); }
    
    public void testBackReference()
        { assertEquals( 2, new BackReference( 2 ).getIndex() ); }

    protected void testExtractFromOneOrMore( RegexpTree operand )
        { assertSame( operand, new OneOrMore( operand ).getOperand() ); }
    
    protected void testExtractFromZeroOrMore( RegexpTree operand )
        { assertSame( operand, new ZeroOrMore( operand ).getOperand() ); }
    
    protected void testExtractFromOptional( RegexpTree operand )
        { assertSame( operand, new Optional( operand ).getOperand() ); }
    }
