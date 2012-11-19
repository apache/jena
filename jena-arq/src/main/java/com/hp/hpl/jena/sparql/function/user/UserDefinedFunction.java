/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.function.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransformSubstitute;
import com.hp.hpl.jena.sparql.expr.ExprTransformer;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException;

/**
 * Represents a user defined function
 *
 */
public class UserDefinedFunction extends UserDefinedFunctionDefinition implements Function {

    private Expr actualExpr;
    
    /**
     * Creates a new user defined function
     * @param def Function Definition
     */
    public UserDefinedFunction(UserDefinedFunctionDefinition def) {
        super(def.getUri(), def.getBaseExpr(), def.getArgList());
    }
    
    /**
     * Creates a user defined function
     * @param url Function URL
     * @param e Expression
     * @param argList Arguments
     */
    public UserDefinedFunction(String url, Expr e, List<Var> argList) {
        super(url, e, argList);
    }

    /**
     * Builds the expression substituting the arguments given into the base expression to yield the actual expression to evaluate
     * @throws ExprBuildException Thrown if an expression cannot be generated
     */
    @Override
    public void build(String uri, ExprList args) {
        //Substitutes the arguments into the base expression to give the actual expression to evaluate
        if (uri == null || !uri.equals(this.getUri())) throw new ExprBuildException("Incorrect URI passed to build() call, expected <" + this.getUri() + "> but got <" + uri + ">");
        if (this.getArgList().size() != args.size()) throw new ExprBuildException("Incorrect number of arguments for user defined <" + this.getUri() + "> function");
        
        Map<String, Expr> substitutions = new HashMap<String, Expr>();
        for (int i = 0; i < this.getArgList().size(); i++) {
            substitutions.put(this.getArgList().get(i).getVarName(), args.get(i));
        }
        
        this.actualExpr = ExprTransformer.transform(new ExprTransformSubstitute(substitutions), this.getBaseExpr());
    }

    /**
     * Executes the function
     */
    @Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
        //Evaluate the actual expression
        return this.actualExpr.eval(binding, env);
    }
    
    /**
     * Gets the actual expression that was built for the function, assuming {@link #build(String, ExprList)} has been called
     * @return Expression if built, null otherwise
     */
    public Expr getActualExpr() {
        return this.actualExpr;
    }
}
