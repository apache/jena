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

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.function.Function;

/**
 * Represents the definition of a user defined function
 *
 */
public class UserDefinedFunctionDefinition {

    private String uri;
    private Expr expr;
    private List<Var> argList;
    
    /**
     * Creates a user defined function definition
     * @param uri Function URL
     * @param e Expression
     * @param argList Arguments
     */
    public UserDefinedFunctionDefinition(String uri, Expr e, List<Var> argList) {
        this.uri = uri;
        this.expr = e;
        this.argList = new ArrayList<Var>(argList);
    }
    
    /**
     * Gets the base expression
     * @return Expression
     */
    public Expr getBaseExpr() {
        return this.expr;
    }
    
    /**
     * Gets the argument list
     * @return Arguments
     */
    public List<Var> getArgList() {
        return this.argList;
    }
    
    /**
     * Gets the function URI
     * @return URI
     */
    public String getUri() {
        return this.uri;
    }
    
    /**
     * Gets an instance of an actual {@link Function} that can be used to evaluate this function
     * @return Function instance
     */
    public Function newFunctionInstance() {
        return new UserDefinedFunction(this);
    }
}
