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
package org.apache.jena.sparql.engine.join;

import java.util.function.BiConsumer;

import org.apache.jena.sparql.engine.benchmark.QueryTask;
import org.apache.jena.sparql.engine.benchmark.QueryTaskResult;
import org.apache.shadedJena480.query.ARQ;
import org.apache.shadedJena480.query.ResultSet;
import org.apache.shadedJena480.query.ResultSetFormatter;
import org.apache.shadedJena480.sparql.ARQConstants;
import org.apache.shadedJena480.sparql.algebra.Op;
import org.apache.shadedJena480.sparql.algebra.optimize.Optimize;
import org.apache.shadedJena480.sparql.algebra.optimize.Rewrite;
import org.apache.shadedJena480.sparql.algebra.optimize.RewriteFactory;
import org.apache.shadedJena480.sparql.core.DatasetGraphFactory;
import org.apache.shadedJena480.sparql.exec.QueryExec;
import org.apache.shadedJena480.sparql.util.Context;
import org.apache.shadedJena480.sys.JenaSystem;

public class QueryTask480
    extends QueryTask
{
    static { JenaSystem.init(); }

    public QueryTask480(String queryString, long expectedResultSetSize, boolean skipExecution, boolean skipValidation) {
        super(queryString, expectedResultSetSize, skipExecution, skipValidation);
    }

    @Override
    public QueryTaskResult exec() {
        Op[] ops = new Op[] { null, null };
        Context cxt = setupContext((origOp, optimizedOp) -> {
            ops[0] = origOp;
            ops[1] = optimizedOp;
        });

        long resultSetSize;
        try (QueryExec qe = QueryExec.newBuilder()
                .dataset(DatasetGraphFactory.empty())
                .query(queryString)
                .context(cxt)
                .build()) {
            ResultSet rs = ResultSet.adapt(qe.select());

            // System.out.println(ResultSetFormatter.asText(rs));
            resultSetSize = ResultSetFormatter.consume(rs);
        }
        return new QueryTaskResult(queryString, ops[0].toString(), ops[1].toString(), resultSetSize);
    }

    protected static Context setupContext(BiConsumer<Op, Op> opHandler) {
        Context cxt = ARQ.getContext().copy();
        RewriteFactory rewriteFactory = Optimize.getFactory();
        RewriteFactory loggingRewriteFactory = c -> {
            Rewrite rewrite = rewriteFactory.create(c);
            return op -> {
                Op optimizedOp = rewrite.rewrite(op);
                opHandler.accept(op, optimizedOp);
                return optimizedOp;
            };
        };
        cxt.set(ARQConstants.sysOptimizerFactory, loggingRewriteFactory);
        return cxt;
    }
}
