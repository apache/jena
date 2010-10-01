/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.ArrayList ;
import java.util.List ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** A function which takes N arguments (N may be variable e.g. regex) */
 
public abstract class ExprFunctionN extends ExprFunction
{
    protected ExprList args = null ;
    
    protected ExprFunctionN(String fName, Expr... args)
    {
        this(fName, argList(args)) ;
    }
    
    protected ExprFunctionN(String fName, ExprList args)
    {
        super(fName) ;
        this.args = args ;
    }

    private static ExprList argList(Expr[] args)
    {
        ExprList exprList = new ExprList() ;
        for ( Expr e : args )
            if ( e != null )
                exprList.add(e) ;
        return exprList ;
    }


    @Override
    public Expr getArg(int i)
    {
        i = i-1 ;
        if ( i >= args.size() )
            return null ;
        return args.get(i) ;
    }

    @Override
    public int numArgs() { return args.size() ; }
    
    @Override
    public List<Expr> getArgs() { return args.getList() ; }

    @Override
    public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        ExprList newArgs = new ExprList() ;
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            Expr e = getArg(i) ;
            e = e.copySubstitute(binding, foldConstants) ;
            newArgs.add(e) ;
        }
        return copy(newArgs) ;
    }

    @Override
    public Expr applyNodeTransform(NodeTransform transform)
    {
        ExprList newArgs = new ExprList() ;
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            Expr e = getArg(i) ;
            e = e.applyNodeTransform(transform) ;
            newArgs.add(e) ;
        }
        return copy(newArgs) ;
    }
    
    /** Special form evaluation (example, don't eval the arguments first) */
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) { return null ; }

    @Override
    final public NodeValue eval(Binding binding, FunctionEnv env)
    {
        NodeValue s = evalSpecial(binding, env) ;
        if ( s != null )
            return s ;
        
        List<NodeValue> argsEval = new ArrayList<NodeValue>() ; 
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            NodeValue x = eval(binding, env, getArg(i)) ;
            argsEval.add(x) ;
        }
        return eval(argsEval, env) ;
    }
    
    public NodeValue eval(List<NodeValue> args, FunctionEnv env) { return eval(args) ; }

    protected abstract NodeValue eval(List<NodeValue> args) ;

    protected abstract Expr copy(ExprList newArgs) ;
    
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform, ExprList exprList) { return transform.transform(this, exprList) ; }

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
