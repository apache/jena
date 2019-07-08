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

package org.apache.jena.tdb2.assembler;

import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.exceptions.AssemblerException;
import static org.junit.Assert.*;
import org.apache.jena.atlas.lib.FileOps;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab;
import org.apache.jena.tdb2.ConfigTest;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.apache.jena.tdb2.store.GraphViewSwitchable;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTDBAssembler
{
    // Can be slow - explicitly closes the dataset.
    static String dirAssem      = null;
    static final String dirDB   = ConfigTest.getTestingDir()+"/DB";

    @BeforeClass
    static public void beforeClass() {
        dirAssem = ConfigTest.getTestingDataRoot() + "/Assembler";
        FileOps.ensureDir(dirDB);
    }

    @Before
    public void before() {
        TDBInternal.reset();
        FileOps.clearDirectory(dirDB);
    }

    @AfterClass
    static public void afterClass() {
        TDBInternal.reset();
        FileOps.clearDirectory(dirDB);
    }

    @Test
    public void createDatasetDirect() {
        createTest(dirAssem + "/tdb-dataset.ttl", VocabTDB2.tDatasetTDB);
    }

    @Test
    public void createDatasetEmbed() {
        createTest(dirAssem + "/tdb-dataset-embed.ttl", DatasetAssemblerVocab.tDataset);
    }

    private void createTest(String filename, Resource type) {
        Object thing = AssemblerUtils.build(filename, type);
        assertTrue(thing instanceof Dataset);
        Dataset ds = (Dataset)thing;
        assertTrue(ds.asDatasetGraph() instanceof DatasetGraphSwitchable);
        assertTrue(ds.supportsTransactions());
        ds.close();
    }

    @Test
    public void createGraphDirect() {
        testGraph(dirAssem + "/tdb-graph.ttl", false);
    }

    @Test
    public void createGraphEmbed() {
        String f = dirAssem + "/tdb-graph-embed.ttl";
        Object thing = null;
        try {
            thing = AssemblerUtils.build(f, JA.Model);
        }
        catch (AssemblerException e) {
            e.getCause().printStackTrace(System.err);
            throw e;
        }

        assertTrue(thing instanceof Model);
        Graph graph = ((Model)thing).getGraph();
        assertTrue(graph instanceof GraphViewSwitchable);

        DatasetGraphSwitchable ds = ((GraphViewSwitchable)graph).getDataset();
        if ( ds != null )
            ds.close();
    }

    @Test
    public void createNamedGraph1() {
        testGraph(dirAssem + "/tdb-named-graph-1.ttl", true);
    }

    @Test
    public void createNamedGraph2() {
        testGraph(dirAssem + "/tdb-named-graph-2.ttl", true);
    }

    @Test
    public void createNamedGraphViaDataset() {
        testGraph(dirAssem + "/tdb-graph-ref-dataset.ttl", false);
    }

    private static void testGraph(String assemblerFile, boolean named) {
        Object thing = null;
        try {
            thing = AssemblerUtils.build(assemblerFile, VocabTDB2.tGraphTDB);
        }
        catch (AssemblerException e) {
            e.getCause().printStackTrace(System.err);
            throw e;
        }

        assertTrue(thing instanceof Model);
        Graph graph = ((Model)thing).getGraph();

        assertTrue(graph instanceof GraphViewSwitchable);

        DatasetGraphSwitchable dsg = ((GraphViewSwitchable)graph).getDataset();
        if ( dsg != null )
            dsg.close();
    }
}
