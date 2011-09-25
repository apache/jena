/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.junit;

import java.io.ByteArrayInputStream ;

import junit.framework.TestCase ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.lang.arq.ARQParser ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

/** An expression test. */

public abstract class TestExpr extends TestCase
{
    public final static int NO_FAILURE    = 100 ;
    public final static int PARSE_FAIL    = 250 ;   // Parser should catch it.
    public final static int EVAL_FAIL     = 200 ;   // Parser should pass it but eval should fail it
    
    String testName ;
    String exprString ;
    Query query ;
    Binding binding ;
    int failureMode ;
    boolean doEval ;
    
    // Global default enviromnent - including the function registry
    Context context = ARQ.getContext().copy();

    protected TestExpr(String label, String expression, Query queryCxt, Binding env, int failureOutcome) 
    {
        super() ;
        testName = label ;
        doEval = true ;
        
        // () in names causes display to be truncated in Eclipse which seems to be
        // compensating for TestCase.toString() (JUnit 3.8)
        // which puts (class) on the end of test case names.
        
        String n = label.replace('(','[').replace(')',']') ;
        switch (failureOutcome)
        {
            case NO_FAILURE: break ;
            case PARSE_FAIL: n = n + " [Parse fail]" ; break ;
            case EVAL_FAIL: n = n + " [Eval fail]" ; break ;
            default:        n = n + " [Unknown fail]" ; break ;
            
        }
        
        setName(n) ;
        
        exprString = expression ;
        if ( queryCxt == null )
            queryCxt = QueryFactory.make() ;
        query = queryCxt ;
        if ( env == null )
            env = BindingFactory.create() ;
        binding = env ;
        this.failureMode = failureOutcome ;
    }

    @Override
    protected void runTest() throws Throwable
    {
        Expr expr = null ;
        try {
            expr = parse(exprString) ;
        }
        catch (Error err)
        {
            fail("Error thrown in parse: "+err) ;
        }
        catch (Exception ex)
        {
            if ( failureMode != TestExpr.PARSE_FAIL )
                fail("Unexpected parsing failure: "+ex) ;
            
            checkException(expr, ex) ;
            return ;
        }

        if ( failureMode == TestExpr.PARSE_FAIL )
        {
            fail("Test should have failed in parsing: "+expr) ;
            return ;
        }
        
        Expr expr2 = expr.deepCopy() ;
        if ( ! expr.equals(expr2) )
        {
            System.out.println("Expr:  "+expr) ;
            System.out.println("Expr2: "+expr2) ;
            assertEquals(expr, expr2) ;
        }
        checkExpr(expr) ;
        
        if ( !doEval )
            return ;
        
        try { 
            FunctionEnv env = new FunctionEnvBase(context) ; 
            NodeValue v = expr.eval(binding, env) ;
            checkValue(expr, v) ;
        }
        catch (NullPointerException ex)
        { throw ex ; }
        catch (Exception ex)
        {
            checkException(expr, ex) ;
        }
    }
    
    private Expr parse(String exprString) throws Throwable
    {
        return ExprUtils.parse(query, exprString, false) ;
    }
    
    private Expr parseSPARQL(ByteArrayInputStream in) throws Throwable
    {
        try {
            ARQParser parser = new ARQParser(in) ;
            parser.setQuery(query) ;
            return parser.Expression() ;
        }
        catch (com.hp.hpl.jena.sparql.lang.arq.ParseException ex)
        { throw new QueryParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn) ; }
        catch (com.hp.hpl.jena.sparql.lang.arq.TokenMgrError tErr)
        { throw new QueryParseException(tErr.getMessage(),-1,-1) ; }
        catch (Error err)
        {
            String tmp = err.getMessage() ;
            if ( tmp == null )
                throw new QueryParseException(err,-1, -1) ;
            throw new QueryParseException(tmp,-1, -1) ;
        }
    }
    
    protected boolean failureCorrect() { return failureMode != NO_FAILURE ; }
    protected boolean evalCorrect() { return failureMode != EVAL_FAIL ; }
    
    abstract void checkExpr(Expr expr) ;
    abstract void checkValue(Expr expr, NodeValue nodeValue) ;
    abstract void checkException(Expr expr, Exception ex) ;
    
    // Junit/TestCase (3.8, 3.8.1 at least) mangles the toString
    @Override
    public String toString() { return testName ; }
    
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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