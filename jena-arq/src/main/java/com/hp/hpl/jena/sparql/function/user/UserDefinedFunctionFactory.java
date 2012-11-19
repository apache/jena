/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.user;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
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
 * @author rvesse
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
    
    /**
     * Private constructor prevents instantiation
     */
    private UserDefinedFunctionFactory() { }
        
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
