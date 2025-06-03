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

package org.apache.jena.sparql.exec.tracker;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;

public class UpdateEngineFactoryExecTracker
    implements UpdateEngineFactory
{
    @Override
    public boolean accept(DatasetGraph datasetGraph, Context context) {
        boolean result = false;
        if (datasetGraph instanceof DatasetGraphWithExecTracker tracker) {
            DatasetGraph backend = tracker.getWrapped();
            UpdateEngineFactory f = UpdateEngineRegistry.findFactory(backend, context);
            result = f.accept(backend, context);
        }
        return result;
    }

    @Override
    public UpdateEngine create(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        DatasetGraphWithExecTracker tracker = (DatasetGraphWithExecTracker)datasetGraph;
        ExecTracker execTracker = ExecTracker.requireTracker(tracker.getContext());
        DatasetGraph backend = tracker.getWrapped();
        UpdateEngineFactory f = UpdateEngineRegistry.findFactory(backend, context);
        UpdateEngine base = f.create(backend, inputBinding, context);

        long[] idRef = {-1};

        UpdateEngine result = new UpdateEngineWrapperBase(base) {
            @Override
            public void startRequest() {
                // Make sure there is a cancel signal in the context.
                AtomicBoolean cancelSignal = context == null ? null : Context.getOrSetCancelSignal(context);
                Runnable cancelAction = (cancelSignal == null)
                    ? null
                    : () -> cancelSignal.set(true);

                idRef[0] = execTracker.put("Update request", cancelAction);
                super.startRequest();
            }

            @Override
            public void finishRequest() {
                execTracker.remove(idRef[0], null);
                super.finishRequest();
            }
        };

        return result;
    }
}
