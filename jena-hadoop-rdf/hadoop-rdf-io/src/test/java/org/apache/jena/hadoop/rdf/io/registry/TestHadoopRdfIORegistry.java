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

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
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
    
    private void testLang(Lang lang, boolean triples, boolean quads) {
        Assert.assertEquals(triples, HadoopRdfIORegistry.hasTriplesReader(lang));
        Assert.assertEquals(quads, HadoopRdfIORegistry.hasQuadReader(lang));
        
        if (triples) {
            RecordReader<LongWritable, TripleWritable> tripleReader;
            try {
                tripleReader = HadoopRdfIORegistry.createTripleReader(lang);
                Assert.assertNotNull(tripleReader);
            } catch (IOException e) {
                Assert.fail("Registry indicates that " + lang.getName() + " can read triples but fails to produce a triple reader when asked: " + e.getMessage());
            }
        } else {
            try {
                HadoopRdfIORegistry.createTripleReader(lang);
                Assert.fail("Registry indicates that " + lang.getName() + " cannot read triples but produced a triple reader when asked (error was expected)");
            } catch (IOException e) {
                // This is expected
            }
        }
        
        if (quads) {
            RecordReader<LongWritable, QuadWritable> quadReader;
            try {
                quadReader = HadoopRdfIORegistry.createQuadReader(lang);
                Assert.assertNotNull(quadReader);
            } catch (IOException e) {
                Assert.fail("Registry indicates that " + lang.getName() + " can read quads but fails to produce a quad reader when asked: " + e.getMessage());
            }
        } else {
            try {
                HadoopRdfIORegistry.createQuadReader(lang);
                Assert.fail("Registry indicates that " + lang.getName() + " cannot read quads but produced a quad reader when asked (error was expected)");
            } catch (IOException e) {
                // This is expected
            }
        }
    }

    @Test
    public void json_ld_registered() {
        testLang(Lang.JSONLD, true, true);
    }
    
    @Test
    public void nquads_registered() {
        testLang(Lang.NQUADS, false, true);
        testLang(Lang.NQ, false, true);
    }
    
    @Test
    public void ntriples_registered() {
        testLang(Lang.NTRIPLES, true, false);
        testLang(Lang.NT, true, false);
    }
    
    @Test
    public void rdf_json_registered() {
        testLang(Lang.RDFJSON, true, false);
    }
    
    @Test
    public void rdf_xml_registered() {
        testLang(Lang.RDFXML, true, false);
    }
    
    @Test
    public void rdf_thrift_registered() {
        testLang(RDFLanguages.THRIFT, true, true);
    }
    
    @Test
    public void trig_registered() {
        testLang(Lang.TRIG, false, true);
    }
    
    @Test
    public void trix_registered() {
        testLang(Lang.TRIX, false, true);
    }
    
    @Test
    public void turtle_registered() {
        testLang(Lang.TURTLE, true, false);
        testLang(Lang.TTL, true, false);
        testLang(Lang.N3, true, false);
    }
    
    @Test
    public void unregistered() {
        testLang(Lang.RDFNULL, false, false);
    }
}
