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

package org.apache.jena.rdfs;

import org.apache.jena.rdfs.engine.DatasetGraphWithGraphTransform;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.util.Context;

public class DatasetGraphRDFS extends DatasetGraphWithGraphTransform implements DatasetGraphWrapperView {
    // Do not unwrap for query execution.
    private final SetupRDFS setup;

    public DatasetGraphRDFS(DatasetGraph dsg, SetupRDFS setup) {
        super(dsg, g -> new GraphRDFS(g, setup));
        this.setup = setup;
    }

    public DatasetGraphRDFS(DatasetGraph dsg, SetupRDFS setup, Context cxt) {
        super(dsg, cxt, g -> new GraphRDFS(g, setup));
        this.setup = setup;
    }
}
