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

import java.math.BigInteger ;

import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.function.FunctionBase1 ;

// Returns the value of natural log(x)
public class Math_exp10 extends FunctionBase1 {
    
    @Override
    public NodeValue exec(NodeValue v) {
        switch (XSDFuncOp.classifyNumeric("exp10", v))
        {
            case OP_INTEGER:
                int x = v.getInteger().intValue() ;
                if ( x >= 0 )
                    return NodeValue.makeInteger(BigInteger.TEN.pow(x)) ;
                // Anything else -> double
                //$FALL-THROUGH$
            case OP_DECIMAL:
            case OP_FLOAT:
            case OP_DOUBLE:
                return NodeValue.makeDouble(Math.pow(10, v.getDouble())) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+ v) ;
        }
    }
}
