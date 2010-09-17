/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import org.openjena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** A function that has a single argument */
 
public abstract class ExprFunction1 extends ExprFunction
{
    protected final Expr expr ;

    protected ExprFunction1(Expr expr, String fName) { this(expr, fName, null) ; }
    
    protected ExprFunction1(Expr expr, String fName, String opSign)
    {
        super(fName, opSign) ;
        this.expr = expr ;
    }

    public Expr getArg() { return expr ; }

    @Override
    public Expr getArg(int i)
    {
        if ( i == 1 )
            return expr ; 
        return null ;
    }
    
    @Override
    public int hashCode()
    {
        return getFunctionSymbol().hashCode() ^ Lib.hashCodeObject(expr) ;
    }

    @Override
    public int numArgs() { return 1 ; }
    
    // ---- Evaluation
    
    @Override
    final public NodeValue eval(Binding binding, FunctionEnv env)
    {
        NodeValue s = evalSpecial(binding, env) ;
        if ( s != null )
            return s ;
        
        NodeValue x = eval(binding, env, expr) ;
        return eval(x, env) ;
    }
    
    // Ideally, we would only have the FunctionEnv form but that break compatibility. 
    public NodeValue eval(NodeValue v, FunctionEnv env) { return eval(v) ; }
    public abstract NodeValue eval(NodeValue v) ;
    
    // Allow special cases.
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) { return null ; } 
    
    @Override
    final public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        Expr e = (expr == null ? null : expr.copySubstitute(binding, foldConstants)) ;
        
        if ( foldConstants)
        {
            try {
                if ( e != null && e.isConstant() )
                    return eval(e.getConstant(), new FunctionEnvBase()) ;
            } catch (ExprEvalException ex) { /* Drop through */ }
        }
        return copy(e) ;
    }

    @Override
    final public Expr applyNodeTransform(NodeTransform transform)
    {
        Expr e = (expr == null ? null : expr.applyNodeTransform(transform)) ;
        return copy(e) ;
    }
    
    public abstract Expr copy(Expr expr) ;
    
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform, Expr sub) { return transform.transform(this, sub) ; }
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
