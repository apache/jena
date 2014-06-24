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

import java.util.HashMap ;
import java.util.List ;
import java.util.Map ;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.Context ;

/**
 * ARQ extension to SPARQL which provides for dynamic function invocation
 */
public class E_Call extends ExprFunctionN
{
    private static final String symbol = Tags.tagCall ;
    private Map<String,Expr> functionCache = new HashMap<>();
    private Expr identExpr;
    private List<Expr> argExprs;

    public E_Call(ExprList args)
    {
        this(symbol, args) ;
    }
    
    protected E_Call(String sym, ExprList args)
    {
        super(sym, args) ;
        if (args.size() == 0) {
        	identExpr = null;
        } else {
        	identExpr = args.get(0);
        	argExprs = args.getList().subList(1,args.size()) ; 
        }
    }

    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        //No argument returns unbound
        if (identExpr == null) throw new ExprEvalException("CALL() has no arguments");
        
        //One/More arguments means invoke a function dynamically
        NodeValue func = identExpr.eval(binding, env);
        if (func == null) throw new ExprEvalException("CALL: Function identifier unbound");
        if (func.isIRI()) {
        	Expr e = buildFunction(func.getNode().getURI(), argExprs, env.getContext());
        	if (e == null) throw new ExprEvalException("CALL: Function identifier <" + func.getNode().getURI() + "> does not identify a known function");
        	//Calling this may throw an error which we will just let bubble up
        	return e.eval(binding, env);
        } else {
        	throw new ExprEvalException("CALL: Function identifier not an IRI");
        }
    }
     
    
    @Override
    public Expr copy(ExprList newArgs)       { return new E_Call(newArgs) ; }

    @Override
    public NodeValue eval(List<NodeValue> args, FunctionEnv env) {
        // Instead of evalSpecial, we can rely on the machinery to evaluate the arguments to CALL first.
        // This precludes special forms for CALL first argument.
        // This code here is not usually called - evalSpecial is more general and is the main code path,
        NodeValue func = args.get(0) ;
        if (func == null) throw new ExprEvalException("CALL: Function identifier unbound");
        if (func.isIRI()) {
            ExprList a = new ExprList() ;
            for ( int i = 1 ; i < args.size() ; i++ )
                a.add(args.get(i)) ;
            //Expr e = null ;
            Expr e = new E_Function(func.getNode().getURI(), a);
            //Calling this may throw an error which we will just let bubble up
            return e.eval(null, env) ;
        } else
            throw new ExprEvalException("CALL: Function identifier not an IRI");
    }
    
	@Override
	public NodeValue eval(List<NodeValue> args) {
	    // eval(List, FunctionEnv) should be called.
		throw new ARQInternalErrorException();
	}
	    
	/**
	 * Returns the expr representing the dynamic function to be invoked
	 * <p>
	 * Uses caching wherever possible to avoid 
	 * </p>
	 */
    private Expr buildFunction(String functionIRI, List<Expr> args, Context cxt)
    {
    	//Use our cached version of the expression wherever possible
    	if (functionCache.containsKey(functionIRI))	{
    		return functionCache.get(functionIRI);
    	}    	
    	
    	//Otherwise generate a new function and cache it
    	try
    	{
    		E_Function e = new E_Function(functionIRI, new ExprList(args));
    		e.buildFunction(cxt);
        	functionCache.put(functionIRI, e);
        	return e;
    	} catch (Throwable e) {
    		//Anything goes wrong in creating the function cache a null so we don't retry every time we see this IRI
    		functionCache.put(functionIRI, null);
    		return null;
    	}
        
    }
}
