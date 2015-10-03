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

package org.apache.jena.sparql.function.library.leviathan;

import java.math.BigInteger;

import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.function.FunctionBase1 ;

public class factorial extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        BigInteger i = v.getInteger();

        switch (i.compareTo(BigInteger.ZERO)) {
        case 0:
            // 0! is 1
            return NodeValue.makeInteger(BigInteger.ONE);
        case -1:
            // Negative factorial is error
            throw new ExprEvalException("Cannot evaluate a negative factorial");
        case 1:
            BigInteger res = i.add(BigInteger.ZERO);
            i = i.subtract(BigInteger.ONE);
            while (i.compareTo(BigInteger.ZERO) != 0) {
                res = res.multiply(i);
                i = i.subtract(BigInteger.ONE);
            }
            return NodeValue.makeInteger(res);
        default:
            throw new ExprEvalException("Unexpecte comparison result");
        }
    }

}
