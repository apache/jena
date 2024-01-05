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

package org.apache.jena.sparql.algebra;

import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.Quad;

/** Functions relating to {@link Op}s. */
public class OpLib
{
    /** Convert a pattern, assumed to be quad form,
     * so that the default graph is the union of named graphs.
     */
    public static Op unionDefaultGraphQuads(Op op)
    {
        // Rewrite so that any explicitly named "default graph" is union graph.
        Transform t = new TransformGraphRename(Quad.defaultGraphNodeGenerated, Quad.unionGraph) ;
        op = Transformer.transform(t, op);
        return op;
    }

    // XXX These may change to be a specific Op implementation.
    // Needs a visitor change.

    /**
     * Return an {@code Op} that is the join unit - {@code (join unit(), X) = X)}.
     * It is one row of zero columns.
     */
    public static Op unit() {
        return OpTable.unit();
    }

    /**
     * Return an {@code Op} that is the join zero - {@code (join empty(), X) = empty)}.
     * It is zero rows.
     */
    public static Op empty()
    { return OpTable.empty(); }
}
