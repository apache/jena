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
    public Expr copySubstitute(Binding binding)
    {
        ExprList newArgs = new ExprList() ;
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            Expr e = getArg(i) ;
            e = e.copySubstitute(binding) ;
            newArgs.add(e) ;
        }
        return copy(newArgs);
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
        
        List<NodeValue> argsEval = new ArrayList<>() ;
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            NodeValue x = eval(binding, env, getArg(i)) ;
            argsEval.add(x) ;
        }
        return eval(argsEval, env) ;
    }
    
    public NodeValue eval(List<NodeValue> args, FunctionEnv env) { return eval(args) ; }

    public abstract NodeValue eval(List<NodeValue> args) ;

    public abstract Expr copy(ExprList newArgs) ;
    
    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform, ExprList exprList) { return transform.transform(this, exprList) ; }

}
