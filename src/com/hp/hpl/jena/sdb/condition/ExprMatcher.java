/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.condition;

import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.query.expr.*;
import com.hp.hpl.jena.query.util.ExprUtils;

/**
 * Matches an expression template to a query expression.  
 * @author Andy Seaborne
 * @version $Id$
 */

public class ExprMatcher
{
    // ?? Package for matcher : inc matcher library
    
    Expr pattern ; 
    
    public ExprMatcher(String template)
    {
        this(ExprUtils.parseExpr(template)) ;
    }

    public ExprMatcher(Expr pattern)
    { 
        this.pattern = pattern ;
    }
    
    // static??
    // Takes a set of restrictions on the expression (bindings for named variables)
    // Returns what variables are bound to.
    public ResultMap matches(ActionMap x, Expr expression, ResultMap rm, CalloutMap cMap)
    {
        MatchOne m = new MatchOne(x, expression, rm, cMap) ;
        try {
            pattern.visit(m) ;
        } catch (NoMatch ex)
        { 
            System.out.println("NoMatch: "+ex.getMessage()) ;
            return null ;
        } 
        return rm ;
    }

    public static void run()
    {
//        runOne(ExprUtils.parseExpr("regex(?x , 'smith')") , ExprUtils.parseExpr("regex(?a1 , ?a2)")) ;
//        runOne(ExprUtils.parseExpr("regex(?x , 'smith')") , ExprUtils.parseExpr("regex(?a1 , ?a2, ?a3)")) ;
//        runOne(ExprUtils.parseExpr("regex(?x , 'smith', 'i')") , ExprUtils.parseExpr("regex(?a1 , ?a2, ?a3)")) ;
//        runOne(ExprUtils.parseExpr("regex(?x , 'smith', 'i')") , ExprUtils.parseExpr("?x + ?y")) ;
//        
//        runOne(ExprUtils.parseExpr("regex(str(?x) , 'smith')") , ExprUtils.parseExpr("regex(str(?a1) , ?a2)")) ;
//        
//        // Matches but should it?
//        runOne(ExprUtils.parseExpr("regex(str(?x) , 'smith')") , ExprUtils.parseExpr("regex(?a1 , ?a2)")) ;
//
//        // a3 is VarAction
//        runOne(ExprUtils.parseExpr("regex(str(?x) , 'smith')") , ExprUtils.parseExpr("regex(?a3 , ?a2)")) ;
//        runOne(ExprUtils.parseExpr("regex(?x , 'smith')") , ExprUtils.parseExpr("regex(str(?a1) , ?a2)")) ;

        runOne(ExprUtils.parseExpr("regex(?x , 'smith')") , ExprUtils.parseExpr("regex(<urn:xyz>(?a1) , ?a2)")) ;

    }
    
    
    public static void runOne(Expr e, Expr p)
    {
        System.out.println("Expr:    "+e) ;
        System.out.println("Pattern: "+p) ;
        
        ExprMatcher eMatch = new ExprMatcher(p) ;
        ActionMap m = new ActionMap() ;
        ResultMap rm = new ResultMap() ;
        CalloutMap cm = new CalloutMap() ;
        
        m.put("a1", new AssignAction(rm)) ;
        m.put("a2", new AssignAction(rm)) ;
        m.put("a3", new VarAction(rm)) ;

        cm.put("urn:xyz", new SpecialFunction()) ;
        
        rm = eMatch.matches(m, e, rm, cm) ;
        if ( rm == null )
        {
            System.out.println("**** No match") ;
            System.out.println() ;
            return ;
        }
        System.out.println("**** Match:") ;
        for ( String x : rm.keySet() )
        {
            Expr exprMatch = rm.get(x) ;
            System.out.printf("?%-4s ==>>  %s\n", x, exprMatch) ;
        }
        System.out.println() ;
    }
}

interface MatchAction
{
    void invoke(String vn, Expr expr) ;
}

//class PrintAction implements MatchAction
//{
//    public void invoke(String vn, Expr expr) { System.out.println("?"+vn+": "+expr) ; }
//}

