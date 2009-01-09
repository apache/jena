/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** An expression that is a variable in an expression. */
 
public class ExprVar extends ExprNode
{
    // AKA ExprFunction0
    protected Var varNode = null ;
    
    public ExprVar(String name) { varNode = Var.alloc(name) ; }
    public ExprVar(Node n)
    { 
        if ( ! n.isVariable() )
            throw new ARQInternalErrorException("Attempt to create a NodeVar from a non variable Node: "+n) ;
        varNode = Var.alloc(n) ;
    }
    
    public ExprVar(Var v)
    { 
        varNode = v ;
    }
    
    @Override
    public NodeValue eval(Binding binding, FunctionEnv env)
    {
        if ( binding == null )
            throw new VariableNotBoundException("Not bound: (no binding): "+varNode) ;
        Node v = binding.get(varNode) ;
        if ( v == null )
            throw new VariableNotBoundException("Not bound: variable "+varNode) ;
        // Wrap as a NodeValue.
        return NodeValue.makeNode(v) ;
    }

    @Override
    public Expr copySubstitute(Binding binding, boolean foldConstants)
    {
        if ( binding == null || !binding.contains(varNode) )
            return copy() ;
        return eval(binding, null) ;
//        catch (VariableNotBoundException ex)
//        {
//            ALog.warn(this, "Failed to eval bound variable (was bound earlier!)");
//            throw ex ;
//        }
    }
    
    public Expr copy()  { return new ExprVar(varNode) ; }
    
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    
    public void format(Query query, IndentedWriter out)
    {
        out.print('?') ;
        out.print(varNode.getName()) ;
    }
    
    @Override
    public int hashCode() { return varNode.hashCode() ; }
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! ( other instanceof ExprVar ) )
            return false ;
        ExprVar nvar = (ExprVar)other ;
        return getVarName().equals(nvar.getVarName()) ;
    }
    
    @Override
    public boolean isVariable() { return true ; }
    /** @return Returns the name. */
    @Override
    public String getVarName()  { return varNode.getName() ; }
    @Override
    public ExprVar getExprVar() { return this ; }
    @Override
    public Var asVar()          { return varNode ; }
    public Node getAsNode()     { return varNode ; }
    
    
    public String toPrefixString()  { return varNode.toString() ; }
    // As an expression (aggregators override this).
    public String asSparqlExpr()    { return  varNode.toString() ; }

//    // ??? Just use format?
//    public String toString()        { return varNode.toString() ; }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
