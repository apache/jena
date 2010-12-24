/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
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
    protected NodeValue eval(List<NodeValue> args)
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
    protected Expr copy(ExprList newArgs)
    {
        return new E_Function(getFunctionIRI(), newArgs) ;
    }
}

/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
