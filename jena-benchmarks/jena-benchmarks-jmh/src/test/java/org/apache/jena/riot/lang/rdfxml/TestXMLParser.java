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

package org.apache.jena.riot.lang.rdfxml;

import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem2.GraphMem2Fast;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.nio.file.StandardOpenOption;

@State(Scope.Benchmark)
public class TestXMLParser {

    @Param({
            "../testing/pizza.owl.rdf",
//            "../testing/citations.rdf",
//            "../testing/BSBM/bsbm-5m.xml",

    })
    public String param0_GraphUri;

    @Param({
            "RRX.RDFXML_SAX",
            "RRX.RDFXML_StAX_ev",
            "RRX.RDFXML_StAX_sr",

//            "RRX.RDFXML_ARP0",
            "RRX.RDFXML_ARP1"
    })
    public String param1_ParserLang;


    private static Lang getLang(String langName) {
        switch (langName) {
            case "RRX.RDFXML_SAX":
                return RRX.RDFXML_SAX;
            case "RRX.RDFXML_StAX_ev":
                return RRX.RDFXML_StAX_ev;
            case "RRX.RDFXML_StAX_sr":
                return RRX.RDFXML_StAX_sr;

            case "RRX.RDFXML_ARP0":
                return RRX.RDFXML_ARP0;
            case "RRX.RDFXML_ARP1":
                return RRX.RDFXML_ARP1;

            default:
                throw new IllegalArgumentException("Unknown lang: " + langName);
        }
    }

    private static org.apache.shadedJena510.riot.Lang getLangJena510(String langName) {
        switch (langName) {
            case "RRX.RDFXML_SAX":
                return org.apache.shadedJena510.riot.lang.rdfxml.RRX.RDFXML_SAX;
            case "RRX.RDFXML_StAX_ev":
                return org.apache.shadedJena510.riot.lang.rdfxml.RRX.RDFXML_StAX_ev;
            case "RRX.RDFXML_StAX_sr":
                return org.apache.shadedJena510.riot.lang.rdfxml.RRX.RDFXML_StAX_sr;

            case "RRX.RDFXML_ARP0":
                return org.apache.shadedJena510.riot.lang.rdfxml.RRX.RDFXML_ARP0;
            case "RRX.RDFXML_ARP1":
                return org.apache.shadedJena510.riot.lang.rdfxml.RRX.RDFXML_ARP1;

            default:
                throw new IllegalArgumentException("Unknown lang: " + langName);
        }
    }

    @Benchmark
    public Graph parseXML() throws Exception {
        final var graph = new GraphMem2Fast();
        try(final var is = new BufferedFileChannelInputStream.Builder()
                .setFile(this.param0_GraphUri)
                .setOpenOptions(StandardOpenOption.READ)
                .setBufferSize(64*4096)
                .get()) {
            RDFParser.source(is)
                    .base("xx:")
                    .forceLang(getLang(this.param1_ParserLang))
                    .checking(false)
                    .parse(graph);
        }
        return graph;
    }

    @Benchmark
    public org.apache.shadedJena510.graph.Graph parseXMLJena510() throws Exception {
        final var graph = new org.apache.shadedJena510.mem2.GraphMem2Fast();
        try(final var is = new BufferedFileChannelInputStream.Builder()
                .setFile(this.param0_GraphUri)
                .setOpenOptions(StandardOpenOption.READ)
                .setBufferSize(64*4096)
                .get()) {
            org.apache.shadedJena510.riot.RDFParser.source(is)
                    .base("xx:")
                    .forceLang(getLangJena510(this.param1_ParserLang))
                    .checking(false)
                    .parse(graph);
        }
        return graph;
    }

    @Setup(Level.Trial)
    public void setup() {
        org.apache.shadedJena510.riot.lang.rdfxml.RRX.register();
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JMHDefaultOptions.getDefaults(this.getClass())
                .warmupIterations(2)
                .measurementIterations(4)
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }

}
