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

import java.math.BigInteger;

import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.MathLimits;

public class cube extends FunctionBase1 {

    @Override
    public NodeValue exec(NodeValue v) {
        switch (XSDFuncOp.classifyNumeric("cube", v))
        {
            case OP_INTEGER:
                BigInteger i = v.getInteger();
                MathLimits.preValidateExponentCalculation(i, BigInteger.valueOf(3));
                return NodeValue.makeInteger( i.pow(3) );
            case OP_DECIMAL:
                double dec = v.getDecimal().doubleValue();
                MathLimits.preValidateExponentCalculation(dec, 3d);
                return NodeValue.makeDecimal( Math.pow(dec, 3d));
            case OP_FLOAT:
            case OP_DOUBLE:
                MathLimits.preValidateExponentCalculation(v.getDouble(), 3d);
                return NodeValue.makeDouble( Math.pow(v.getDouble(), 3d) );
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v);
        }
    }

}
