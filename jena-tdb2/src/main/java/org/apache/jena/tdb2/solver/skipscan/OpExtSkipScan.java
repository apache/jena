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

package org.apache.jena.tdb2.solver.skipscan;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.apache.jena.sparql.util.Symbol;

public class OpExtSkipScan
    extends OpExt
{
    protected PatternQuery patternQuery;

    private static Symbol symOpExecutor = Symbol.create("opExecutor");

    public static void setOpExecutorIfAbsent(Context cxt, OpExecutor opExecutor) {
        cxt.computeIfAbsent(symOpExecutor, x -> opExecutor);
    }

    public static void setOpExecutor(Context cxt, OpExecutor opExecutor) {
        cxt.set(symOpExecutor, opExecutor);
    }

    public static OpExecutor getOpExecutor(Context cxt) {
        return cxt.get(symOpExecutor);
    }

    public OpExtSkipScan(PatternQuery patternQuery) {
        super("skipScan");
        this.patternQuery = patternQuery;
    }

    @Override
    public Op effectiveOp() {
        return patternQuery.effectiveOp();
    }

    private OpExecutor getOpExecutor(ExecutionContext execCxt) {
        OpExecutor result = getOpExecutor(execCxt.getContext());
        if (result == null) {
            // It would be possible to gracefully create a new exec context, but better be strict.
            throw new ARQInternalErrorException("OpExecutor not set");
        }
        return result;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        OpExecutor opExecutor = getOpExecutor(execCxt);
        QueryIterator result = OpExecutorTDB2SkipScan.exec(patternQuery, input, execCxt, opExecutor);
        return result;
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        Op eop = effectiveOp();
        eop.output(out, sCxt);
    }

    @Override
    public int hashCode() {
        return 31 * patternQuery.hashCode();
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        return other instanceof OpExtSkipScan o && this.effectiveOp().equalTo(o.effectiveOp(), labelMap);
    }
}
