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

package org.apache.jena.sparql.service.enhancer.assembler;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.ext.com.google.common.io.MoreFiles;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.assembler.VocabTDB2;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;


public class TestServiceEnhancerDatasetAssembler
{
    private static final String SPEC_STR_01 = String.join("\n",
            "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
            "PREFIX se: <http://jena.apache.org/service-enhancer#>",
            "<urn:example:root> a se:DatasetServiceEnhancer ; ja:baseDataset <urn:example:base> .",
            "<urn:example:root> se:cacheMaxEntryCount 5 ; se:cachePageSize 1000 ; se:cacheMaxPageCount 10 .",
            "<urn:example:root> se:bulkMaxSize 20 ; se:bulkSize 10 ; se:bulkMaxOutOfBandSize 5 .",
            "<urn:example:base> a ja:MemoryDataset ."
        );

    /**
     * This test case attempts to assemble a dataset with the service enhancer plugin
     * set up in its context. A query making use of enhancer features is fired against it.
     * Only if the plugin is loaded successfully then the query will execute successfully.
     */
    @Test
    public void testAssembler() {
        Model spec = ModelFactory.createDefaultModel();
        RDFDataMgr.read(spec, new StringReader(SPEC_STR_01), null, Lang.TURTLE);

        Dataset dataset = DatasetFactory.assemble(spec.getResource("urn:example:root"));
        Context cxt = dataset.getContext();

        Assert.assertEquals(20, cxt.getInt(ServiceEnhancerConstants.serviceBulkMaxBindingCount, -1));
        Assert.assertEquals(10, cxt.getInt(ServiceEnhancerConstants.serviceBulkBindingCount, -1));
        Assert.assertEquals(5, cxt.getInt(ServiceEnhancerConstants.serviceBulkMaxOutOfBandBindingCount, -1));

        try (QueryExecution qe = QueryExecutionFactory.create(
                "SELECT * { BIND(<urn:example:x> AS ?x) SERVICE <loop:bulk+10:> { ?x ?y ?z } }", dataset)) {
            ResultSetFormatter.consume(qe.execSelect());
        }
    }

    /** Test that calling cacheRm fails because enableMgmt has not been set to true in the context */
    @Test(expected = QueryExecException.class)
    public void testAssemblerMgmtFail() {
        String specStr = String.join("\n",
            "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
            "PREFIX se: <http://jena.apache.org/service-enhancer#>",
            "<urn:example:root> a se:DatasetServiceEnhancer ; ja:baseDataset <urn:example:base> .",
            "<urn:example:base> a ja:MemoryDataset ."
        );

        Model spec = ModelFactory.createDefaultModel();
        RDFDataMgr.read(spec, new StringReader(specStr), null, Lang.TURTLE);
        Dataset dataset = DatasetFactory.assemble(spec.getResource("urn:example:root"));
        try (QueryExecution qe = QueryExecutionFactory.create(
                "PREFIX se: <http://jena.apache.org/service-enhancer#> SELECT se:cacheRm(0) { }", dataset)) {
            Assert.assertEquals(1, ResultSetFormatter.consume(qe.execSelect()));
        }
    }

    /** Test for cacheRm to execute successfully due to enableMgmt having been set to true in the context */
    @Test
    public void testAssemblerMgmtSuccess() {
        String specStr = String.join("\n",
            "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
            "PREFIX se: <http://jena.apache.org/service-enhancer#>",
            "<urn:example:root> a se:DatasetServiceEnhancer ; se:enableMgmt true ; ja:baseDataset <urn:example:base> .",
            "<urn:example:base> a ja:MemoryDataset ."
        );

        Model spec = ModelFactory.createDefaultModel();
        RDFDataMgr.read(spec, new StringReader(specStr), null, Lang.TURTLE);

        Dataset dataset = DatasetFactory.assemble(spec.getResource("urn:example:root"));

        dataset.asDatasetGraph().getDefaultGraph().add(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.Property);
        try (QueryExecution qe = QueryExecutionFactory.create(
                "SELECT * { SERVICE <cache:> { ?s ?p ?o } }", dataset)) {
            Assert.assertEquals(1, ResultSetFormatter.consume(qe.execSelect()));
        }

        try (QueryExecution qe = QueryExecutionFactory.create(
                "PREFIX se: <http://jena.apache.org/service-enhancer#> SELECT se:cacheRm(0) { }", dataset)) {
            Assert.assertEquals(1, ResultSetFormatter.consume(qe.execSelect()));
        }
    }

    /**
     * Test case to check that when {@code OpService('loop:', subOp)} is evaluated against an union default
     * graph (UDG) enabled dataset then the subOp must be evaluated on the UDG too.
     * This also applies to {@code cache:} and {@code bulk:}.
     */
    @Test
    public void testAssemblerTdbUnionDefaultGraph() throws IOException {
        Path tdb2TmpFolder = Files.createTempDirectory("jena-service-enhancer-tdb2").toAbsolutePath();
        try {
            String assemblerStr = String.join("\n",
                    "PREFIX ja: <http://jena.hpl.hp.com/2005/11/Assembler#>",
                    "PREFIX se: <http://jena.apache.org/service-enhancer#>",
                    "PREFIX tdb2: <http://jena.apache.org/2016/tdb#>",
                    "<urn:example:root> a se:DatasetServiceEnhancer ; ja:baseDataset <urn:example:base> .",
                    "<urn:example:root> se:cacheMaxEntryCount 5 ; se:cachePageSize 1000 ; se:cacheMaxPageCount 10 .",
                    "<urn:example:root> se:bulkMaxSize 20 ; se:bulkSize 10 ; se:bulkMaxOutOfBandSize 5 .",
                    "<urn:example:base> a tdb2:DatasetTDB2 .",
                    "<urn:example:base> tdb2:unionDefaultGraph true ."
                );

            String dataStr = String.join("\n",
                    "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                    "<urn:example:g1> { <urn:example:s1> a <urn:example:class1> ; rdfs:label \"s1\" }",
                    "<urn:example:g2> { <urn:example:s2> a <urn:example:class2> ; rdfs:label \"s2\" }");

            String queryStr = "SELECT * { ?s a [] BIND(NOW() AS ?outer) SERVICE <loop:> { ?s ?p ?o BIND(NOW() AS ?inner) } }";

            Model confModel = ModelFactory.createDefaultModel();
            RDFDataMgr.read(confModel, new StringReader(assemblerStr), null, Lang.TURTLE);
            Resource tdb2Conf = confModel.getResource("urn:example:base");
            tdb2Conf.addProperty(VocabTDB2.pLocation, tdb2TmpFolder.toString());

            Dataset tdb2Ds = DatasetFactory.assemble(confModel.getResource("urn:example:root"));

            Txn.executeWrite(tdb2Ds, () -> RDFDataMgr.read(tdb2Ds, new StringReader(dataStr), null, Lang.TRIG));

            int actualRowCount = Txn.calculateRead(tdb2Ds, () -> {
                try (QueryExecution qe = QueryExecution.create(queryStr, tdb2Ds)) {
                    ResultSet rs = qe.execSelect();
                    // System.out.println(ResultSetFormatter.asText(rs));
                    return ResultSetFormatter.consume(rs);
                }
            });

            Assert.assertEquals(4, actualRowCount);
        } finally {
            MoreFiles.deleteRecursively(tdb2TmpFolder);
        }
    }
}
