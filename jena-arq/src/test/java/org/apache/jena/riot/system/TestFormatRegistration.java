
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

package org.apache.jena.riot.system;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestFormatRegistration
{
    public static Stream<Arguments> provideArgs() {
        List<Arguments> x = new ArrayList<>();
        add(x, "NULL",     RDFFormat.RDFNULL,           false, false);
        add(x, "RDFXML",   RDFFormat.RDFXML,            true, false);
        add(x, "RDFXML",   RDFFormat.RDFXML_ABBREV,     true, false);
        add(x, "RDFXML",   RDFFormat.RDFXML_PLAIN,      true, false);
        add(x, "RDFXML",   RDFFormat.RDFXML_PLAIN,      true, false);

        add(x, "NTRIPLES", RDFFormat.NTRIPLES,          true, false);
        add(x, "NT",       RDFFormat.NT,                true, false);
        add(x, "TURTLE",   RDFFormat.TURTLE,            true, false);
        add(x, "TTL",      RDFFormat.TTL,               true, false);
        add(x, "JSONLD",   RDFFormat.JSONLD,            true, true);
        add(x, "RDFJSON",  RDFFormat.RDFJSON,           true, false);
        add(x, "NQUADS",   RDFFormat.NQUADS,            true, true);
        add(x, "NQ",       RDFFormat.NQ,                true, true);
        add(x, "TRIG",     RDFFormat.TRIG,              true, true);
        add(x, "TRIX",     RDFFormat.TRIX,              true, true);

        add(x, "PB RDF",   RDFFormat.RDF_PROTO,         true, true);
        add(x, "PB RDF",   RDFFormat.RDF_PROTO_VALUES,  true, true);

        add(x, "TRDF",     RDFFormat.RDF_THRIFT,        true, true);
        add(x, "TRDF",     RDFFormat.RDF_THRIFT_VALUES, true, true);
        return x.stream();
    }

    private static void add(List<Arguments> x, String name, RDFFormat format, boolean istriples, boolean isquads) {
        x.add(Arguments.of(name, format, istriples , isquads));
    }

    private String name;
    private RDFFormat format;
    private boolean istriples;
    private boolean isquads;

    public TestFormatRegistration(String name, RDFFormat format, boolean istriples, boolean isquads) {
        this.name = name;
        this.format = format;
        this.istriples = istriples;
        this.isquads = isquads;
    }

    @Test public void jenaSystem_write_1() {
        assertTrue(RDFWriterRegistry.contains(format));
    }

    @Test public void jenaSystem_write_2() {
        assertTrue(RDFWriterRegistry.registeredGraphFormats().contains(format));
        if ( istriples ) assertNotNull(RDFWriterRegistry.getWriterGraphFactory(format));
        if ( isquads )   assertNotNull(RDFWriterRegistry.getWriterDatasetFactory(format));
    }

  @Test public void xjenaSystem_write_3() {
      RDFWriterRegistry.contains(format);
      if ( istriples )
          assertTrue(RDFWriterRegistry.registeredGraphFormats().contains(format));
      if ( isquads )
          assertTrue(RDFWriterRegistry.registeredDatasetFormats().contains(format));
  }
}

