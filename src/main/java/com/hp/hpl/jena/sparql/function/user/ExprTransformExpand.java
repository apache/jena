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

import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprTransformCopy;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException;

/**
 * An expression transformer that will expand user defined function expressions
 * so they do not explicitly rely on other user defined functions.
 * <p>
 * See {@link UserDefinedFunctionFactory#getPreserveDependencies()} for discussion of what this means in practise
 * </p>
 */
public class ExprTransformExpand extends ExprTransformCopy {
    
    private Map<String, UserDefinedFunctionDefinition> definitions;
    
    /**
     * Creates a new transformer
     * @param defs User defined function definitions
     */
    public ExprTransformExpand(Map<String, UserDefinedFunctionDefinition> defs) {
        if (defs == null) throw new IllegalArgumentException("defs cannot be null");
        this.definitions = defs;
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        ExprFunction f = func.getFunction();
        if (this.shouldExpand(f)) {
            UserDefinedFunctionDefinition def = this.definitions.get(f.getFunction().getFunctionIRI());
            UserDefinedFunction uFunc = (UserDefinedFunction) def.newFunctionInstance();
            
            //Need to watch out for the case where the arguments supplied to the invoked
            //function are in a different order to the arguments supplied to the defined
            //function
            //Thus we will build the list of arguments used to expand the inner function
            //manually
            List<Var> defArgs = def.getArgList();
            ExprList subArgs = new ExprList();
            
            for (int i = 0; i < args.size(); i++) {
                Expr arg = args.get(i);
                String var = arg.getVarName();
                if (var == null) {
                    //Non-variable args may be passed as-is
                    subArgs.add(arg);
                } else {
                    //Variable args must be checked to ensure they are within the number of
                    //arguments of the invoked function
                    //We then use the arg as-is to substitute
                    if (i > defArgs.size()) throw new ExprBuildException("Unable to expand function dependency, the function <" + def.getUri() + "> is called but uses an argument ?" + var + " which is not an argument to the outer function");
                    subArgs.add(arg);
                }
            }
            
            //Expand the function
            uFunc.build(def.getUri(), subArgs);
            return uFunc.getActualExpr();
        } else {
            return super.transform(func, args);
        }
    }
    
    private boolean shouldExpand(ExprFunction func) {
        return this.definitions.containsKey(func.getFunctionIRI());
    }

}
