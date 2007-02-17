/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query.expr;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.function.Function;
import com.hp.hpl.jena.query.function.FunctionFactory;
import com.hp.hpl.jena.query.function.FunctionRegistry;
import com.hp.hpl.jena.query.serializer.SerializationContext;
import com.hp.hpl.jena.query.util.Context;
import com.hp.hpl.jena.query.util.FmtUtils;

/** SPARQL filter function
 * @author Andy Seaborne
 * @version $Id: E_Function.java,v 1.40 2007/02/06 17:05:51 andy_seaborne Exp $
 */

public class E_Function extends ExprFunctionN
{
    private static final String name = "function" ;
    public static boolean WarnOnUnknownFunction = true ;
    private String functionIRI ;
    
    // Only set after a copySubstitute has been done by PlanFilter.
    // at which point this isnatnce if not part of the query abstract syntax.  
    private Function function = null ;
    private boolean functionBound = false ;

//    public E_Function(String name, Expr arg)
//    {
//        super(symbol) ;
//        this.name = name ;
//        this.args = new ArrayList() ;
//        args.add(arg) ;
//    }
    
    public E_Function(String functionIRI, List args)
    {
        super(name, args) ;
        this.functionIRI = functionIRI ; 
    }

    public String getFunctionIRI() { return functionIRI ; }
    
    public void buildFunction(Context context)
    {
        try { bindFunction(context) ; }
        catch (ExprException ex)
        {
            if ( WarnOnUnknownFunction )
                LogFactory.getLog(E_Function.class).warn("URI <"+functionIRI+"> has no registered function factory") ;
        }
    }
    
    private FunctionRegistry chooseRegistry(Context context)
    {
        FunctionRegistry registry = FunctionRegistry.get(context) ;
        if ( registry == null )
            registry = FunctionRegistry.get() ;
        return registry ;
    }
    
    private void bindFunction(Context context)
    {
        if ( functionBound )
            return ;
        
        FunctionRegistry registry = chooseRegistry(context) ;
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
    
    public NodeValue eval(Binding binding, Context cxt)
    {
        // Only needed because some tests call straight in
        // Otherwise, the buildFunction() calls should have done everything
        if ( ! functionBound )
            // Allow breakpoint for this case.
            buildFunction(cxt) ;
        if ( function == null )
            throw new ExprEvalException("URI <"+getFunctionIRI()+"> not bound") ;
        NodeValue r = function.exec(binding, args, getFunctionIRI(), cxt) ;
        return r ;
    }
    
    public String getFunctionPrintName(SerializationContext cxt)
    {
        return FmtUtils.stringForURI(functionIRI, cxt) ;
    }

    protected Expr copy(List newArgs)
    {
        return new E_Function(getFunctionIRI(), newArgs) ;
    }
}

/*
 *  (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
