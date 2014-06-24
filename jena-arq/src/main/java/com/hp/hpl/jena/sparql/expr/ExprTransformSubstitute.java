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

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.function.user.UserDefinedFunction;

/**
 * An expression transformer that substitutes another expression in place of variables
 * <p>
 * Primarily introduced in order to support the new {@link UserDefinedFunction} capabilities
 * </p>
 */
public class ExprTransformSubstitute extends ExprTransformCopy {
    
    private Map<String, Expr> replacements = new HashMap<>();
    
    /**
     * Creates a simple transform that replaces any occurrence of the given variable with the given expression
     * @param find Variable to find
     * @param replace Expression to replace with
     */
    public ExprTransformSubstitute(Var find, Expr replace) {
        if (find == null) throw new IllegalArgumentException("find cannot be null");
        if (replace == null) throw new IllegalArgumentException("replace cannot be null");
        this.replacements.put(find.getVarName(), replace);
    }
    
    /**
     * Creates an advanced transform that uses the given map to make substitutions
     * @param substitutions Substitutions from variables to expressions
     */
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
