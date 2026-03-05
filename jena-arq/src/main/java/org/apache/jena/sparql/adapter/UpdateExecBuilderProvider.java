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

package org.apache.jena.sparql.adapter;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.util.Context;

public interface UpdateExecBuilderProvider {
    /**
     * Tests whether {@link #create(DatasetGraph)} will return an
     * appropriate update execution builder for the given dataset graph.
     */
    boolean accept(DatasetGraph dsg, Context context);

    /**
     * Create a new update execution builder for the given dataset graph.
     * <p>
     * This method must only be called with arguments for which
     * {@link #accept(DatasetGraph)} returns true.
     */
    UpdateExecBuilder create(DatasetGraph dsg, Context context);
}
