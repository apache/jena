/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.test;

// Quick import of test code into JUnit.
// Mega-tests should be broken up.  Can use TestCase.setName to change the name while running.

import java.io.* ;
import junit.framework.* ;

import com.hp.hpl.jena.rdql.* ;
import com.hp.hpl.jena.rdql.parser.* ;

public class TestExpressions extends TestSuite
{
	static final String testSetName = "RDQL - Expressions" ;
	
	public static TestSuite suite()
    {
    	return new TestExpressions(testSetName) ;
    }
    
    private TestExpressions(String name)
    {
    	super(name) ;
    	
        addTest(new TestNumeric("7", 7)) ;
        addTest(new TestNumeric("-3", -3)) ;
        addTest(new TestNumeric("3+4+5", 3+4+5)) ;
        // Test the trees
        addTest(new TestNumeric("(3+4)+5", 3+4+5)) ;
        addTest(new TestNumeric("3+(4+5)", 3+4+5)) ;

        // Precedence
        addTest(new TestNumeric("3*4+5", 3*4+5)) ;
        addTest(new TestNumeric("3*(4+5)", 3*(4+5))) ;

        addTest(new TestNumeric("10-3-5", 10-3-5)) ;
        addTest(new TestNumeric("(10-3)-5", (10-3)-5)) ;
        addTest(new TestNumeric("10-(3-5)", 10-(3-5))) ;
        addTest(new TestNumeric("10-3+5", 10-3+5)) ;
        addTest(new TestNumeric("10-(3+5)", 10-(3+5))) ;

        addTest(new TestNumeric("1<<2", 1<<2)) ;
        addTest(new TestNumeric("1<<2<<2", 1<<2<<2)) ;

        addTest(new TestNumeric("10000>>2", 10000>>2)) ;

        addTest(new TestNumeric("1.5 + 2.5", 1.5+2.5)) ;
        addTest(new TestNumeric("1.5 + 2", 1.5+2)) ;
        
        // Test longs
        // A long is over 32bites signed = +2Gig
        addTest(new TestNumeric("4111222333444", 4111222333444L)) ;
        addTest(new TestNumeric("1234 + 4111222333444", 1234 + 4111222333444L)) ;
        
        // Boolean
        addTest(new TestBoolean("true", false, true)) ;

        addTest(new TestBoolean("false", false, false)) ;

        addTest(new TestBoolean("false || true", false, true)) ;
        addTest(new TestBoolean("false && true", false, false)) ;

        addTest(new TestBoolean("2 < 3", false, 2 < 3)) ;
        addTest(new TestBoolean("2 > 3", false, 2 > 3)) ;
        addTest(new TestBoolean("(2 < 3) && (3<4)", false, (2 < 3) && (3<4))) ;
        addTest(new TestBoolean("(2 < 3) && (3>=4)", false, (2 < 3) && (3>=4))) ;
        addTest(new TestBoolean("(2 < 3) || (3>=4)", false, (2 < 3) || (3>=4))) ;
        addTest(new TestBoolean("2 == 3", false, 2 == 3)) ;

        addTest(new TestBoolean("\"fred\" ne \"joe\"", false, true )) ;
        addTest(new TestBoolean("\"fred\" eq \"joe\"", false, false )) ;
        addTest(new TestBoolean("\"fred\" eq \"fred\"", false, true )) ;
        addTest(new TestBoolean("\"fred\" eq 'fred'", false, true )) ;
        addTest(new TestBoolean("\"fred\" eq 'fr\\ed'", false, true )) ;
        addTest(new TestBoolean("\"fred\" ne \"fred\"", false, false )) ;

        // Escapes in strings
        addTest(new TestBoolean("\"fred\\1\" eq 'fred1'", false, true )) ;
        addTest(new TestBoolean("\"fred2\" eq 'fred\\2'", false, true )) ;
        addTest(new TestBoolean("'fred\\\\3' ne \"fred3\"", false, true )) ;


        addTest(new TestBoolean("\"urn:fred\" eq <urn:fred>", false, true )) ;
        addTest(new TestBoolean("\"urn:fred\" ne <urn:fred>", false, false )) ;

        addTest(new TestBoolean("\"urn:fred/1.5\" ne <urn:fred/1.5>", false, false )) ;
        
        addTest(new TestBoolean("\"aabbcc\" =~ /abbc/", false, true )) ;
        addTest(new TestBoolean("\"aabbcc\" =~ /a..c/", false, true )) ;
        addTest(new TestBoolean("\"aabbcc\" =~ /^aabb/", false, true )) ;
        addTest(new TestBoolean("\"aabbcc\" =~ /cc$/", false, true )) ;
        addTest(new TestBoolean("\"aabbcc\" !~ /abbc/", false, false )) ;
        
        addTest(new TestBoolean("\"aab*bcc\" =~ /ab\\*bc/", false, true )) ;
        addTest(new TestBoolean("\"aabbcc\" ~~ /ab\\\\*bc/", false, true )) ;
        addTest(new TestBoolean("'aabbcc' =~ /B.*B/i", false, true )) ;

        addTest(new TestBoolean("1.5 < 2", false, 1.5 < 2 )) ;
        addTest(new TestBoolean("1.5 > 2", false, 1.5 > 2 )) ;
        addTest(new TestBoolean("1.5 < 2.3", false, 1.5 < 2.3 )) ;
        addTest(new TestBoolean("1.5 > 2.3", false, 1.5 > 2.3 )) ;
        
        // Longs
        addTest(new TestBoolean("4111222333444 > 1234", false, 4111222333444L > 1234)) ;
        addTest(new TestBoolean("4111222333444 < 1234", false, 4111222333444L < 1234L)) ;
        
        // These are false because a failure should occur

        addTest(new TestBoolean("2 < \"fred\"", true, false)) ;
        addTest(new TestBoolean("2 || true", true, false)) ;
    }


