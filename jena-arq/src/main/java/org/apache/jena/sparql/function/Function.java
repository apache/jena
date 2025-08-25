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

package org.apache.jena.sparql.function;


import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.Context;

/**
 * Interface to function extensions of the expression evaluator. This includes
 * "functional forms" - functions that take expressions for argument, rather than the
 * usual value from already evaluated argument (hence they are not mathematical
 * functions).
 */
public interface Function
{
    /**
     * Called during query plan construction immediately after the
     * construction of the extension instance.
     * A function can throw {@link ExprException} if something is wrong (like wrong number of arguments).
     * @param uri The function URI
     * @param args The parsed arguments
     * @param context The build context.
     */
    public void build(String uri, ExprList args, Context context);

    /** Call a function.
     *  The argument list will not be null but may have the wrong number of arguments.
     *  FunctionBase provides a more convenient way to implement a function.
     *  Functions can throw {@link ExprEvalException} if something goes wrong.
     *
     * @param binding   The current solution
     * @param args      A list of unevaluated expressions
     * @param uri       The name of this
     * @param env   The execution context
     * @return NodeValue - a value
     */
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env);
}
