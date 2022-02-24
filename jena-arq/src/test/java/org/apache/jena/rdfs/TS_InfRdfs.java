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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
@RunWith(Suite.class)
@Suite.SuiteClasses( {

    // Separate data and schema.
    TestGraphSplitRDFS.class
    , TestMatchVocab.class

    // ---- Test of InfStreamRDFS; separate data and schema.
    , TestStreamRDFS.class
    // Materialized by stream.
    , TestStreamRDFS_MaterializedSplit.class

    // ---- Test of GraphIncRDFS
    // Include vocabulary and vocabulary derived triples.

    // Separate data and schema. Add back calculated schema inferences.
    , TestGraphIncRDFS_Split.class
    // One graph: data and schema.
    , TestGraphIncRDFS_Combined.class

    // ---- Other
    , TestMiscRDFS.class

    // DatasetGraph API usage.
    , TestDatasetGraphRDFS.class

    // DatasetGraphRDFS and SPARQL.
    , TestInfSPARQL.class

    , TestAssemblerRDFS.class
})

public class TS_InfRdfs { }
