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

package com.hp.hpl.jena.sparql.function;


import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;

/** Interface to value-testing extensions to the expression evaluator. */

public interface Function
{
    /** Called during query plan construction immediately after the
     * construction of the extension instance.
     * Can throw ExprBuildException if something is wrong (like wrong number of arguments). 
     * @param args The parsed arguments
     */ 
    public void build(String uri, ExprList args) ;

    /** Test a list of values - argument will not be null but
     *  may have the wrong number of arguments.
     *  FunctionBase provides a more convenient way to implement a function. 
     *  Can throw ExprEvalsException if something goes wrong.
     *   
     * @param binding   The current solution
     * @param args      A list of unevaluated expressions
     * @param uri       The name of this   
     * @param env   The execution context
     * @return NodeValue - a value
     */ 
    
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) ;
}
