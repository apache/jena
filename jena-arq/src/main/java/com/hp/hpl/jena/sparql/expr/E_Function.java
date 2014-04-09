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

import java.util.List ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.Function ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.function.FunctionFactory ;
import com.hp.hpl.jena.sparql.function.FunctionRegistry ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/** SPARQL filter function */

public class E_Function extends ExprFunctionN
{
    private static final String name = "function" ;
    public static boolean WarnOnUnknownFunction = true ;
    private String functionIRI ;
    
    // Only set after a copySubstitute has been done by PlanFilter.
    // at which point this instance if not part of the query abstract syntax.  
    private Function function = null ;
    private boolean functionBound = false ;

    public E_Function(String functionIRI, ExprList args)
    {
        super(name, args) ;
        this.functionIRI = functionIRI ; 
    }

    @Override
    public String getFunctionIRI() { return functionIRI ; }
    
    // The Function subsystem takes over evaluation via SpecialForms.
    // This is merely to allow "function" to behave as special forms
    // (this is discouraged).
    // Doing the function call in evalSpecial maintains the old 
    // interface to functions.
    
    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        // Only needed because some tests call straight in.
        // Otherwise, the buildFunction() calls should have done everything
        if ( ! functionBound  )
            buildFunction(env.getContext()) ;
        if ( function == null )
            throw new ExprEvalException("URI <"+getFunctionIRI()+"> not bound") ;
        NodeValue r = function.exec(binding, args, getFunctionIRI(), env) ;
        return r ;
    }
    
    @Override
    public NodeValue eval(List<NodeValue> args)
    {
        // For functions, we delay argument evaluation to the "Function" heierarchy
        // so applications can add their own functional forms.
        throw new ARQInternalErrorException() ;
    }

    public void buildFunction(Context cxt)
    {
        try { bindFunction(cxt) ; }
        catch (ExprException ex)
        {
            if ( WarnOnUnknownFunction )
                ARQ.getExecLogger().warn("URI <"+functionIRI+"> has no registered function factory") ;
        }
    }
    
    private void bindFunction(Context cxt)
    {
        if ( functionBound )
            return ;
        
        FunctionRegistry registry = chooseRegistry(cxt) ;
        FunctionFactory ff = registry.get(functionIRI) ;
        
        if ( ff == null )
        {
            functionBound = true ;
            throw new ExprEvalException("URI <"+functionIRI+"> not found as a function") ;
        }
        function = ff.create(functionIRI) ;
        function.build(functionIRI, args) ;
        functionBound = true ;
    }
    
    private FunctionRegistry chooseRegistry(Context context)
    {
        FunctionRegistry registry = FunctionRegistry.get(context) ;
        if ( registry == null )
            registry = FunctionRegistry.get() ;
        return registry ;
    }
    
    @Override
    public String getFunctionPrintName(SerializationContext cxt)
    {
        return FmtUtils.stringForURI(functionIRI, cxt) ;
    }

    @Override
    public String getFunctionName(SerializationContext cxt)
    {
        return FmtUtils.stringForURI(functionIRI, cxt) ;
    }


    @Override
    public Expr copy(ExprList newArgs)
    {
        return new E_Function(getFunctionIRI(), newArgs) ;
    }
}
