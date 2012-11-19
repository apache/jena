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

package com.hp.hpl.jena.sparql.function.user;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.lang.sparql_11.ParseException;
import com.hp.hpl.jena.sparql.lang.sparql_11.SPARQLParser11;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException;

/**
 * A function factory for managing user defined functions
 * <p>
 * User defined functions provide a simple mechanism for a user to inject custom functions into SPARQL processing
 * without the need to write any code.  These functions essentially act as aliases for another SPARQL expression
 * and serve as a means to aid users in simplifying their SPARQL queries.
 * </p>
 * <p>
 * For example we can define a <strong>square</strong> function like so:
 * </p>
 * <pre>
 * List&lt;Var&gt; args = new ArrayList&lt;Var&gt;(Var.alloc("x"));
 * UserDefinedFunctionFactory.getFactory().add("http://example/square", "?x * ?x", args);
 * </pre>
 * <p>
 * We can then use this in queries like so:
 * </p>
 * <pre>
 * SELECT (&lt;http://example/square&gt;(3) AS ?ThreeSquared) { }
 * </pre>
 * <p>
 * Internally the call to the <strong>square</strong> function is translated into it's equivalent SPARQL expression and executed in that form.
 * </p>
 *
 */
public class UserDefinedFunctionFactory implements FunctionFactory {
    
    private static UserDefinedFunctionFactory factory = new UserDefinedFunctionFactory();    
    
    /**
     * Gets the static instance of the factory
     * @return Function Factory
     */
    public static UserDefinedFunctionFactory getFactory() {
        return factory;
    }

    private Map<String, UserDefinedFunctionDefinition> definitions = new HashMap<String, UserDefinedFunctionDefinition>();
    private boolean allowDependencies = false;
    
    /**
     * Private constructor prevents instantiation
     */
    private UserDefinedFunctionFactory() { }
    
    /**
     * Gets whether user defined functions may explicitly rely on each other (default false)
     * <p>
     * When this is disabled (as it is by default) function definitions are fully expanded at registration time, so if
     * you add a function that references an existing user defined function it will be expanded to include the
     * resulting expression rather than left with a reference to another function.  This protects the user from
     * depending on other functions whose definitions are removed or changed.
     * </p>
     * <p>
     * However it may sometimes be desirable to have functions explicitly depend on each in which case this option may be 
     * disabled with the corresponding {@link #setAllowDependencies(boolean)} setter
     * </p>
     * @return Whether explicit dependencies are allowed
     */
    public boolean getAllowDependencies() {
        return this.allowDependencies;
    }
    
    /**
     * Sets whether user functions may explicitly depend on each other, see {@link #getAllowDependencies()} for explanation of this behaviour
     * @param allow Whether to allow explicit dependencies
     */
    public void setAllowDependencies(boolean allow) {
        this.allowDependencies = allow;
    }
        
    /**
     * Creates a function for the given URI
     * @throws ExprBuildException Thrown if the given URI is not a known function
     */
    @Override
    public Function create(String uri) {
        UserDefinedFunctionDefinition def = this.definitions.get(uri);
        if (def == null) throw new ExprBuildException("Function <" + uri + "> not known by this function factory");
        return def.newFunctionInstance();
    }
    
    /**
     * Adds a function
     * @param uri URI
     * @param e Expression
     * @param args Arguments
     */
    public void add(String uri, Expr e, List<Var> args) {
        if (!allowDependencies) {
            //If not allowing dependencies expand expression fully
            e = ExprTransformer.transform(new ExprTransformExpand(this.definitions), e);
        }        
        
        UserDefinedFunctionDefinition def = new UserDefinedFunctionDefinition(uri, e, args);
        this.definitions.put(uri, def);
        FunctionRegistry.get().put(uri, this);
    }
    
    /**
     * Adds a function
     * <p>
     * This method will build the expression to use based on the expression string given, strings must match the SPARQL expression syntax e.g.
     * </p>
     * <pre>
     * (?x * ?y) + 5
     * </pre>
     * @param uri URI
     * @param expr Expression String (in SPARQL syntax)
     * @param args Arguments
     * @throws ParseException Thrown if the expression string is not valid syntax
     */
    public void add(String uri, String expr, List<Var> args) throws ParseException {
        Expr e = new SPARQLParser11(new StringReader(expr)).Expression();
        if (!allowDependencies) {
            //If not allowing dependencies expand expression fully
            e = ExprTransformer.transform(new ExprTransformExpand(this.definitions), e);
        }  
        
        UserDefinedFunctionDefinition def = new UserDefinedFunctionDefinition(uri, e, args);
        this.definitions.put(uri, def);
        FunctionRegistry.get().put(uri, this);
    }
    
    /**
     * Removes a function definition
     * @param uri URI
     * @throws NoSuchElementException Thrown if a function with the given URI does not exist
     */
    public void remove(String uri) {
        if (!this.definitions.containsKey(uri)) throw new NoSuchElementException("No function definition is associated with the URI <" + uri + ">");
        this.definitions.remove(uri);
        FunctionRegistry.get().remove(uri);
    }
    
    /**
     * Gets the definition of the function (if registered)
     * @param uri URI
     * @return Function Definition if registered, null otherwise
     */
    public UserDefinedFunctionDefinition get(String uri) {
        if (!this.definitions.containsKey(uri)) return null;
        return this.definitions.get(uri);
    }
    
    /**
     * Gets whether a function with the given URI has been registered
     * @param uri URI
     * @return True if registered, false otherwise
     */
    public boolean isRegistered(String uri) {
        return this.definitions.containsKey(uri);
    }
    
    /**
     * Clears all function definitions
     */
    public void clear() {
        for (String uri : this.definitions.keySet()) {
            FunctionRegistry.get().remove(uri);
        }
        this.definitions.clear();
    }
}
