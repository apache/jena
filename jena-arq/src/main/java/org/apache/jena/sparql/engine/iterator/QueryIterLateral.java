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

package org.apache.jena.sparql.engine.iterator;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;

public class QueryIterLateral extends QueryIterRepeatApply {

    private final Op lateralOp;
    private final boolean isUnit;

    public QueryIterLateral(QueryIterator input, Op lateralOp, ExecutionContext execCxt) {
        super(input, execCxt);
        this.lateralOp = lateralOp;
        this.isUnit = isJoinIdentity(lateralOp);
    }

    private boolean isJoinIdentity(Op op) {
        if( ! ( op instanceof OpTable ) )
            return false;
        OpTable table = (OpTable)lateralOp;
        return table.isJoinIdentity();
    }

    @Override
    protected QueryIterator nextStage(Binding binding) {
        if ( isUnit )
            return QueryIterSingleton.create(binding, super.getExecContext());
        Op op = Substitute.substitute(lateralOp, binding);
        return QC.execute(op, binding, super.getExecContext());
    }
}
