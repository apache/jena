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

package org.apache.jena.sparql.engine.dispatch;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

/**
 * An update dispatcher is responsible for taking an update request and routing it to the
 * appropriate component or system for preparing the execution against a dataset.
 * The result is a {@linkplain UpdateExec} instance.
 * Update requests may be passed to dispatchers in syntactic or string representation.
 *
 * Update dispatchers form a chain, and a ChainingUpdateDispatcher acts as a link in such a chain.
 * A ChainingUpdateDispatcher instance can choose to process an update request by itself or to delegate processing to the
 * remainder of the chain.
 *
 * @see SparqlDispatcherRegistry
 */
public interface ChainingUpdateDispatcher {
    UpdateExec create(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context, UpdateDispatcher chain);
    UpdateExec create(UpdateRequest updateRequest, DatasetGraph dsg, Binding initialBinding, Context context, UpdateDispatcher chain);
}
