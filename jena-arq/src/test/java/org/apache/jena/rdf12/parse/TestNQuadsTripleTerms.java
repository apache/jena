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

package org.apache.jena.rdf12.parse;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

/**
 * Basic parsing tests of RDF triple term constructs for N-Quads
 */
public class TestNQuadsTripleTerms {

    private ErrorHandler silent = ErrorHandlerFactory.errorHandlerStrictNoLogging;
    private static StreamRDF sink = StreamRDFLib.sinkNull();

    @Test public void parse_nq_good_1()    { parse("<x:s> <x:q> <<( <x:s> <x:p> <x:o> )>> . "); }

    @Test public void parse_nq_good_2()    { parse("<x:s> <x:p> <<( <x:s> <x:p> <x:o> )>> <http://example/g> . "); }

    @Test public void parse_nq_good_3()    { parse("<x:s> <x:q> <<( <x:s> <x:p> <<(<x:s1> <x:p1> <x:o1>)>> )>> . "); }

    @Test public void parse_nq_good_4()    { parse("<x:s> <x:p> <<( <x:s> <x:p> <<(<x:s1> <x:p1> <x:o1>)>> )>> <http://example/g> . "); }

    @Test public void parse_nq_good_5()    { parse("_:b <x:p> <<(_:b <x:p> _:o)>> _:g . "); }

    @Test
    public void parse_nq_bad_1()           { parseException("<<<x:s> <x:p> <x:o>>> <x:q> <x:z> . "); }

    @Test
    public void parse_nq_bad_2()           { parseException("<<(<x:s> <x:p> <x:o>)>> <x:q> 'ABC' . "); }

    @Test
    public void parse_nq_bad_3()           { parseException("<x:s> <<(<x:s> <x:p> <x:o>)>> <x:o> <http://example/g> . "); }

    @Test
    public void parse_nq_bad_4()           { parseException("<x:s> <x:p> <x:o> <<(<x:gs> <x:gp> <x:go> )>> . "); }

    private void parseException(String string) {
        assertThrows(RiotException.class, ()->parse(string));
    }

    private void parse(String string) {
        RDFParser.fromString(string, Lang.NQUADS).errorHandler(silent).parse(sink);
    }
}
