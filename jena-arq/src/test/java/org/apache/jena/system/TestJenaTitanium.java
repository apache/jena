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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;
import com.apicatalog.rdf.io.error.RdfWriterException;
import com.apicatalog.rdf.io.nquad.NQuadsWriter;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.JenaTitanium;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;
import org.junit.Test;

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
                , "(_ :s :p :o)"
                , "(_ :s :p 123)"
                , "(_ :s :p 123.5)"
                , "(_ :s :p 1e10)"
                , "(_ :s :p '2021-08-10'^^xsd:date)"
                , "(_ :s :p 'foo')"
                , "(:g1 :s :p :o)"
                , "(:g1 _:x :p :o)"
                , "(:g2 _:x :p 123)"
                , "(:g2 _:x :p 'abc'@en)"
                , "(_:x _:x :p _:x)"
                //, "(_ <<:s :q :z>> :p <<:s :q :z>>)"
                ,")"
                );
        DatasetGraph dsg1 = SSE.parseDatasetGraph(dsStr);
        //RDFDataMgr.write(System.out, dsg1, Lang.NQUADS);

        RdfDataset rdfDataset = JenaTitanium.convert(dsg1);

        // Check the RdfDataset
        try ( StringWriter writer = new StringWriter() ) {
            RdfWriter w = new NQuadsWriter(writer);
            w.write(rdfDataset);
            String s = writer.toString();
            assertTrue(s.contains("_:b0"));
            assertTrue(s.contains("http://example/p"));
            assertTrue(s.contains("@en"));
        }

        DatasetGraph dsg2 = JenaTitanium.convert(rdfDataset);
        assertTrue(IsoMatcher.isomorphic(dsg1, dsg2));
    }
}
