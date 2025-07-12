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

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.util.IsoMatcher;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestTriXWriter {

    static String DIR = "testing/RIOT/Lang/TriX";

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of(DIR+"/trix-01.trix", DIR+"/trix-01.nq" ) ,
                 Arguments.of(DIR+"/trix-02.trix", DIR+"/trix-02.nq" ) ,
                 Arguments.of(DIR+"/trix-03.trix", DIR+"/trix-03.nq" ) ,
                 Arguments.of(DIR+"/trix-04.trix", DIR+"/trix-04.nq" ) ,
                 Arguments.of(DIR+"/trix-05.trix", DIR+"/trix-05.nq" ) ,
                 Arguments.of(DIR+"/trix-06.trix", DIR+"/trix-06.nq" ) ,
                 Arguments.of(DIR+"/trix-10.trix", DIR+"/trix-10.nq" ) ,
                 Arguments.of(DIR+"/trix-11.trix", DIR+"/trix-11.nq" ) ,
                 Arguments.of(DIR+"/trix-12.trix", DIR+"/trix-12.nq" ) ,
                 Arguments.of(DIR+"/trix-13.trix", DIR+"/trix-13.nq" ) ,
                 Arguments.of(DIR+"/trix-14.trix", DIR+"/trix-14.nq" ) ,
                 Arguments.of(DIR+"/trix-15.trix", DIR+"/trix-15.nq" ) ,
                 Arguments.of(DIR+"/trix-star-1.trix", DIR+"/trix-star-1.nq" ) ,
                 Arguments.of(DIR+"/trix-star-2.trix", DIR+"/trix-star-2.nq" )
                );
        return x.stream();
    }

    @Parameter(0)
    public String fTrix;

    @Parameter(1)
    public String fNQuads;

    @Test
    public void trix_writer() {
        DatasetGraph dsg = RDFDataMgr.loadDatasetGraph(fNQuads);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        RDFDataMgr.write(bout, dsg, Lang.TRIX);
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        DatasetGraph dsg2 = DatasetGraphFactory.create();
        RDFDataMgr.read(dsg2, bin, Lang.TRIX);
        boolean b = IsoMatcher.isomorphic(dsg, dsg2);
        assertTrue(b, "Not isomorphic");
    }
}
