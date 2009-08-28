/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.rdql;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.query.Expression;
import com.hp.hpl.jena.graph.query.IndexValues;
import com.hp.hpl.jena.graph.query.Valuator;
import com.hp.hpl.jena.graph.query.VariableIndexes;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingIndex;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprBuild;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.ExprVisitor;
import com.hp.hpl.jena.sparql.expr.ExprWalker;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.IndentedWriter;


/** A node that is a RDQL constraint expression that can be evaluated to true or false */
 
abstract class ExprNodeRDQL 
    extends SimpleNode            // So it is parser generated
    implements
        Expression , Valuator     // Part of jena.graph.query.*
        , ExprRDQL                    // Part of the RDQL parser package
{
    public ExprNodeRDQL(RDQLParser p, int i) { super(p, i); }
    public ExprNodeRDQL(int i) { super(i); }

//    // Not a Expr (as in com.hp.hpl.jena.query.expr.Expr)
//    public boolean isExpr() { return false ; }
//    public com.hp.hpl.jena.query.expr.Expr getExpr() { return null ; }

    // -- The ARQ Expr interface
    
    public Expr copySubstitute(Binding binding) { return null ; }

    public Expr copySubstitute(Binding binding, boolean foldConstants) { return null ; }

    public Expr deepCopy() { return null ; }

    public NodeValue eval(Binding binding, FunctionEnv env)
    { return null ; }

    public com.hp.hpl.jena.sparql.expr.NodeValue getConstant()
    { return null ; }

    public ExprVar getExprVar() { return null ; }

    public String getVarName()  { return null ; }
    
    public Var asVar()          { return null ; }

    public void visit(ExprVisitor visitor)
    { 
        if ( visitor instanceof ExprBuild ) return ;
        if ( visitor instanceof ExprWalker.WalkerTopDown ) return ;
        if ( visitor instanceof ExprWalker.WalkerBottomUp ) return ;
        throw new ARQInternalErrorException("Attempt to visit an RDQL expression") ;
    }

    public boolean isSatisfied(Binding binding, FunctionEnv execCxt)
    {
        BindingIndex bInd = new BindingIndex(binding) ;
        VariableIndexes vi = bInd ;
        this.prepare(vi) ;
        IndexValues iv = bInd ;
        return isSatisfied(iv) ;
    }

    
    public boolean isSatisfied(IndexValues env)
    {
        return evalBool(env) ;
    }
    
    @Override
    public void postParse2(Query q)
    {
        super.postParse2(q) ;
    }

    public Set<Var> getVarsMentioned()
    {
        Set<Var> acc = new HashSet<Var>() ;
        varsMentioned(acc) ;
        return acc ;
    }
    
    public Set<String> getVarNamesMentioned()
    {
        Set<String> acc = new HashSet<String>() ;
        varNamesMentioned(acc) ;
        return acc ;
    }
    
    public void varNamesMentioned(Collection<String> acc) 
    {
        if ( this instanceof Q_Var )
        {
            Q_Var v = (Q_Var)this ;
            acc.add(v.getName()) ;
            return ;
        }

        // Not a variable.  Recurse. 
        for ( int i = 0 ; i < argCount() ; i++ )
        {
            Expression e = getArg(i) ;
            if ( e != null )
            {
                ExprNodeRDQL ex = (ExprNodeRDQL)e ;
                ex.varNamesMentioned(acc) ;
            }
        }
    }
    
    public void varsMentioned(Collection<Var> acc)
    {
        if ( this instanceof Q_Var )
        {
            Q_Var v = (Q_Var)this ;
            // Convert to Var as used by the rest of ARQ
            acc.add(Var.alloc(v.getName())) ;
            return ;
        }

        // Not a variable.  Recurse. 
        for ( int i = 0 ; i < argCount() ; i++ )
        {
            Expression e = getArg(i) ;
            if ( e != null )
            {
                ExprNodeRDQL ex = (ExprNodeRDQL)e ;
                ex.varsMentioned(acc) ;
            }
        }
    }
    
    // Per query execution processing.

    public Valuator prepare(VariableIndexes vi)
    {
        //varIndexes = vi ;
        if (children != null)
        {
            for (int i = 0; i < children.length; ++i)
            {
                Object n = children[i];
                if ( n instanceof Expression )
                {
                    ((Expression)n).prepare(vi);
                }
            }
        }
        // This object is its own Valuator (if its an expression)
        // Effect is to return the top node - rest were ignored.
        return this ;
    }

    /**
    Answer the result of evaluating the constraint expression; implements
    Expression.evalObject(*).
    */
    public Object evalObject( IndexValues iv )
    {
        return evalNode( null, iv ) ;
    }
    
    /**
        Answer the result of evaluating the constraint expression as a
        primitive boolean value; implements Expression.evalBool(*).
    */
    public boolean evalBool( IndexValues iv )
    {
        return evalBool( null, iv ) ;
    }
    
    /**
        Answer the result of evaluating the constraint expression with 
        a specified query - helper function to allow isSatisfied() to be
        implemented in terms of the eval methods.
    */
    protected boolean evalBool( Query q, IndexValues iv)
    {
        RDQL_NodeValue v = evalNode( q, iv ) ;
        return v == null ? false : v.getBoolean() ;
    }
    
    /**
        Answer the NodeValue result of evaluating the constraints
        relative to the query and with the specified environment.
        Traps and handles exceptions. This code used to be in
        isSatisfied() until Expressions gained evalObject(), then
        it was factored out so that it could be shared by both
        evalXXX() methods.
    */
    public RDQL_NodeValue evalNode( Query q, IndexValues env )
    {
        try {
            return evalRDQL(q, env) ;
        }
        catch (RDQLEvalFailureException e) //Includes EvalTypeException
        {
            // Check all exceptions possible.
            //expr = null ;
            return null ;
        }
        catch (Exception e)
        {
            ALog.warn(this, "RDQL : general exception!", e) ;
            // Shouldn't happen
            return null ;
        }
    }
    // com.hpl.hpl.jena.graph.query.Expression
    // -- Variables
    public boolean isVariable()        { return false; }
    public String getName()            { return null; } // For variables only

    // -- Constants (literals - but that name is confusing with RDF literals). 
    public boolean isConstant()        { return false; }
    public Object getValue()           { return null; }

    public boolean isFunction()        { return false ; }
    public ExprFunction getFunction()  { return null; }
    
    // -- Functions
    public boolean isApply()           { return false; }
    public String getFun()             { return null; } // For URI of the function
    public int argCount()              { return 0; }    // For functions
    public Expression getArg(int i)    { return null; }

    static final String exprBaseURI = "urn:x-jena:expr:" ; 
    protected String constructURI(String className)
    {
        if ( className.lastIndexOf('.') > -1 )
            className = className.substring(className.lastIndexOf('.')+1) ;
        return exprBaseURI+className ;
    }
    
    public void format(Query query, IndentedWriter writer)
    {
        format(writer) ;
    }
    
    abstract public void format(IndentedWriter writer) ;
    
    // Used in printing a query - not for getting the string
    // value of a expression (which itself must be unquoted)
    @Override
    public String toString()
    {
        return asInfixString() ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
