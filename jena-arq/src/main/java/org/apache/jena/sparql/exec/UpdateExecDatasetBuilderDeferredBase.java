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

package org.apache.jena.sparql.exec;

import java.util.Map.Entry;

import org.apache.jena.graph.Node;
import org.apache.jena.http.sys.UpdateElt;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.exec.tracker.UpdateExecTransform;
import org.apache.jena.sparql.util.Context;

/**
 * QueryExecBuilder that chooses the actual builder only when build is called.
 */
public abstract class UpdateExecDatasetBuilderDeferredBase<X extends UpdateExecDatasetBuilderDeferredBase<X>>
    extends UpdateExecDatasetBuilderBase<X>
{
    // Abbreviated forms

    @Override
    public void execute() {
        build().execute();
    }

    public void execute(DatasetGraph dsg) {
        dataset(dsg);
        execute();
    }

    protected abstract UpdateExecBuilder newActualExecBuilder();

    @Override
    public UpdateExec build() {
        UpdateExecBuilder ueb = newActualExecBuilder();
        ueb = applySettings(ueb);
        UpdateExec ue = ueb.build();
        return ue;
    }

    /** Transfer settings from this builder to to the destination. */
    protected UpdateExecBuilder applySettings(UpdateExecBuilder dest) {
        if (parseCheck != null) {
            dest = dest.parseCheck(parseCheck);
        }

        for (UpdateElt updateElt : updateEltAcc) {
            if (updateElt.isParsed()) {
                dest.update(updateElt.update());
            } else {
                dest.update(updateElt.updateString());
            }
        }

        // Transfer the built context.
        Context cxt = contextAcc.context();
        dest = dest.context(cxt);

        if (substitutionMap != null) {
            for (Entry<Var, Node> e : substitutionMap.entrySet()) {
                dest = dest.substitution(e.getKey(), e.getValue());
            }
        }

        Timeout timeout = timeoutBuilder.build();
        if (timeout.hasOverallTimeout()) {
            dest = dest.timeout(timeout.overallTimeout().amount(), timeout.overallTimeout().unit());
        }

        for (UpdateExecTransform execTransform : updateExecTransforms) {
            dest = dest.transformExec(execTransform);
        }

        return dest;
    }
}
