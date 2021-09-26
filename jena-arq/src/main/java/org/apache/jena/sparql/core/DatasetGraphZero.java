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

package org.apache.jena.sparql.core;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapZero;
import org.apache.jena.sparql.graph.GraphZero;
import org.apache.jena.sparql.util.Context;

/** An always empty {@link DatasetGraph}.
 * One graph (the default graph) with zero triples.
 * No changes allowed - this is not a sink.
 * <p>
 * This class of {@code DatasetGraph} does track transaction state.
 * <p>
 * It does have a mutable {@link Context}.
 * @see DatasetGraphSink
 */
public class DatasetGraphZero extends DatasetGraphNull {
    /**
     * Invariant {@link DatasetGraph}; it does have transaction state so create new
     * object here.
     */
    public static DatasetGraph create() { return new DatasetGraphZero(); }

    @Override
    protected Graph createGraph() {
        return GraphZero.instance();
    }

    private DatasetGraphZero() {}

    @Override
    public PrefixMap prefixes() {
        return PrefixMapZero.empty;
    }
}
