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

import java.util.List;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.function.FunctionBase;

/** fn:format-number : 2 or 3 arguments.
 * The 3rd argument, decimal-format-name, is here a IETF BCP 47 language tag string.
 */
public class FN_FormatNumber extends FunctionBase {
    @Override
    public void checkBuild(String uri, ExprList args) {
        if ( args.size() != 2 && args.size() != 3 )
            throw new QueryBuildException("Function '"+Lib.className(this)+"' takes two or three arguments");
    }
    
    @Override
    public NodeValue exec(List<NodeValue> args) {
        if ( args.size() != 2 && args.size() != 3 )
            throw new ExprEvalException("Function '"+Lib.className(this)+"' takes two or three arguments");
        NodeValue value = args.get(0); 
        NodeValue picture = args.get(1);
        NodeValue decimalFormatName = null;
        if ( args.size() == 3)
            decimalFormatName = args.get(2);
        return XSDFuncOp.formatNumber(value, picture, decimalFormatName);
    }
}
