/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestRegexpTrees.java,v 1.4 2004-09-02 11:34:45 chris-dollin Exp $
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
            { new Text( "hello" ), "hello" },
            { new Text( "goodbye" ), "goodbye" },
            { new AnyOf( "abcde" ), "any[abcde]" },
            { new AnyOf( "defgh" ), "any[defgh]" },
            { new NoneOf( "pqrst" ), "none[pqrst]" },
            { new NoneOf( "12345" ), "none[12345]" }
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
        { assertEquals( "hello", new Text( "hello" ).getString() ); }

    protected void testExtractFromOneOrMore( RegexpTree operand )
        { assertSame( operand, new OneOrMore( operand ).getOperand() ); }
    
    protected void testExtractFromZeroOrMore( RegexpTree operand )
        { assertSame( operand, new ZeroOrMore( operand ).getOperand() ); }
    
    protected void testExtractFromOptional( RegexpTree operand )
        { assertSame( operand, new Optional( operand ).getOperand() ); }
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