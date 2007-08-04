/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import java.math.BigDecimal;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;


public class TestExprNumeric extends TestExpr
{
    boolean isDouble = false ;
    
    Number rightAnswer ;

    public TestExprNumeric(String exprStr, Number rightAnswer)
    { this(exprStr, rightAnswer, null, null) ; }
    
    public TestExprNumeric(String exprStr, Number rightAnswer, Query query, Binding env)
    { this(exprStr, rightAnswer, query, env,  TestExpr.NO_FAILURE) ; }
    
    private TestExprNumeric(String exprStr, Number rightAnswer, Query query, Binding env, int failureMode)
    { 
        super("Numeric test : "+exprStr+" ", exprStr, query, env, failureMode) ;
        this.rightAnswer = rightAnswer ;
    }
    
    // ---- integer
    
    public TestExprNumeric(String exprStr, long rightAnswer)
    { this(exprStr, new Long(rightAnswer)) ; }

    public TestExprNumeric(String exprStr, BigDecimal rightAnswer)
    { this(exprStr, (Number)rightAnswer) ; }

    public TestExprNumeric(String exprStr, double rightAnswer)
    { this(exprStr, new Double(rightAnswer)) ; }
    
    public TestExprNumeric(String exprStr, long rightAnswer, Query query, Binding env)
    { this(exprStr, new Long(rightAnswer), query, env) ; }

    public TestExprNumeric(String exprStr, BigDecimal rightAnswer, Query query, Binding env)
    { this(exprStr, (Number)rightAnswer, query, env) ; }

    public TestExprNumeric(String exprStr, double rightAnswer, Query query, Binding env)
    { this(exprStr, new Double(rightAnswer), query, env) ; }
    
    void checkExpr(Expr expr) {}

    void checkValue(Expr expr, NodeValue nodeValue)
    {
        if ( ! evalCorrect() )
            fail(exprString+" => "+expr+" :: Expected eval exception but got: "+nodeValue) ;

        if ( ! nodeValue.isNumber() )
            fail(exprString+": Not a number for numeric expression: "+nodeValue) ;

        
        if ( rightAnswer instanceof Long )
        {
            if ( ! nodeValue.isInteger() )
                fail("Right answer is an integer: "+rightAnswer+": got "+nodeValue) ;
            assertEquals("Parse "+exprString+" ==> "+expr+" ", 
                         ((Long)rightAnswer).longValue(), 
                         nodeValue.getInteger().longValue() ) ;
            return ;
        }

        if ( rightAnswer instanceof BigDecimal )
        {
            if ( ! nodeValue.isDecimal() )
                fail("Rigth answer is a decimal: "+rightAnswer+": got "+nodeValue) ;
            assertEquals("Parse "+exprString+" ==> "+expr+" ", 
                         rightAnswer, nodeValue.getDecimal()) ; 
            return ;
        }
        
        if ( rightAnswer instanceof Double )
        {
            if ( ! nodeValue.isDouble() )
                fail("Rigth answer is a double: "+rightAnswer+": got "+nodeValue) ;
            assertEquals("Parse "+exprString+" ==> "+expr+" ", 
                         ((Double)rightAnswer).doubleValue(), nodeValue.getDouble(), 0.000001) ; 
            return ;
        }
        
        fail(exprString+": Unrecognized kind of NodeValue for numeric expression: "+nodeValue) ;
    }
    
    void checkException(Expr expr, Exception ex)
    {
        if ( ! failureCorrect() )
            fail(exprString+" => "+expr+" :: Exception: "+ex) ;

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