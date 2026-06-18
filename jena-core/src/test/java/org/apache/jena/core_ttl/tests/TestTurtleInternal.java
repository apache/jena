/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.core_ttl.tests;


import java.io.StringReader;

import org.junit.jupiter.api.Test;

import org.apache.jena.core_ttl.parser.TurtleEventNull;
import org.apache.jena.core_ttl.parser.TurtleParseException;
import org.apache.jena.core_ttl.parser.javacc.ParseException;
import org.apache.jena.core_ttl.parser.javacc.TurtleParser;

public class TestTurtleInternal
{
	static public final String QUOTE3 = "\"\"\"";
	static public boolean VERBOSE = false;

	@Test public void test_01() {
	    addTest("a:subj a:prop a:d .");
	}
	@Test public void test_02() {
	    addTest("a:subj a:prop a:d . ");
	}
	@Test public void test_03() {
	    addTest("a:subj a:prop a:d.");
	}
	@Test public void test_04() {
	    addTest("a:subj a:prop a:d. ");
	}

	@Test public void test_05() {
	    addTest("rdf: rdf:type :_.");
	}
	@Test public void test_06() {
	    addTest("@prefix start: <somewhere>.");
	}
	@Test public void test_07() {
	    addTest("<http://here/subj> <http://here/prep> <http://here/obj>.");
	}

	// Whitespace, comments
	@Test public void test_08() {
	    addTest("a:subj\ta:prop\ta:d.\t");
	}
	@Test public void test_09() {
	    addTest("       a:subj\ta:prop\ta:d.     ");
	}
	@Test public void test_10() {
	    addTest("a:subj a:prop a:d.  ");
	}
	@Test public void test_11() {
	    addTest("");
	}
	@Test public void test_12() {
	    addTest(" #Comment");
	}
	@Test public void test_13() {
	    addTest("a:subj a:prop a:d.  # Comment");
	}
	@Test public void test_14() {
	    addTest("a:subj a:prop a:d.# Comment");
	}

	// Literal: strings
	@Test public void test_15() {
	    addTest("a:subj a:prop 'string1'.");
	}
	@Test public void test_16() {
	    addTest("a:subj a:prop \"string2\".");
	}
	@Test public void test_17() {
	    addTest("a:subj a:prop '''string3'''.");
	}
	@Test public void test_18() {
	    addTest("a:subj a:prop "+QUOTE3+"string3"+QUOTE3+".");
	}

    // Literals: datatypes
	@Test public void test_19() {
	    addTest("a:subj a:prop 'string1'^^x:dt.");
	}
	@Test public void test_20() {
	    addTest("a:subj a:prop 'string1'^^<uriref>.");
	}

    // Literals: numbers.
	@Test public void test_21() {
	    addTest("a: :p 2. .");
	}
	@Test public void test_22() {
	    addTest("a: :p +2. .");
	}
	@Test public void test_23() {
	    addTest("a: :p -2 .");
	}
	@Test public void test_24() {
	    addTest("a: :p 2e6.");
	}
	@Test public void test_25() {
	    addTest("a: :p 2e-6.");
	}
	@Test public void test_26() {
	    addTest("a: :p -2e-6.");
	}
	@Test public void test_27() {
	    addTest("a: :p 2.0e-6.");
	}
	@Test public void test28() {
	    addTest("a: :p 2.0 .");
	}

    // Quotes in string
	@Test public void test29() {
	    addTest("a:subj a:prop \"\\'string2\\'\".");
	}
	@Test public void test_30() {
	    addTest("a:subj a:prop \"\\\"string2\\\"\".");
	}
	@Test public void test_31() {
	    addTest("a:subj a:prop '\\'string1\\'\'.");
	}
	@Test public void test_32() {
	    addTest("a:subj a:prop '\\\"string1\\\"\'.");
	}

	@Test public void test_33() {
	    addTest("a:q21 a:prop "+QUOTE3+"start\"finish"+QUOTE3+".");
	}
	@Test public void test_34() {
	    addTest("a:q22 a:prop "+QUOTE3+"start\"\"finish"+QUOTE3+".");
	}
	@Test public void test_35() {
	    addTest("a:q2e3 a:prop "+QUOTE3+"start\\\"\\\"\\\"finish"+QUOTE3+".");
	}
	@Test public void test_36() {
	    addTest("a:q13 a:prop "+QUOTE3+"start'''finish"+QUOTE3+".");
	}

