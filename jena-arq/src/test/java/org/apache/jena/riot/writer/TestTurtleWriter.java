/**
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

package org.apache.jena.riot.writer;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.IOException;
import java.io.StringReader ;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.util.Context;
import org.junit.Assert ;
import org.junit.Test ;

public class TestTurtleWriter {
    // Tests data.
    static String cycle1 = "_:a <urn:xx:p> _:b . _:b <urn:xx:q> _:a ." ;
    static String cycle2 = "_:a <urn:xx:p> _:b . _:b <urn:xx:q> _:a . _:a <urn:xx:r> \"abc\" . " ;

    static String base = "http://example.org/";
    static String basetester = "@base <"+base+"> .   " +
                               "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .   " +
                               "@prefix foaf:  <http://xmlns.com/foaf/0.1/> .  " +
                               "<green-goblin> rdf:type foaf:Person ." ;
    static final Model baseTestData;
    static {
        baseTestData = ModelFactory.createDefaultModel();
        RDFParser.fromString(TestTurtleWriter.basetester)
            .lang(Lang.TTL)
            .parse(baseTestData);
    }

    /** Read in N-Triples data, which is not empty,
     *  then write-read-compare using the format given.
     *
     * @param testdata
     * @param lang
     */
    static void blankNodeLang(String testdata, RDFFormat lang) {
        StringReader r = new StringReader(testdata) ;
        Model m = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(m, r, null, RDFLanguages.NTRIPLES) ;
        Assert.assertTrue(m.size() > 0);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        RDFDataMgr.write(output, m, lang);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Model m2 = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m2, input, lang.getLang());

        Assert.assertTrue(m2.size() > 0);
        Assert.assertTrue(m.isIsomorphicWith(m2));
    }

    // Tests from JENA-908
    @Test
    public void bnode_cycle1_1() { blankNodeLang(cycle1, RDFFormat.TURTLE) ; }

    @Test
    public void bnode_cycle1_2() { blankNodeLang(cycle1, RDFFormat.TURTLE_BLOCKS) ; }

    @Test
    public void bnode_cycle1_3() { blankNodeLang(cycle1, RDFFormat.TURTLE_FLAT) ; }

    @Test
    public void bnode_cycle1_4() { blankNodeLang(cycle1, RDFFormat.TURTLE_PRETTY) ; }

    @Test
    public void bnode_cycle1_15() { blankNodeLang(cycle1, RDFFormat.TURTLE_LONG) ; }


    @Test
    public void bnode_cycles2_1() { blankNodeLang(cycle2, RDFFormat.TURTLE) ; }

    @Test
    public void bnode_cycles2_2() { blankNodeLang(cycle2, RDFFormat.TURTLE_BLOCKS) ; }

    @Test
    public void bnode_cycles2_3() { blankNodeLang(cycle2, RDFFormat.TURTLE_FLAT) ; }

    @Test
    public void bnode_cycle2_4() { blankNodeLang(cycle2, RDFFormat.TURTLE_PRETTY) ; }

    @Test
    public void bnode_cycle2_5() { blankNodeLang(cycle2, RDFFormat.TURTLE_LONG) ; }

    @Test
    public void bnode_cycles() {
        Model m = RDFDataMgr.loadModel("testing/DAWG-Final/construct/data-ident.ttl");
        Assert.assertTrue(m.size() > 0);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        RDFDataMgr.write(output, m, Lang.TURTLE);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Model m2 = ModelFactory.createDefaultModel();
        RDFDataMgr.read(m2, input, Lang.TURTLE);
        Assert.assertTrue(m2.size() > 0);

        Assert.assertTrue(m.isIsomorphicWith(m2));
    }

    // @base
    @Test
    public void test_base_1() {
        // Default base style
        String result = modelToString(baseTestData, RDFFormat.TURTLE_FLAT, null);
        int count1 = StringUtils.countMatches(result, "@base");
        Assert.assertEquals(0, count1);
        int count2 = StringUtils.countMatches(result, "BASE");
        Assert.assertEquals(1, count2);
    }

    @Test
    public void test_base_2() {
        Context cxt = RIOT.getContext().copy().set(RIOT.symTurtleDirectiveStyle, DirectiveStyle.AT);
        String result = modelToString(baseTestData, RDFFormat.TURTLE_BLOCKS, cxt);
        int count1 = StringUtils.countMatches(result, "@base");
        Assert.assertEquals(1, count1);
        int count2 = StringUtils.countMatches(result, "BASE");
        Assert.assertEquals(0, count2);
    }

    // BASE
    @Test
    public void test_base_3() {
        Context cxt = RIOT.getContext().copy().set(RIOT.symTurtleDirectiveStyle, DirectiveStyle.KEYWORD);
        String result = modelToString(baseTestData, RDFFormat.TURTLE_FLAT, cxt);
        int count1 = StringUtils.countMatches(result, "BASE");
        Assert.assertEquals(1, count1);
        int count2 = StringUtils.countMatches(result, "@base");
        Assert.assertEquals(0, count2);
    }

    @Test
    public void test_base_4() {
        Context cxt = RIOT.getContext().copy();
        cxt.set(RIOT.symTurtleDirectiveStyle, DirectiveStyle.KEYWORD);
        String result = modelToString(baseTestData, RDFFormat.TURTLE_BLOCKS, cxt);
        int count1 = StringUtils.countMatches(result, "BASE");
        Assert.assertEquals(1, count1);
        int count2 = StringUtils.countMatches(result, "@base");
        Assert.assertEquals(0, count2);
    }

    private String modelToString(Model model, RDFFormat format, Context context) {
        try(ByteArrayOutputStream o = new ByteArrayOutputStream()) {
            RDFWriter.create().source(baseTestData).format(format).base(base).context(context).output(o);
            String result = Bytes.bytes2string(o.toByteArray());
            return result;
        } catch (IOException ex) { IO.exception(ex); return null;}
    }
}

