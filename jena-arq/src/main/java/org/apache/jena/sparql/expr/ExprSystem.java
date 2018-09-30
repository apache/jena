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

package org.apache.jena.sparql.expr;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.util.Symbol ;

public abstract class ExprSystem extends ExprFunction0
{
    protected final Symbol systemSymbol ;

    protected ExprSystem(String fName, Symbol systemSymbol)
    {
        super(fName) ;
        this.systemSymbol = systemSymbol ;
    }

    @Override
    public NodeValue eval(FunctionEnv env)
    {
        Object obj = env.getContext().get(systemSymbol) ;
        
        if ( obj == null )
            throw new ExprEvalException("null for system symbol: "+systemSymbol) ;
        if ( ! ( obj instanceof Node ) )
            throw new ExprEvalException("ExprSystem: Not a Node: "+Lib.className(obj)) ;
        
        Node n = (Node)obj ;
        if ( n == null )
            throw new ExprEvalException("No value for system variable: "+systemSymbol) ;  
        NodeValue nv = NodeValue.makeNode(n) ;
        return nv ;
    }
}
