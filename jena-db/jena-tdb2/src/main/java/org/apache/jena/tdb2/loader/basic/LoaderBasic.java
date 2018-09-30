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

package org.apache.jena.tdb2.loader.basic;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.lang.StreamRDFCounting;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFCountingBase;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.loader.base.LoaderBase;
import org.apache.jena.tdb2.loader.base.LoaderOps;
import org.apache.jena.tdb2.loader.base.MonitorOutput;

/** Simple bulk loader. Algorithm: Parser to dataset. */ 
public class LoaderBasic extends LoaderBase {
    private static int DataTickPoint = 100_000;
    private static int DataSuperTick = 10;
    // The destination for loading data.
    private final StreamRDFCounting dest;
    private final StreamRDF baseDest;
    
    public LoaderBasic(DatasetGraph dsg, Node graphName, MonitorOutput output) {
        super(dsg, graphName, output);
        baseDest = LoaderOps.toNamedGraph(StreamRDFLib.dataset(dsg), graphName);
        dest = new StreamRDFCountingBase(baseDest);
    }

    @Override
    protected boolean bulkUseTransaction() {
        return true;
    }

    @Override
    public StreamRDF stream() {
        return dest;
    }

    @Override
    protected void loadOne(String source) {
        LoaderOps.inputFile(dest, source, output, DataTickPoint, DataSuperTick);
    }

    @Override
    public long countTriples() {
        return dest.countTriples();
    }

    @Override
    public long countQuads() {
        return dest.countQuads();
    }
}
