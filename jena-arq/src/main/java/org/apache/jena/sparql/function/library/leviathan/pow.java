/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.function.library.leviathan;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.function.FunctionBase2;
import org.apache.jena.sparql.function.MathLimits;
import org.apache.jena.sparql.function.library.Math_pow;

public class pow extends FunctionBase2 {

    private final Math_pow mathPow = new Math_pow();

    @Override
    public NodeValue exec(NodeValue v1, NodeValue v2) {
        switch (XSDFuncOp.classifyNumeric("pow", v1))
        {
            case OP_DECIMAL:
                double dec = v1.getDecimal().doubleValue();
                MathLimits.preValidateExponentCalculation(dec, v2.getDouble());
                return NodeValue.makeDecimal( Math.pow(dec, v2.getDouble()));
            case OP_INTEGER:
            case OP_FLOAT:
            case OP_DOUBLE:
                // Defer to Math_pow as that handles all the special cases
                return this.mathPow.exec(v1, v2);
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+ v1);
        }
    }

}
