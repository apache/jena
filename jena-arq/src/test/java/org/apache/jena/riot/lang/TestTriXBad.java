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

package org.apache.jena.riot.lang;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestTriXBad {

    static String DIR = "testing/RIOT/Lang/TriX";

    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of(DIR+"/trix-bad-01.trix"),
                 Arguments.of(DIR+"/trix-bad-02.trix"),
                 Arguments.of(DIR+"/trix-bad-03.trix"),
                 Arguments.of(DIR+"/trix-bad-04.trix"),
                 Arguments.of(DIR+"/trix-bad-05.trix"),
                 Arguments.of(DIR+"/trix-bad-06.trix"),
                 Arguments.of(DIR+"/trix-bad-07.trix"),
                 Arguments.of(DIR+"/trix-bad-08.trix"),
                 Arguments.of(DIR+"/trix-bad-09.trix"),

                 Arguments.of(DIR+"/trix-star-bad-triple-term-1.trix" ),
                 Arguments.of(DIR+"/trix-star-bad-triple-term-2.trix" ),
                 Arguments.of(DIR+"/trix-star-bad-triple-term-3.trix" ),
                 Arguments.of(DIR+"/trix-star-bad-triple-term-4.trix" )
                        );
        return x.stream();
    }

    @Parameter(0)
    public String fInput;

    @Test
    public void trix_bad() {
        ErrorHandler err = ErrorHandlerFactory.getDefaultErrorHandler();
        try {
            ErrorHandlerFactory.setDefaultErrorHandler(ErrorHandlerFactory.errorHandlerSimple());
            InputStream in = IO.openFile(fInput);
            StreamRDF sink = StreamRDFLib.sinkNull();
            assertThrows(RiotException.class, ()->RDFParser.source(in).lang(Lang.TRIX).parse(sink));
        } finally {
            ErrorHandlerFactory.setDefaultErrorHandler(err);
        }
    }
}