	@Test public void test_37() {
	    addTest("a:q11 a:prop '''start'finish'''.");
	}
	@Test public void test_38() {
	    addTest("a:q12 a:prop '''start''finish'''.");
	}
	@Test public void test_39() {
	    addTest("a:q12 a:prop '''start\\'\\'\\'finish'''.");
	}
	@Test public void test_40() {
	    addTest("a:q23 a:prop '''start\"\"\"finish'''.");
	}

	// Property lists
	@Test public void test_41() {
	    addTest("a:subj a:p1 a:v1;  a:p2 a:v2 .");
	}
	@Test public void test_42() {
	    addTest("a:subj a:p1 a:v1, a:v2;  a:p2 a:v2; a:p3 'v4' ,'v5' .");
	}
	@Test public void test_43() {
	    addTest("a:subj a:p1 a:v1; .");                 // Null property list
	}
	@Test public void test_44() {
	    addTest("a:subj a:p1 a:v1; a:p2 a:v2; .");      // Null property list
	}


	// anon nodes
	@Test public void test_45() {
	    addTest("[a:prop a:val].");
	}
	@Test public void test_46() {
	    addTest("[] a:prop a:val.");
	}
	@Test public void test_47() {
	    addTest("[] a:prop [].");
	}
    // RDF collections
	@Test public void test_48() {
	    addTest("<here> <list> ().");
	}
	@Test public void test_49() {
	    addTest(" ( a:i1 a:i2 a:i3 ) a rdf:List.");
	}

	// Datatypes
	@Test public void test_50() {
	    addTest("a:subj a:prop '123'^^xsd:integer .");
	}
	@Test public void test_51() {
	    addTest("a:subj a:prop '123'^^<uri> .");
	}
	@Test public void test_52() {
	    addTest("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral .");
	}

	// Numbers
	@Test public void test_53() {
	    addTest("a:subj a:prop 123 .");
	}
	@Test public void test_54() {
	    addTest("a:subj a:prop 123.1 .");
	}
	@Test public void test_55() {
	    addTest("a:subj a:prop -123.1 .");
	}
	@Test public void test_56() {
	    addTest("a:subj a:prop 123.1e3 .");
	}
	@Test public void test_57() {
	    addTest("a:subj a:prop 123.1e-3 .");
	}
	@Test public void test_58() {
	    addTest("a:subj a:prop 123.1E3 .");
	}
	@Test public void test_59() {
	    addTest("a:subj a:prop 123.1E-3 .");
	}

	// Language tags
	@Test public void test_60() {
	    addTest("a:subj a:prop 'text'@en .");
	}
    // XML Literal
	@Test public void test_61() {
	    addTest("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral ."); // Can't have both
	}
	// Unicode 00E9 is e-acute
	// Unicode 03B1 is alpha
	@Test public void test_62() {
	    addTest("a:subj a:prop '\u00E9'.");
	}
	@Test public void test_63() {
	    addTest("a:subj a:prop '\u003B1'.");
	}

	@Test public void test_64() {
	    addTest("\u00E9:subj a:prop '\u00E9'.");
	}
	@Test public void test_65() {
	    addTest("a:subj-\u00E9 a:prop '\u00E9'.");
	}

	@Test public void test_66() {
	    addTest("\u03B1:subj a:prop '\u03B1'.");
	}
	@Test public void test_67() {
	    addTest("a:subj-\u03B1 a:prop '\u03B1'.");
	}

	static void addTest(String testString) {
	    TurtleParser parser = new TurtleParser(new StringReader(testString));
	    parser.setEventHandler(new TurtleEventNull());
	    parser.getPrefixMapping().setNsPrefix("a", "http://host/a#");
	    parser.getPrefixMapping().setNsPrefix("x", "http://host/a#");
	    // Unicode 00E9 is e-acute
	    // Unicode 03B1 is alpha
	    parser.getPrefixMapping().setNsPrefix("\u00E9", "http://host/e-acute/");
	    parser.getPrefixMapping().setNsPrefix("\u03B1", "http://host/alpha/");
	    parser.getPrefixMapping().setNsPrefix("", "http://host/");
	    parser.getPrefixMapping().setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
	    parser.getPrefixMapping().setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
	    parser.setBaseURI("http://base/");
	    try {
            parser.parse();
        } catch (ParseException e) {
            throw new TurtleParseException(e.getMessage(), e);
        }
	}
}
