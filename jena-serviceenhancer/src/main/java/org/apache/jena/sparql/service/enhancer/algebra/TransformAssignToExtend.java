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

package org.apache.jena.sparql.service.enhancer.algebra;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpAssign;
import org.apache.jena.sparql.algebra.op.OpExtend;

/**
 * Transform OpAssign to OpExtend.
 * Rationale: Execution of OpLateral in Jena 5.0.0 inserts OpAssign operations. Attempting to execute those remote results in
 * SPARQL LET syntax elements which are usually not understood by remote endpoints.
 */
@Deprecated // Should no longer be necessary with https://github.com/apache/jena/pull/3029
public class TransformAssignToExtend
    extends TransformCopy
{
    private static TransformAssignToExtend INSTANCE = null;

    public static TransformAssignToExtend get() {
        if (INSTANCE == null) {
            synchronized (TransformAssignToExtend.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TransformAssignToExtend();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Op transform(OpAssign opAssign, Op subOp) {
        return OpExtend.create(subOp, opAssign.getVarExprList());
    }
}
