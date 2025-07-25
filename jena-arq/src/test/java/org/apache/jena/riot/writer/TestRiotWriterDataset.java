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

package org.apache.jena.riot.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.*;
import org.apache.jena.sparql.util.IsoMatcher;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestRiotWriterDataset extends AbstractWriterTest {

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of(RDFFormat.RDFNULL),
                 Arguments.of(RDFFormat.TRIG),
                 Arguments.of(RDFFormat.TRIG_PRETTY),
                 Arguments.of(RDFFormat.TRIG_BLOCKS),
                 Arguments.of(RDFFormat.TRIG_FLAT),
                 Arguments.of(RDFFormat.JSONLD),
                 Arguments.of(RDFFormat.JSONLD_PRETTY),
                 Arguments.of(RDFFormat.JSONLD_FLAT),
                 Arguments.of(RDFFormat.NQUADS),
                 Arguments.of(RDFFormat.NQUADS_UTF8),
                 Arguments.of(RDFFormat.NQUADS_ASCII),
                 Arguments.of(RDFFormat.RDF_PROTO),
                 Arguments.of(RDFFormat.RDF_PROTO_VALUES),
                 Arguments.of(RDFFormat.RDF_THRIFT),
                 Arguments.of(RDFFormat.RDF_THRIFT_VALUES),
                 Arguments.of(RDFFormat.TRIX)
                        );
        return x.stream();
    }

    @Parameter
    private RDFFormat format;

    private static boolean isJsonLDJava(RDFFormat format) {
        return Lang.JSONLD.equals(format.getLang());
    }


    @Test public void writer00() { test("writer-rt-20.trig"); }
    @Test public void writer01() { test("writer-rt-21.trig"); }
    @Test public void writer02() { test("writer-rt-22.trig"); }
    @Test public void writer03() { test("writer-rt-23.trig"); }
    @Test public void writer04() { test("writer-rt-24.trig"); }

    @Test public void writer05() { test("writer-rt-25.trig"); }
    @Test public void writer06() { test("writer-rt-26.trig"); }
    @Test public void writer07() { test("writer-rt-27.trig"); }
    @Test public void writer08() {
        // List across graphs
        if ( isJsonLDJava(format) )
            // Broken for JSON-LD
            return;
        test("writer-rt-28.trig");
    }
    @Test public void writer09() { test("writer-rt-29.trig"); }
    @Test public void writer10() { test("writer-rt-30.trig"); }

    private void test(String filename)
    {
        String displayname = filename.substring(0, filename.lastIndexOf('.'));
        Dataset ds = readDataset(filename);
        Lang lang = format.getLang();

        WriterDatasetRIOT rs = RDFWriterRegistry.getWriterDatasetFactory(format).create(format);
        assertEquals(lang, rs.getLang());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, ds, format);

        if ( lang == Lang.RDFNULL )
            return;

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        String s = StrUtils.fromUTF8bytes(out.toByteArray());
        Dataset ds2 = DatasetFactory.create();
        try {
            RDFDataMgr.read(ds2, in, lang);
        } catch (RiotException ex)
        {
            System.out.println(displayname+" : "+format);
            System.out.println(s);
            throw ex;
        }

        boolean b = IsoMatcher.isomorphic(ds.asDatasetGraph(), ds2.asDatasetGraph());
        if ( ! b ) {
            System.out.println("Test: "+format.toString());
            System.out.println("-- Input");
            RDFDataMgr.write(System.out, ds.asDatasetGraph(), Lang.NQUADS );
            System.out.println("-- Written");
            System.out.println(s);
            System.out.println();
            System.out.println("-- Seen as");
            RDFDataMgr.write(System.out, ds2.asDatasetGraph(), Lang.NQUADS );
            System.out.println("-------------");
        }
        assertTrue(b, "Datasets are not isomorphic");
    }
}

