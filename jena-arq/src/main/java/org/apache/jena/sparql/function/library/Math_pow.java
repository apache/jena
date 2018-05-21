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
import org.apache.jena.sparql.function.FunctionBase2 ;

// math:pow($x as xs:double?, $y as xs:numeric) as xs:double?
//    except pow(integer,+ve integer) is an integer 
public class Math_pow extends FunctionBase2 {

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        switch (XSDFuncOp.classifyNumeric("pow", v1, v2))
        {
            case OP_INTEGER:
                BigInteger x = v1.getInteger();
                int y = v2.getInteger().intValue() ;
                if ( y >= 0 )
                    return NodeValue.makeInteger( x.pow(y)) ;
                // Anything else -> double
                //$FALL-THROUGH$
            case OP_DECIMAL:
            case OP_FLOAT:
            case OP_DOUBLE:
                double d1 = v1.getDouble() ;
                double d2 = v2.getDouble() ;
                if ( d1 == 1 && d2 == Double.POSITIVE_INFINITY ) {
                    if ( v1.isInteger() )
                        return NodeValue.nvONE ;
                    else
                        return NodeValue.makeDouble(1) ;
                }
                return NodeValue.makeDouble( Math.pow(v1.getDouble(), v2.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+ v1) ;
        }
    }

}
