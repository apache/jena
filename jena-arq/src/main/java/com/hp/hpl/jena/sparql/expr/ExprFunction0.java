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

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** An expression that is constant (does not depend on evaluating a sub expression). */

public abstract class ExprFunction0 extends ExprFunction
{
    protected ExprFunction0(String fName) { this(fName, null) ; }
    
    protected ExprFunction0(String fName, String opSign)
    {
        super(fName, opSign) ;
    }

    @Override
    public Expr getArg(int i)       { return null ; }
    
    @Override
    public int hashCode()           { return getFunctionSymbol().hashCode() ; }

    @Override
    public int numArgs()            { return 0 ; }
    
    // ---- Evaluation
    
    @Override
    final public NodeValue eval(Binding binding, FunctionEnv env)
    {
        return eval(env) ;
    }
   
    public abstract NodeValue eval(FunctionEnv env)  ;
    
    @Override
    final public Expr applyNodeTransform(NodeTransform transform)
    {
        // Nothing to transform. 
        return copy() ;
    }
    
    public abstract Expr copy() ;
    
    @Override
    final public Expr copySubstitute(Binding binding)
    {
        return copy() ;
    }
    
    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform) { return transform.transform(this) ; }
}
