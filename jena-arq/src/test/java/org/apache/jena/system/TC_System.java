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

package org.apache.jena.system;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.http.auth.TS_HttpAuth;
import org.apache.jena.rdfs.TS_InfRdfs;
import org.apache.jena.sparql.graph.TS_Graph;
import org.apache.jena.sparql.sse.TS_SSE;
import org.apache.jena.sparql.transaction.TS_Transaction;
import org.apache.jena.sparql.util.TS_DyadicDatasetGraphs;
import org.apache.jena.sparql.util.TS_Util;
import org.apache.jena.sparql.util.compose.TS_DatasetCollectors;
import org.apache.jena.sparql.util.iso.TS_Iso;
import org.apache.jena.system.buffering.TS_Buffering;
import org.apache.jena.util.TS_UtilsARQ;

@Suite
@SelectClasses({
    TS_System.class
    , TS_SSE.class
    , TS_Graph.class
    , TS_DyadicDatasetGraphs.class
    , TS_DatasetCollectors.class
    , TS_Util.class
    , TS_Iso.class
    , TS_Transaction.class

    , TS_UtilsARQ.class
    , TS_System.class
    , TS_Buffering.class

    , TS_InfRdfs.class
    , TS_HttpAuth.class
})

public class TC_System {}

