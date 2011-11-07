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

package com.hp.hpl.jena.sparql.function.library;

import java.util.List ;

import com.hp.hpl.jena.query.QueryBuildException ;
import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionBase ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** substring(string, start[, finish]) - Java style */

public class substring extends FunctionBase
{
    public substring() { super() ; }

    @Override
    public void checkBuild(String uri, ExprList args)
    {
        if ( args.size() != 2 && args.size() != 3 )
            throw new QueryBuildException("Function '"+Utils.className(this)+"' takes two or three arguments") ;
    }
    @Override
    public NodeValue exec(List<NodeValue> args)
    {
        if ( args.size() > 3 )
            throw new ExprEvalException("substring: Wrong number of arguments: "+
                                        args.size()+" : [wanted 2 or 3]") ;
        
        NodeValue v1 = args.get(0) ;
        NodeValue v2 = args.get(1) ;
        NodeValue v3 = null ;
        
        if ( args.size() == 3 )
        {
            v3 = args.get(2) ;
            return XSDFuncOp.javaSubstring(v1, v2, v3) ;
        }

        return XSDFuncOp.javaSubstring(v1, v2) ;
    }
        
}
