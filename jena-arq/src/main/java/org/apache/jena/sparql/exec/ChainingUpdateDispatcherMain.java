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

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Timeouts;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.dispatch.ChainingUpdateDispatcher;
import org.apache.jena.sparql.engine.dispatch.UpdateDispatcher;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class ChainingUpdateDispatcherMain
    implements ChainingUpdateDispatcher
{
    @Override
    public UpdateExec create(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context, UpdateDispatcher chain) {
        UpdateRequest updateRequest = UpdateFactory.create(updateRequestString);
        return create(updateRequest, dsg, initialBinding, context, chain);
    }

    @Override
    public UpdateExec create(UpdateRequest updateRequest, DatasetGraph dataset, Binding initialBinding, Context cxt, UpdateDispatcher chain) {
        UpdateRequest actualUpdate = updateRequest;

        UpdateEngineFactory f = UpdateEngineRegistry.get().find(dataset, cxt);
        if ( f == null )
            throw new UpdateException("Failed to find an UpdateEngine");

        Timeout timeout = Timeouts.extractUpdateTimeout(cxt);
        UpdateExec uExec = new UpdateExecDataset(actualUpdate, dataset, initialBinding, cxt, f, timeout);
        return uExec;
    }
}
