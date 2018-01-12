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

package org.apache.jena.sparql.expr;

import java.util.List ;

import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.function.Function ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.function.FunctionFactory ;
import org.apache.jena.sparql.function.FunctionRegistry ;
import org.apache.jena.sparql.function.js.EnvJavaScript;
import org.apache.jena.sparql.function.js.FunctionJavaScript;
import org.apache.jena.sparql.serializer.SerializationContext ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.FmtUtils ;

/** SPARQL filter function */

public class E_Function extends ExprFunctionN
{
    private static final String name = "function" ;
    public static boolean WarnOnUnknownFunction = true ;
    private String functionIRI ;
    private String javaScriptFunction = null; 
    
    private Function function = null ;
    private boolean functionBound = false ;

    public E_Function(String functionIRI, ExprList args) {
        super(name, args) ;
        this.functionIRI = functionIRI ;
        if ( functionIRI.startsWith(ARQConstants.JavaScriptURI) )
            javaScriptFunction = functionIRI.substring(ARQConstants.JavaScriptURI.length());
    }

    @Override
    public String getFunctionIRI() { return functionIRI ; }
    
    // The Function subsystem takes over evaluation via SpecialForms.
    // This allows a "function" to behave as a special form (this is discouraged).
    
    @Override
    public NodeValue evalSpecial(Binding binding, FunctionEnv env) {
        if ( javaScriptFunction != null ) {
            // JavaScript 
            EnvJavaScript jsEnv = EnvJavaScript.get();
            if ( jsEnv == null ) {
                ARQ.getExecLogger().warn("JavaScript function found '"+javaScriptFunction+"' but no JavaScript library installed");
                throw new ExprUndefFunction("No JavaScript function library", javaScriptFunction) ;
            }
            // FunctionJavaScript : functions only : no special forms.
            function = new FunctionJavaScript(javaScriptFunction, jsEnv);
            try {
                return function.exec(binding, args, getFunctionIRI(), env);
            } catch (ExprUndefFunction ex) {
                // Or check if there is a Java function registered - allows java replacement.
                if ( WarnOnUnknownFunction )
                    ARQ.getExecLogger().warn("JavaScript function not found: "+ex.getFunctionName());
                throw ex;
            }
        }
        
        // Only needed because some tests call straight in.
        // Otherwise, the buildFunction() calls should have done everything
        if ( !functionBound )
            buildFunction(env.getContext()) ;
        if ( function == null )
            throw new ExprEvalException("URI <" + getFunctionIRI() + "> not bound") ;
        NodeValue r = function.exec(binding, args, getFunctionIRI(), env) ;
        return r ;
    }
    
    @Override
    public NodeValue eval(List<NodeValue> args) {
        // evalSpecial hands over function evaluation to the "Function" hierarchy
        throw new ARQInternalErrorException() ;
    }

    public void buildFunction(Context cxt) {
        if ( javaScriptFunction != null )
            return;
        try { bindFunction(cxt) ; }
        catch (ExprUndefFunction ex) {
            if ( WarnOnUnknownFunction )
                ARQ.getExecLogger().warn("URI <"+functionIRI+"> has no registered function factory") ;
        }
    }
    
    private FunctionFactory functionFactory(Context cxt) {
        FunctionRegistry registry = chooseRegistry(cxt) ;
        FunctionFactory ff = registry.get(functionIRI) ;
        return ff;
    }
    
    private void bindFunction(Context cxt) {
        if ( functionBound )
            return ;
        FunctionFactory ff = functionFactory(cxt);
        if ( ff == null ) {
            functionBound = true ;
            throw new ExprUndefFunction("URI <" + functionIRI + "> not found as a function", functionIRI) ;
        }
        function = ff.create(functionIRI) ;
        function.build(functionIRI, args) ;
        functionBound = true ;
    }
    
    private FunctionRegistry chooseRegistry(Context context) {
        FunctionRegistry registry = FunctionRegistry.get(context) ;
        if ( registry == null )
            registry = FunctionRegistry.get() ;
        return registry ;
    }

    @Override
    public String getFunctionPrintName(SerializationContext cxt) {
        return FmtUtils.stringForURI(functionIRI, cxt) ;
    }

    @Override
    public String getFunctionName(SerializationContext cxt) {
        return FmtUtils.stringForURI(functionIRI, cxt) ;
    }

    @Override
    public Expr copy(ExprList newArgs) {
        return new E_Function(getFunctionIRI(), newArgs) ;
    }
}
