/*
 * Copyright 2012 YarcData LLC All Rights Reserved.
 */ 

package com.hp.hpl.jena.sparql.expr;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.function.user.UserDefinedFunction;

/**
 * An expression transformer that substitutes another expression in place of variables
 * <p>
 * Primarily introduced in order to support the new {@link UserDefinedFunction} capabilities
 * </p>
 * @author rvesse
 *
 */
public class ExprTransformSubstitute extends ExprTransformCopy {
    
    private Map<String, Expr> replacements = new HashMap<String, Expr>();
    
    public ExprTransformSubstitute(Var find, Expr replace) {
        if (find == null) throw new IllegalArgumentException("find cannot be null");
        if (replace == null) throw new IllegalArgumentException("replace cannot be null");
        this.replacements.put(find.getVarName(), replace);
    }
    
    public ExprTransformSubstitute(Map<String, Expr> substitutions) {
        if (substitutions == null) throw new IllegalArgumentException("replacements cannot be null");
        this.replacements.putAll(substitutions);
        
        for (String key : this.replacements.keySet()) {
            if (this.replacements.get(key) == null) throw new IllegalArgumentException("Variable ?" + key + " cannot be mapped to a null expression");
        }
    }
    
    @Override
    public Expr transform(ExprVar exprVar) {
        //If variable matches replace with the chosen expression
        if (this.replacements.containsKey(exprVar.getVarName())) return this.replacements.get(exprVar.getVarName());
        //Otherwise leave as is
        return super.transform(exprVar);
    }

}
