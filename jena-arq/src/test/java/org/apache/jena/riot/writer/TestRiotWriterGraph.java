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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.*;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestRiotWriterGraph extends AbstractWriterTest {

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of(RDFFormat.RDFNULL),

                 Arguments.of(RDFFormat.NTRIPLES_UTF8),
                 Arguments.of(RDFFormat.NTRIPLES_ASCII),
                 Arguments.of(RDFFormat.NTRIPLES),
                 Arguments.of(RDFFormat.TURTLE),
                 Arguments.of(RDFFormat.TURTLE_PRETTY),
                 Arguments.of(RDFFormat.TURTLE_BLOCKS),
                 Arguments.of(RDFFormat.TURTLE_FLAT),
                 Arguments.of(RDFFormat.TURTLE_LONG),
                 Arguments.of(RDFFormat.RDFXML),
                 Arguments.of(RDFFormat.RDFXML_PRETTY),
                 Arguments.of(RDFFormat.RDFXML_PLAIN),

                 Arguments.of(RDFFormat.JSONLD),
                 Arguments.of(RDFFormat.JSONLD_PRETTY),
                 Arguments.of(RDFFormat.JSONLD_FLAT),

                 Arguments.of(RDFFormat.JSONLD11),
                 Arguments.of(RDFFormat.JSONLD11_PRETTY),
                 Arguments.of(RDFFormat.JSONLD11_FLAT),

                 Arguments.of(RDFFormat.RDFJSON),

                 Arguments.of(RDFFormat.TRIG),
                 Arguments.of(RDFFormat.TRIG_PRETTY),
                 Arguments.of(RDFFormat.TRIG_BLOCKS),
                 Arguments.of(RDFFormat.TRIG_FLAT),
                 Arguments.of(RDFFormat.TRIG_LONG),
                 Arguments.of(RDFFormat.NQUADS_UTF8),
                 Arguments.of(RDFFormat.NQUADS_ASCII),
                 Arguments.of(RDFFormat.NQUADS),

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

    @Test public void writer00() { test("writer-rt-00.ttl"); }
    @Test public void writer01() { test("writer-rt-01.ttl"); }
    @Test public void writer02() { test("writer-rt-02.ttl"); }
    @Test public void writer03() { test("writer-rt-03.ttl"); }
    @Test public void writer04() { test("writer-rt-04.ttl"); }
    @Test public void writer05() { test("writer-rt-05.ttl"); }
    @Test public void writer06() { test("writer-rt-06.ttl"); }
    @Test public void writer07() { test("writer-rt-07.ttl"); }
    @Test public void writer08() { test("writer-rt-08.ttl"); }

    private static boolean isJsonLDJava(RDFFormat format) {
        return Lang.JSONLD.equals(format.getLang());
    }

    @Test public void writer09() {
        // Bad list
        if ( ! isJsonLDJava(format) )
            test("writer-rt-09.ttl");
    }

    @Test public void writer10() {
        // Bad list
        if ( ! isJsonLDJava(format) )
            test("writer-rt-10.ttl");
    }

    @Test public void writer11() { test("writer-rt-11.ttl"); }
    @Test public void writer12() { test("writer-rt-12.ttl"); }
    @Test public void writer13() { test("writer-rt-13.ttl"); }
    @Test public void writer14() { test("writer-rt-14.ttl"); }
    @Test public void writer15() { test("writer-rt-15.ttl"); }
    @Test public void writer16() { test("writer-rt-16.ttl"); }
    @Test public void writer17() { test("writer-rt-17.ttl"); }
    @Test public void writer18() { test("writer-rt-18.ttl"); }

    private void test(String filename) {
        String displayname = filename.substring(0, filename.lastIndexOf('.'));
        Model m = readModel(filename);
        Lang lang = format.getLang();

        WriterGraphRIOT rs = RDFWriterRegistry.getWriterGraphFactory(format).create(format);
        assertNotNull(rs);
        assertEquals(lang, rs.getLang());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RDFDataMgr.write(out, m, format);

        if ( lang == Lang.RDFNULL )
            return;

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        String s = StrUtils.fromUTF8bytes(out.toByteArray());

        Model m2 = ModelFactory.createDefaultModel();

        try {
            RDFDataMgr.read(m2, in, lang);
        } catch (RiotException ex) {
            System.out.println(format);
            System.out.println(s);
            throw ex;
        }

        boolean b = m.isIsomorphicWith(m2);
        if ( !b ) {
            System.out.println("------[" + format + "]---------------------------------------------------");

            System.out.println("#### file=" + displayname);
            System.out.print(s);
            System.out.println("--- model");
            RDFDataMgr.write(System.out, m, Lang.NT);
            System.out.println("--- model2");
            RDFDataMgr.write(System.out, m2, Lang.NT);
            System.out.println("---");
        }

        assertTrue(b, ()->"Did not round-trip file=" + filename + " / format=" + format);
    }
}

