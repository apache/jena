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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.sse.builders.ExprBuildException;

/**
 * Represents the definition of a user defined function
 *
 */
public class UserDefinedFunctionDefinition {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserDefinedFunctionDefinition.class);
	
	/**
	 * Whether to log warnings for unused variables
	 */
	public static boolean warnOnUnusedVariable = true;

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
        this.argList = new ArrayList<>(argList);
        
        //Verify that all mentioned variables are in the arguments list
        Set<Var> mentioned = this.expr.getVarsMentioned();
        for (Var v : mentioned) {
        	if (!argList.contains(v)) throw new ExprBuildException("Cannot use the variable " + v.toString() + " in the expression since it is not included in the argList argument.  All variables must be arguments to the function"); 
        }        
        //If used variables is greater than argument variables this is an error
        if (mentioned.size() > this.argList.size()) throw new ExprBuildException("Mismatch between variables used in expression and number of variables in argument list, expected " + this.argList.size() + " but found " + mentioned.size());
        //May have more arguments than used, however this only gives warning(s)
        if (mentioned.size() < this.argList.size()) {
        	for (Var v : this.argList) {
        		if (!mentioned.contains(v) && warnOnUnusedVariable) LOG.warn("Function <" + uri + "> has argument " + v + " which is never used in the expression");
        	}
        }
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
