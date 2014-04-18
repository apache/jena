/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.expr;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

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
        return eval(varNode, binding, env) ;
    }

    static NodeValue eval(Var v, Binding binding, FunctionEnv env)
    {
        if ( binding == null )
            throw new VariableNotBoundException("Not bound: (no binding): "+v) ;
        Node nv = binding.get(v) ;
        if ( nv == null )
            throw new VariableNotBoundException("Not bound: variable "+v) ;
        // Wrap as a NodeValue.
        return NodeValue.makeNode(nv) ;
    }
    
    @Override
    public Expr copySubstitute(Binding binding)
    {
        Var v = varNode ;  
        if ( binding == null || !binding.contains(v) )
            return new ExprVar(v) ;
        Node v2 = binding.get(v);
        return v2.isVariable() ? new ExprVar(v2) : eval(binding, null) ;
    }
    
    @Override
    public Expr applyNodeTransform(NodeTransform transform)
    {
        Node node = transform.convert(varNode) ;
        if ( Var.isVar(node))
            return new ExprVar(Var.alloc(node)) ;
        return NodeValue.makeNode(node) ;
    }
    
    public Expr copy(Var v)  { return new ExprVar(v) ; }
    
    
    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    
    public Expr apply(ExprTransform transform)  { return transform.transform(this) ; }
    
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

    // ??? Just use format?
    @Override
    public String toString()        { return varNode.toString() ; }
}
