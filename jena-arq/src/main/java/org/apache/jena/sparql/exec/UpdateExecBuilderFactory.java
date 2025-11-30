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

package org.apache.jena.sparql.exec;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * Factory for {@link UpdateExecBuilder} instances that are created only
 * when this provider accepts the given {@link DatasetGraph} and {@link Context}.
 *
 * Providers are registered with {@link UpdateExecBuilderRegistry}.
 *
 * @see UpdateExecBuilderRegistry
 */
public interface UpdateExecBuilderFactory {
    /**
     * Tests whether {@link #create(DatasetGraph, Context)} will return an
     * appropriate update execution builder for the given dataset graph.
     *
     * @param dataset The dataset. <b>May be null!</b>
     * @param context The context. Never null.
     * @return True iff this factory accepts the given arguments.
     */
    public boolean accept(DatasetGraph dataset, Context context);

    /**
     * Create a new update execution builder for the given dataset graph.
     * <p>
     * This method must <b>only</b> be called with arguments for which
     * {@link #accept(DatasetGraph, Context)} returns true.
     */
    public UpdateExecBuilder create(DatasetGraph dataset, Context context);
}
