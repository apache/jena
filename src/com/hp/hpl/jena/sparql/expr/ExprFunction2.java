/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import org.openjena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;


/** A function of two arguments */
 
public abstract class ExprFunction2 extends ExprFunction
{
    protected final Expr expr1 ;
    protected final Expr expr2 ;

    protected ExprFunction2(Expr expr1, Expr expr2, String fName) { this(expr1, expr2, fName, null) ; }
    
    protected ExprFunction2(Expr expr1, Expr expr2, String fName, String opSign)
    {
        super(fName, opSign) ;
        this.expr1 = expr1 ;
        this.expr2 = expr2 ;
    }
    
    public Expr getArg1() { return expr1 ; }
    public Expr getArg2() { return expr2 ; }
    
    @Override
    public Expr getArg(int i)
    {
        if ( i == 1 )
            return expr1 ; 
        if ( i == 2 )
            return expr2 ; 
        return null ;
    }
    
    @Override
    public int numArgs() { return 2 ; }
    
    // ---- Evaluation
    
    @Override
    public int hashCode()
    {
        return getFunctionSymbol().hashCode() ^
                Lib.hashCodeObject(expr1) ^
                Lib.hashCodeObject(expr2) ;
    }

    @Override
    final public NodeValue eval(Binding binding, FunctionEnv env)
    {
        NodeValue s = evalSpecial(binding, env) ;
        if ( s != null )
            return s ;
        
        NodeValue x = eval(binding, env, expr1) ;
        NodeValue y = eval(binding, env, expr2) ;
        return eval(x, y, env) ;
    }
    
    /** Special form evaluation (example, don't eval the arguments first) */
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) { return null ; } 
    
    public NodeValue eval(NodeValue x, NodeValue y, FunctionEnv env) { return eval(x,y) ; }

    public abstract NodeValue eval(NodeValue x, NodeValue y) ; 

    // ---- Duplication
    
    @Override
    final public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        Expr e1 = (expr1 == null ? null : expr1.copySubstitute(binding, foldConstants)) ;
        Expr e2 = (expr2 == null ? null : expr2.copySubstitute(binding, foldConstants)) ;
        
        if ( foldConstants)
        {
            try {
                if ( e1 != null && e2 != null && e1.isConstant() && e2.isConstant() )
                    return eval(e1.getConstant(), e2.getConstant()) ;
            } catch (ExprEvalException ex) { /* Drop through */ }
        }
        return copy(e1, e2) ;
    }
    

    @Override
    final public Expr applyNodeTransform(NodeTransform transform)
    {
        Expr e1 = (expr1 == null ? null : expr1.applyNodeTransform(transform)) ;
        Expr e2 = (expr2 == null ? null : expr2.applyNodeTransform(transform)) ;
        return copy(e1, e2) ;
    }


    public abstract Expr copy(Expr arg1, Expr arg2) ;

    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform, Expr arg1, Expr arg2) { return transform.transform(this, arg1, arg2) ; }

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
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
 */
