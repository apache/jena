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

package org.apache.jena.sparql.function.js;
import java.util.List;

import javax.script.ScriptException;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprUndefFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

/**
 * Javascript implemented SPARQL custom functions for ARQ. The JavaScript function is
 * called with arguments which are mapped so that XSD strings, numbers and booleans become the
 * equivalent native JavaScript object, and anything else becomes a {@link NV}, a
 * JavaScript object providing access to the RDF features such as datatype.
 * {@link NV#toString} returns a string so a function working with URIs can treat URIs as
 * strings which is natural in JavaScript and aligned to
 * <a href="https://github.com/rdfjs/representation-task-force/"
 * >rdfjs/representation-task-force</a>.
 * <p>
 * Functions are executed in {@link EnvJavaScript}. There is a global
 * {@link EnvJavaScript} and it can also be set specifically for a query execution.
 * See {@link EnvJavaScript} for details of configuration.
 * <p>
 * Note: there is an
 * attempt to reconstruct the datatype of the result of the function into
 * {@code xsd:integer} and {@code xsd:double}.
 * <p>
 * Functions that return null or undefined will result in a {@link ExprEvalException}.
 * 
 * @see EnvJavaScript
 * @see NV
 */
public class FunctionJavaScript extends FunctionBase {

    private final EnvJavaScript envJS;
    private final String functionName;

    private boolean initialized = false;
    
    public FunctionJavaScript(String functionName, EnvJavaScript env) {
        this.functionName = functionName;
        this.envJS = env;
    }

    @Override
    public void checkBuild(String uri, ExprList args) {}

    @Override
    public NodeValue exec(List<NodeValue> args) {
        try {
            // Convert NodeValues to types more akin to Javascript. 
            // Pass strings as string, and numbers as Number.
            Object[] a = new Object[args.size()];
            for ( int i = 0 ; i < args.size(); i++ )
                a[i] = NV.fromNodeValue(args.get(i));
            Object r = envJS.call(functionName, a);
            if ( r == null )
                // null is used used to signal an ExprEvalException.
                // NV.throwExprEvalException(....);
                throw new ExprEvalException(functionName);
            NodeValue nv = NV.toNodeValue(r);
            return nv;
        }
        catch (NoSuchMethodException ex) {
            throw new ExprUndefFunction("No such JavaScript function '"+functionName+"'", functionName);
        }
        catch (ScriptException e) {
            throw new ExprEvalException("Failed to evaluate javascript function '"+functionName+"'", e);
        }
    }
}