class VarAction extends AssignAction
{
    VarAction(ResultMap rMap) { super(rMap) ; }  

    @Override
    public void invoke(String vn, Expr expr)
    {
        if ( ! expr.isVariable() )
            throw new NoMatch("VarAction: Not a variable: "+expr) ;
        super.invoke(vn, expr) ;
    }
}


class AssignAction implements MatchAction
{
    private ResultMap rMap ;
    AssignAction(ResultMap rMap) { this.rMap = rMap ; }  
    public void invoke(String varName, Expr expr)
    {
        rMap.put(varName, expr) ;
    }
    
}

interface SpecialAction { public boolean callout(String fn, List args) ; }

class SpecialFunction implements SpecialAction
{

    public boolean callout(String fn, List args)
    {
        System.out.println("Call: "+fn+" "+args) ;
        return true ;
    }
    
}

class ActionMap  extends HashMap<String, MatchAction> {}
class CalloutMap extends HashMap<String, SpecialAction> {}
class ResultMap  extends HashMap<String, Expr> {}

class MatchWalker extends ExprWalker
{
    public MatchWalker(ExprVisitor visitor)
    {
        super(visitor) ;
        
    }
}

class NoMatch extends RuntimeException
{
    //NoMatch() { this(null) ; }
    NoMatch(String msg) { super(msg) ; }
}

// Walk the pattern
class MatchOne implements ExprVisitor
{
    private Expr target ;
    private ActionMap aMap ;
    private ResultMap rMap ;
    private CalloutMap cMap ;
    
    //public void  
    MatchOne(ActionMap aMap, Expr target, ResultMap rMap, CalloutMap cMap)
    { 
        this.aMap = aMap ;
        this.rMap = rMap ;
        this.cMap = cMap ;
        this.target = target ;
    }
    
    public void startVisit()
    {}

    public void visit(ExprFunction patExpr)
    {
        String uri = patExpr.getFunctionIRI() ;
        
        if ( uri != null && cMap.containsKey(uri) )
        {
            if( ! cMap.get(uri).callout(uri, patExpr.getArgs()) )
                throw new NoMatch("Function callout rejected match") ;
            return ;
        }
        
        if ( ! ( target instanceof ExprFunction ) )
            throw new NoMatch("Not an ExprFunction: "+target) ;
        
        ExprFunction funcTarget = (ExprFunction)target ;
        
        if ( ! patExpr.getFunctionSymbol().equals(funcTarget.getFunctionSymbol()) )
            throw new NoMatch("Different function symbols: "+patExpr.getFunctionSymbol().getSymbol()+" // "+funcTarget.getFunctionSymbol().getSymbol()) ;
        
        if ( patExpr.numArgs() != funcTarget.numArgs() )
            throw new NoMatch("Different arity: "+patExpr.numArgs()+"/"+funcTarget.numArgs()) ;
        
        for ( int i = 1 ; i <= funcTarget.numArgs() ; i++ )
        {
            Expr p = patExpr.getArg(i) ;
            Expr e = funcTarget.getArg(i) ;
            
            MatchOne m = new MatchOne(aMap, e, rMap, cMap) ;
            p.visit(m) ;
        }
    }

    public void visit(NodeValue nv)
    {
        if ( ! ( target instanceof NodeValue ) )
            throw new NoMatch("Not a NodeValue") ;
        if ( ! nv.equals(target.getConstant()) )
            throw new NoMatch("Different value: "+nv+" & "+target.getConstant()) ;
    }

    MatchAction defaultAction = new MatchAction()
    {
        public void invoke(String varName, Expr expr) { System.out.println("Default: ?"+varName+": "+expr) ; }
    } ;
    
    public void visit(NodeVar patternVar)
    {
        String vn = patternVar.getVarName() ;
        if ( aMap.containsKey(vn) )
        {
            MatchAction a = aMap.get(vn) ;
            a.invoke(vn, target) ;
            return ;
        }
        
        defaultAction.invoke(vn, target) ;
    }

    public void finishVisit()
    {}
    
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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