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

package org.apache.jena.sparql.service;

import java.util.function.Supplier;

import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * Interface for custom handling of service execution requests.
 */
public interface ServiceExecutorFactory {

    /**
     * Whether the OpService instance passed to {@link #createExecutor(OpService, Binding, ExecutionContext)}
     * should already be substituted with the binding. Defaults to true.
     */
    default boolean substituteOp() {
        return true;
    }

    /**
     * If this factory cannot handle the execution request then this method needs to return null.
     * Otherwise, a supplier with the corresponding QueryIterator needs to be supplied.
     *
     * @return A QueryIterator supplier if this factory can handle the request, null otherwise.
     */
    Supplier<QueryIterator> createExecutor(OpService op, Binding binding, ExecutionContext execCxt);
}
