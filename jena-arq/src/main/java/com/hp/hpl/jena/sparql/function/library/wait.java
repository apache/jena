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

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.expr.ExprEvalException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.function.FunctionBase1 ;

/** Function that pauses for N milliseconds whenever it is called (for testing) */
public class wait extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue nv)
    {
        if ( ! nv.isInteger() )
            throw new ExprEvalException("Not an integer") ;
        int x = nv.getInteger().intValue() ;
        Lib.sleep(x) ;
        return NodeValue.TRUE ;
    }
}
