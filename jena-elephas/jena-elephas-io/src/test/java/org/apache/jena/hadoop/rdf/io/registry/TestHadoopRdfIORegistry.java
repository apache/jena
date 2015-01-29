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
package org.apache.jena.hadoop.rdf.io.registry;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the {@link HadoopRdfIORegistry}
 */
public class TestHadoopRdfIORegistry {

    private void testLang(Lang lang, boolean triples, boolean quads, boolean writesSupported) {
        Assert.assertEquals(triples, HadoopRdfIORegistry.hasTriplesReader(lang));
        Assert.assertEquals(quads, HadoopRdfIORegistry.hasQuadReader(lang));

        // Some formats may be asymmetric
        if (writesSupported) {
            Assert.assertEquals(triples, HadoopRdfIORegistry.hasTriplesWriter(lang));
            Assert.assertEquals(quads, HadoopRdfIORegistry.hasQuadWriter(lang));
        } else {
            Assert.assertFalse(HadoopRdfIORegistry.hasTriplesWriter(lang));
            Assert.assertFalse(HadoopRdfIORegistry.hasQuadWriter(lang));
        }

        if (triples) {
            // Check that triples are supported
            RecordReader<LongWritable, TripleWritable> tripleReader;
            try {
                tripleReader = HadoopRdfIORegistry.createTripleReader(lang);
                Assert.assertNotNull(tripleReader);
            } catch (IOException e) {
                Assert.fail("Registry indicates that " + lang.getName()
                        + " can read triples but fails to produce a triple reader when asked: " + e.getMessage());
            }

            if (writesSupported) {
                RecordWriter<NullWritable, TripleWritable> tripleWriter;
                try {
                    tripleWriter = HadoopRdfIORegistry.createTripleWriter(lang, new StringWriter(), new Configuration(
                            false));
                    Assert.assertNotNull(tripleWriter);
                } catch (IOException e) {
                    Assert.fail("Registry indicates that " + lang.getName()
                            + " can write triples but fails to produce a triple writer when asked: " + e.getMessage());
                }
            }
        } else {
            // Check that triples are not supported
            try {
                HadoopRdfIORegistry.createTripleReader(lang);
                Assert.fail("Registry indicates that " + lang.getName()
                        + " cannot read triples but produced a triple reader when asked (error was expected)");
            } catch (IOException e) {
                // This is expected
            }
            try {
                HadoopRdfIORegistry.createTripleWriter(lang, new StringWriter(), new Configuration(false));
                Assert.fail("Registry indicates that " + lang.getName()
                        + " cannot write triples but produced a triple write when asked (error was expected)");
            } catch (IOException e) {
                // This is expected
            }
        }

        if (quads) {
            // Check that quads are supported
            RecordReader<LongWritable, QuadWritable> quadReader;
            try {
                quadReader = HadoopRdfIORegistry.createQuadReader(lang);
                Assert.assertNotNull(quadReader);
            } catch (IOException e) {
                Assert.fail("Registry indicates that " + lang.getName()
                        + " can read quads but fails to produce a quad reader when asked: " + e.getMessage());
            }

            if (writesSupported) {
                RecordWriter<NullWritable, QuadWritable> quadWriter;
                try {
                    quadWriter = HadoopRdfIORegistry.createQuadWriter(lang, new StringWriter(),
                            new Configuration(false));
                    Assert.assertNotNull(quadWriter);
                } catch (IOException e) {
                    Assert.fail("Registry indicates that " + lang.getName()
                            + " can write quads but fails to produce a triple writer when asked: " + e.getMessage());
                }
            }
        } else {
            try {
                HadoopRdfIORegistry.createQuadReader(lang);
                Assert.fail("Registry indicates that " + lang.getName()
                        + " cannot read quads but produced a quad reader when asked (error was expected)");
            } catch (IOException e) {
                // This is expected
            }
            try {
                HadoopRdfIORegistry.createQuadWriter(lang, new StringWriter(), new Configuration(false));
                Assert.fail("Registry indicates that " + lang.getName()
                        + " cannot write quads but produced a quad writer when asked (error was expected)");
            } catch (IOException e) {
                // This is expected
            }
        }
    }

    @Test
    public void json_ld_registered() {
        testLang(Lang.JSONLD, true, true, true);
    }

    @Test
    public void nquads_registered() {
        testLang(Lang.NQUADS, false, true, true);
        testLang(Lang.NQ, false, true, true);
    }

    @Test
    public void ntriples_registered() {
        testLang(Lang.NTRIPLES, true, false, true);
        testLang(Lang.NT, true, false, true);
    }

    @Test
    public void rdf_json_registered() {
        testLang(Lang.RDFJSON, true, false, true);
    }

    @Test
    public void rdf_xml_registered() {
        testLang(Lang.RDFXML, true, false, true);
    }

    @Test
    public void rdf_thrift_registered() {
        testLang(RDFLanguages.THRIFT, true, true, true);
    }

    @Test
    public void trig_registered() {
        testLang(Lang.TRIG, false, true, true);
    }

    @Test
    public void trix_registered() {
        testLang(Lang.TRIX, false, true, true);
    }

    @Test
    public void turtle_registered() {
        testLang(Lang.TURTLE, true, false, true);
        testLang(Lang.TTL, true, false, true);
        testLang(Lang.N3, true, false, true);
    }

    @Test
    public void unregistered() {
        testLang(Lang.RDFNULL, false, false, true);
    }
}
