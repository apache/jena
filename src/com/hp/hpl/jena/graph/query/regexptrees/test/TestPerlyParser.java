/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestPerlyParser.java,v 1.7 2004-08-17 19:03:35 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query.regexptrees.test;

import java.util.Arrays;

import junit.framework.TestSuite;

import com.hp.hpl.jena.graph.query.regexptrees.*;
import com.hp.hpl.jena.graph.test.GraphTestBase;

/**
     TestPerlyParser - tests for the parser of Perl REs into RegexpTrees.
     @author kers
*/
public class TestPerlyParser extends GraphTestBase
    {
    public TestPerlyParser( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestPerlyParser.class ); }
    
    protected static class FlagException extends RuntimeException
        {}
    
    public void testAlternateGenerator()
        {
        RegexpTreeGenerator g = new SimpleGenerator()
            {
            public RegexpTree getAnySingle() { throw new FlagException(); }
            };
        PerlPatternParser p = new PerlPatternParser( ".", g );
        try { p.parseAtom(); fail( "should be using supplied generator" ); }
        catch (FlagException e) { pass(); }
        }
    
    public void testLit()
        {
        assertEquals( new Text( "a" ), new Text( "a" ) );
        assertDiffer( new Text( "a" ), new Text( "b" ) );
        assertEquals( new Text( "aga" ).hashCode(), new Text( "aga" ).hashCode() );
        }
    
    public void testInitialParserState()
        {
        assertEquals( 0, new PerlPatternParser( "hello" ).getPointer() );
        assertEquals( "hello", new PerlPatternParser( "hello" ).getString() );
        }
    
    public void testLetterAtoms()
        {
        for (char ch = 0; ch < 256; ch += 1)
            if (Character.isLetter( ch ))
                {
                PerlPatternParser p = new PerlPatternParser( "" + ch );
                assertEquals( new Text( "" + ch ), p.parseAtom() );
                assertEquals( 1, p.getPointer() );
                }
        }
    
    public void testEmptyExpression()
        {
        PerlPatternParser p = new PerlPatternParser( "" );
        assertEquals( new Nothing(), p.parseAtom() );
        }
    
    public void testDotAtom()
        { testSimpleSpecialAtom( RegexpTree.ANY, "." ); }
    
    public void testHatAtom()
        { testSimpleSpecialAtom( RegexpTree.SOL, "^" );  }
    
    public void testDollarAtom()
        { testSimpleSpecialAtom( RegexpTree.EOL, "$" ); }
    
    public void testClassesUnimplemented()
        {
        PerlPatternParser p = new PerlPatternParser( "[" );
        try { p.parseAtom(); fail( "should be unimplemented at the moment" ); }
        catch (PerlPatternParser.SyntaxException e) { pass(); }
        }
    
    public void testTerminatorsReturnNull()
        {
        assertEquals( new Nothing(), new PerlPatternParser( "|" ).parseAtom() );
        }
    
    public void testBackslashedAtomsUnimplemented()
        {
        PerlPatternParser p = new PerlPatternParser( "\\" );
        try { p.parseAtom(); fail( "should be unimplemented at the moment" ); }
        catch (PerlPatternParser.SyntaxException e) { pass(); }
        }
    
    public void testNoQuantifier()
        {
        RegexpTree d = RegexpTree.ANY;
        assertSame( d, new PerlPatternParser( "" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "x" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "[" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "(" ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "." ).parseQuantifier( d ) );
        assertSame( d, new PerlPatternParser( "\\" ).parseQuantifier( d ) );
        }
    
    public void testStarQuantifier()
        {
        RegexpTree d = RegexpTree.EOL;
        assertEquals( new ZeroOrMore( d ), new PerlPatternParser( "*" ).parseQuantifier( d ) );
        }
    
    public void testPlusQuantifier()
        {
        RegexpTree d = RegexpTree.SOL;
        assertEquals( new OneOrMore( d ), new PerlPatternParser( "+" ).parseQuantifier( d ) );
        }

    public void testQueryQuantifier()
        {
        RegexpTree d = RegexpTree.ANY;
        assertEquals( new Optional( d ), new PerlPatternParser( "?" ).parseQuantifier( d ) );
        }
    
    public void testUnboundQuantifiers()
        { testUnboundQuantifier( "*" );
        testUnboundQuantifier( "+" );
        testUnboundQuantifier( "?" );
        testUnboundQuantifier( "{" ); }

    /**
    	check that the quantifier string <code>q</code>throws a syntax error if it's
        not preceeded by an atom.
    */
    private void testUnboundQuantifier( String q )
        { PerlPatternParser p = new PerlPatternParser( q ); 
        try { p.parseElement(); fail( "must trap unbound quantifier " + q ); }
        catch (PerlPatternParser.SyntaxException e) { pass(); } }

    public void testUnitSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "x" );
        assertEquals( new Text( "x" ), p.parseSeq() );
        }
    
    public void testSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "^.$" );
        assertEquals( seq3( new StartOfLine(), new AnySingle(), new EndOfLine() ), p.parseSeq() );
        }
    
    public void testAlt()
        {
        PerlPatternParser L = new PerlPatternParser( "abc" );
        PerlPatternParser R = new PerlPatternParser( "def" );
        PerlPatternParser p = new PerlPatternParser( "abc|def" );
        assertEquals( alt( L.parseSeq(), R.parseSeq() ), p.parseAlts() );
        }
    
    protected RegexpTree seq3( RegexpTree a, RegexpTree b, RegexpTree c )
        {
        return Sequence.create( Arrays.asList( new RegexpTree[] {a, b, c} ) );
        }
    
    protected RegexpTree alt( RegexpTree L, RegexpTree R )
        { return Alternatives.create( Arrays.asList( new RegexpTree[] {L, R} ) ); }
    
    public void testPerlParse()
        {
        assertTrue( PerlPatternParser.parse( "this is|a pattern" ) instanceof Alternatives );
        assertTrue( PerlPatternParser.parse( "this is|a pattern", new SimpleGenerator() ) instanceof Alternatives );
        }
    
    public void testOldSeq()
        {
        PerlPatternParser p = new PerlPatternParser( "hello" );
        assertEquals( new Text( "h" ), p.parseAtom() );
        assertEquals( 1, p.getPointer() );
        assertEquals( new Text( "e" ), p.parseAtom() );
        assertEquals( 2, p.getPointer() );
        assertEquals( new Text( "l" ), p.parseAtom() );
        assertEquals( 3, p.getPointer() );
        assertEquals( new Text( "l" ), p.parseAtom() );
        assertEquals( 4, p.getPointer() );
        assertEquals( new Text( "o" ), p.parseAtom() );
        assertEquals( 5, p.getPointer() );
        assertEquals( new Nothing(), p.parseAtom() );
        }
    
    public void testSimpleSpecialAtom( Object wanted, String toParse )
        {
        PerlPatternParser p = new PerlPatternParser( toParse );
        assertEquals( wanted, p.parseAtom() );
        assertEquals( 1, p.getPointer() );
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