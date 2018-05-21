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

package org.apache.jena.sparql.function.library;

import java.util.List ;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprList ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.function.FunctionBase;

/** XPath and XQuery Functions and Operators 3.1
 * <p> 
 * {@code fn:error()}
 * <br/>
 * {@code fn:error(message)}
 */
public class FN_Error extends FunctionBase {

    @Override
    public void checkBuild(String uri, ExprList args) {
        if ( args.size() != 0 && args.size() != 1 )
            throw new QueryBuildException("Function '"+Lib.className(this)+"' takes zero or one arguments") ;
    }
    
    @Override
    public NodeValue exec(List<NodeValue> args) {
        if ( args == null )
            // The contract on the function interface is that this should not happen.
            throw new ARQInternalErrorException("FunctionBase1: Null args list") ;
        
        switch(args.size()) {
            case 0:
                throw new ExprEvalException();
            case 1: {
                NodeValue nv = args.get(0);
                String str = NodeFunctions.str(nv.asNode());
                throw new ExprEvalException(str);
            }
            default:
                    throw new ExprEvalException("fn:error: Wrong number of arguments: Wanted 0 or 1, got "+args.size()) ;
        }
    }
}
