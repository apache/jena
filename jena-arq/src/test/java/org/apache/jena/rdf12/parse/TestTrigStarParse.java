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
 * Test parsing of RDF-star constructs for TriG.
 */
public class TestTrigStarParse {

    private ErrorHandler silent = ErrorHandlerFactory.errorHandlerStrictNoLogging;
    private static StreamRDF sink = StreamRDFLib.sinkNull();

    @Test public void parse_trig_good_tripleTerm_1()    { parse(":x :q <<(:s :p :o)>>  . "); }

    @Test public void parse_trig_good_tripleTerm_2()    { parse(":x :p <<( :s :p <<( :Ls1 :1p :o1 )>> )>> . "); }

    @Test public void parse_trig_good_reifiedTriple_1()    { parse("<<:s :p :o>> :q 1 . "); }

    @Test public void parse_trig_good_reifiedTriple_2()    { parse(":x :p <<:s :p :o>> . "); }

    @Test public void parse_trig_good_reifiedTriple_3()    { parse(":x :p [ :q <<:s :p :o>> ]. "); }

    @Test public void parse_trig_good_reifiedTriple_4()    { parse("( <<:s :p :o>> ) :p :z . "); }

    @Test public void parse_trig_good_reifiedTriple_5()    { parse("<<:s :p :o>> . "); }

    @Test public void parse_trig_good_reifiedTriple_10()   { parse("<<:s :p <<:x :r :z >>>> :q 1 . "); }

    @Test public void parse_trig_good_reifiedTriple_20()   { parse(":a :p <<:s :p <<:x :r :z >>>> . "); }

    @Test
    public void parse_trig_bad_1()           { parseException("GRAPH <<(:s :p :o)>> {} "); }

    @Test
    public void parse_trig_bad_2()           { parseException("<<(:s :p :o)>> { :s :p :o } "); }

    private void parseException(String string) {
        assertThrows(RiotException.class, ()->parse(string));
    }

    private void parse(String string) {
        string = "PREFIX : <http://example/>\n"+string;
        RDFParser.fromString(string, Lang.TRIG).errorHandler(silent).parse(sink);
    }
}
