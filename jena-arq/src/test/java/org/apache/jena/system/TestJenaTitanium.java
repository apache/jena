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

package org.apache.jena.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;
import com.apicatalog.rdf.io.error.RdfWriterException;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.JenaTitanium;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;

public class TestJenaTitanium {

    @Test
    public final void readContext_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        RDFParser.source("testing/RIOT/jsonld11/doc-1.jsonld11").parse(dsg);
        assertFalse(dsg.isEmpty());
        // "@vocab" : "http://example.org/vocab" -- not a prefix - does not end in "/" "#" or ":"
        assertEquals(0, dsg.prefixes().size());
    }

    @Test
    public final void readContextArrayPrefixes_1() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        RDFParser.source("testing/RIOT/jsonld11/doc-2.jsonld11").parse(dsg);
        assertTrue(dsg.prefixes().containsPrefix("foaf"));
        assertEquals(1, dsg.prefixes().size());
    }

    @Test
    public final void readContextArrayPrefixes_2() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        RDFParser.source("testing/RIOT/jsonld11/doc-3.jsonld11").parse(dsg);
        assertTrue(dsg.prefixes().containsPrefix("foaf"));
        assertEquals(3, dsg.prefixes().size());
        assertTrue(dsg.prefixes().containsPrefix("foaf"));
        assertTrue(dsg.prefixes().containsPrefix("foo"));
        assertTrue(dsg.prefixes().containsPrefix(""));
        assertFalse(dsg.prefixes().containsPrefix("bar"));
    }

    @Test public void convertDataset() throws IOException, RdfWriterException {
        String dsStr = StrUtils.strjoinNL
                ("(dataset"
                , "  (_ :s :p :o)"
                , "  (_ :s :p 123)"
                , "  (_ :s :p 123.5)"
                , "  (_ :s :p 1e10)"
                , "  (_ :s :p '2021-08-10'^^xsd:date)"
                , "  (_ :s :p 'foo')"
                , "  (:g1 :s :p :o)"
                , "  (:g1 _:x :p :o)"
                , "  (:g2 _:x :p 123)"
                , "  (:g2 _:x :p 'abc'@en)"
                , "  (_:x _:x :p _:x)"
                //, "  (_ <<:s :q :z>> :p <<:s :q :z>>)"
                ,")"
                );
        DatasetGraph dsg1 = SSE.parseDatasetGraph(dsStr);
        //RDFDataMgr.write(System.out, dsg1, Lang.NQUADS);

        RdfDataset rdfDataset = JenaTitanium.convert(dsg1);

        // Check the RdfDataset
        try ( StringWriter writer = new StringWriter() ) {
            @SuppressWarnings("deprecation")
            RdfWriter w = new com.apicatalog.rdf.io.nquad.NQuadsWriter(writer);
            w.write(rdfDataset);
            String s = writer.toString();
            assertTrue(s.contains("_:b0"));
            assertTrue(s.contains("http://example/p"));
            assertTrue(s.contains("@en"));
        }

        DatasetGraph dsg2 = JenaTitanium.convert(rdfDataset);
        assertTrue(IsoMatcher.isomorphic(dsg1, dsg2));
    }

    @Test public void convertDatasetWithNullGraph() throws IOException, RdfWriterException {
        // .createTxnMem() returns an implementation that does not allow tripleInQuad
        DatasetGraph dsg1 = DatasetGraphFactory.create();

        // Add a triple in quad -- the graph term is set to null.
        // The S, P, O terms can be whatever else.
        // See: https://github.com/apache/jena/issues/2578
        dsg1.add(Quad.create(
                Quad.tripleInQuad,
                NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode(),
                NodeFactory.createBlankNode()
        ));

        RdfDataset rdfDataset = JenaTitanium.convert(dsg1);

        // Try converting it back – it should not output any nulls.
        DatasetGraph dsg2 = JenaTitanium.convert(rdfDataset);
        dsg2.find().forEachRemaining(q->{
            assertNotNull(q.getGraph());
            assertTrue(q.isDefaultGraph());
        });
    }
}
