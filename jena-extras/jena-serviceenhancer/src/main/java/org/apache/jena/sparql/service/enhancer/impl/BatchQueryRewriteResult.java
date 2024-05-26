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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.Map;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;

/**
 * Rewrite result of a bulk service request. The 'renames' mapping
 * may turn publicized variables back to internal/anonymous ones.
 * For instance, running <pre>{@code SERVICE <foo> { { SELECT COUNT(*) {...} } }}</pre>
 * will allocate an internal variable for count.
 */
public class BatchQueryRewriteResult {
    protected Op op;
    protected Map<Var, Var> renames;

    public BatchQueryRewriteResult(Op op, Map<Var, Var> renames) { //  Set<Var> joinVars
        super();
        this.op = op;
        this.renames = renames;
    }

    public Op getOp() {
        return op;
    }

    public Map<Var, Var> getRenames() {
        return renames;
    }

    @Override
    public String toString() {
        return "BatchQueryRewriteResult [op=" + op + ", renames=" + renames + "]";
    }
}