    static class TestNumeric extends TestCase
    {
        String s ;
        boolean isDouble = false ;
        long rightAnswer ;
        double rightAnswerDouble ;

        TestNumeric(String _s, long _rightAnswer)
        {
            super("Numeric test : "+_s+" ") ;
            s = _s ;
            rightAnswer = _rightAnswer ;
            isDouble = false ;
        }

        TestNumeric(String _s, double _rightAnswer)
        {
            super("Numeric test : "+_s+" ") ;
            s = _s ;
            rightAnswerDouble = _rightAnswer ;
            isDouble = true ;
        }

        protected void runTest() throws Throwable
        {
            long initTime = 0;
            long parseTime = 0;
            long startTime = 0;
            long stopTime = 0;

            ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes()) ;
            RDQLParser parser = new RDQLParser(in) ;
            startTime = System.currentTimeMillis();

            //parser.CompilationUnit();
            //System.out.println("Input: "+s);

            try {
                parser.Expression() ;
                // Be careful, catch ASAP - JUnit uses errors internally
            }
            catch (Error e)
            {
                fail("Error thrown in parse: "+e) ;
            }

            stopTime = System.currentTimeMillis();
            parseTime = stopTime - startTime;
            //System.out.println("Time: "+parseTime+"ms") ;

            //parser.top().dump(" ");
            Expr e = (Expr)parser.top() ;

            assertTrue("Expression is not ExprNumeric: "+e.getClass().getName() ,
                       (e instanceof ExprNumeric)) ;

            ExprNumeric n = (ExprNumeric)e ;

            Value v = n.eval(null, null) ;

            if ( ! isDouble )
                assertEquals(s+" => "+v.getInt()+" ["+rightAnswer+"]",  v.getInt(), rightAnswer ) ;
            else
                assertEquals(s+" => "+v.getDouble()+" ["+rightAnswerDouble+"]",  v.getDouble(), rightAnswerDouble, 0.0001 ) ;
        }
    } // End of inner class


    static class TestBoolean extends TestCase
    {
        String s ;
        boolean failureCorrect ;
        boolean rightAnswer ;

        TestBoolean(String _s, boolean _failureCorrect, boolean _rightAnswer)
        {
            super("Boolean test : "+_s+" ") ;
            s = _s ;
            failureCorrect = _failureCorrect ;
            rightAnswer = _rightAnswer ;
        }

        protected void runTest() throws Throwable
        {
            long initTime = 0;
            long parseTime = 0;
            long startTime = 0;
            long stopTime = 0;

            ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes()) ;
            RDQLParser parser = new RDQLParser(in) ;
            startTime = System.currentTimeMillis();

            try {
                parser.Expression() ;
            } catch (Error e)
            {
                super.fail("Error throw in parse: "+s) ;
            }

            stopTime = System.currentTimeMillis();
            parseTime = stopTime - startTime;
            //System.out.println("Time: "+parseTime+"ms") ;

            SimpleNode topNode = parser.top() ;
            //topNode.dump("--");

            Expr e = (Expr)topNode ;

            assertTrue("Expression is not ExprBoolean: "+e.getClass().getName(),
                       (e instanceof ExprBoolean) ) ;

            ExprBoolean n = (ExprBoolean)e ;

            Value v = null ;
            boolean result = false ;
            try {
                v = n.eval(null, null) ;
                result = v.getBoolean() ;
            } catch (com.hp.hpl.jena.rdql.EvalFailureException evalEx)
            {
                if ( ! failureCorrect )
                    throw evalEx ;
                result = false ;

            }
            assertEquals(s+" => "+result+" ["+rightAnswer+"]", result, rightAnswer ) ;
        }
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
 *  All rights reserved.
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
