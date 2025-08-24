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

package org.apache.jena.ttl_test.test.turtle;

import junit.framework.*;

import java.io.*;

import org.apache.jena.ttl_test.turtle.TurtleEventNull;
import org.apache.jena.ttl_test.turtle.parser.TurtleParser;
import org.apache.jena.util.junit.TestUtils;

public class TestTurtleInternal extends TestSuite
{
    static public TestSuite suite() {
        return new TestTurtleInternal();
    }

	static public final String QUOTE3 = "\"\"\"";
	static public boolean VERBOSE = false;

    public TestTurtleInternal() {
		super("Turtle Parser Syntactic tests");

        // ---- Debug testing
        //addTest("<thing> b:px.b:py [] . ");
        // if ( true ) return;
        // ---- Debug testing

		// Make sure basic things, at least, parse.

		// URIs, qnames, statements, prefixes

        // End of statement (and whitespace)
        addTest("a:subj a:prop a:d .");
        addTest("a:subj a:prop a:d . ");
        addTest("a:subj a:prop a:d.");
        addTest("a:subj a:prop a:d. ");

        addTest("rdf: rdf:type :_.");
        addTest("@prefix start: <somewhere>.");
        addTest("<http://here/subj> <http://here/prep> <http://here/obj>.");

		// Whitespace, comments
		addTest("a:subj\ta:prop\ta:d.\t");
		addTest("       a:subj\ta:prop\ta:d.     ");
		addTest("a:subj a:prop a:d.  ");
		addTest("");
		addTest(" #Comment");
		addTest("a:subj a:prop a:d.  # Comment");
		addTest("a:subj a:prop a:d.# Comment");

		// Literal: strings
		addTest("a:subj a:prop 'string1'.");
		addTest("a:subj a:prop \"string2\".");
		addTest("a:subj a:prop '''string3'''.");
		addTest("a:subj a:prop "+QUOTE3+"string3"+QUOTE3+".");

		// Literals: datatypes
		addTest("a:subj a:prop 'string1'^^x:dt.");
		addTest("a:subj a:prop 'string1'^^<uriref>.");

        // Literals: numbers.
        addTest("a: :p 2. .");
        addTest("a: :p +2. .");
        addTest("a: :p -2 .");
        addTest("a: :p 2e6.");
        addTest("a: :p 2e-6.");
        addTest("a: :p -2e-6.");
        addTest("a: :p 2.0e-6.");
        addTest("a: :p 2.0 .");

//		// The "unusual" cases
//		addTest("a:subj 'prop'^^<uriref> 'string'.");
//		addTest("a:subj a:prop 'string1'^^'stringDT'.");
//
//		addTest("a:subj a:prop1 ?x ^^ x:dt.");
//		addTest("a:subj a:prop2 ?x ^^ ?x.");

		// Quotes in string
		addTest("a:subj a:prop \"\\'string2\\'\".");
		addTest("a:subj a:prop \"\\\"string2\\\"\".");
		addTest("a:subj a:prop '\\'string1\\'\'.");
		addTest("a:subj a:prop '\\\"string1\\\"\'.");

		addTest("a:q21 a:prop "+QUOTE3+"start\"finish"+QUOTE3+".");
		addTest("a:q22 a:prop "+QUOTE3+"start\"\"finish"+QUOTE3+".");
		addTest("a:q2e3 a:prop "+QUOTE3+"start\\\"\\\"\\\"finish"+QUOTE3+".");
		addTest("a:q13 a:prop "+QUOTE3+"start'''finish"+QUOTE3+".");

		addTest("a:q11 a:prop '''start'finish'''.");
		addTest("a:q12 a:prop '''start''finish'''.");
		addTest("a:q12 a:prop '''start\\'\\'\\'finish'''.");
		addTest("a:q23 a:prop '''start\"\"\"finish'''.");

		// Keywords and syntactic sugar
//		addTest("this a:prop x:y .");
//		addTest("a:subj  a   x:y .");
//		addTest("a:subj  =   x:y .");
//		addTest("a:subj  =>  x:y .");
//		addTest("a:subj  <=  x:y .");
//		// <=> is not legal : it would mean "implies and is implied by"
//        // addTest("a:subj  <=> x:y .");
//		addTest("a:subj  >- x:y -> 'value' .");
//		addTest("a:subj  >- x:y -> 'value1', 'value2' .");

		// Not keywords
		addTest("a:subj <a>  x:y .");
		addTest("<this>  a   x:y .");
		addTest("@prefix has: <uri>.");

		addTest("<>   a:prop  x:y .");
		addTest("<#>  a:prop  x:y .");

		// Object lists
		addTest("a:subj a:prop a:d, a:e.");
		addTest("a:subj a:prop a:d, '123'.");
		addTest("a:subj a:prop '123', a:e.");
        //addTest("a:subj a:prop '123', .");            // Null object list
        //addTest("a:subj a:prop '123', '456', .");     // Null object list

		// Property lists
		addTest("a:subj a:p1 a:v1;  a:p2 a:v2 .");
    	addTest("a:subj a:p1 a:v1, a:v2;  a:p2 a:v2; a:p3 'v4' ,'v5' .");
        addTest("a:subj a:p1 a:v1; .");                 // Null property list
        addTest("a:subj a:p1 a:v1; a:p2 a:v2; .");      // Null property list


		// anon nodes
		addTest("[a:prop a:val].");
		addTest("[] a:prop a:val.");
		addTest("[] a:prop [].");

		// formulae
        // The final dot (statement terminator of outer statement) is necessary
        // Inside formulae, it is not.
//        addTest("{:x :y :z} => {:x :y :z}.");
//        addTest("{:x :y :z} => {:x :y :z . }.");
//        addTest("{:x :y :z. } => {:x :y :z}.");

		// Variables
//		addTest("?who ?knows ?what .");
//		addTest("{?who ?knows ?what} => {'somesort' 'of' 'logic'}." );

		// Formulae do not need the trailing '.'
//		addTest("{ this a \"string2\". } => { this a 'string1'} .");

		// And they can have directives in.
//		addTest("{ @prefix : <a> } => { this a 'string1'} .");
//		addTest("{ @prefix : <a> . a:x <b> 'c'} => { this a 'string1'} .");

		// RDF collections
		//addTest("() .");
		addTest("<here> <list> ().");
		addTest(" ( a:i1 a:i2 a:i3 ) a rdf:List.");

		// Paths
//		addTest(":x!:y <prop> [].");
//		addTest(":x!:y!:z <prop> [].");
//		addTest(":x^:y <prop> [].");
//		addTest(":x^:y^:z <prop> [].");
//		addTest("[] <prop> :x!:y^:z.");
//		addTest("[] :x^:y!:z [].");

        // Paths - using . (dot)
//        addTest(":x.:y <prop> [].");
//        addTest(":x.:y.:z <prop> [].");
//        addTest("[] <prop> :a.:c.");
//        addTest("<thing>.:y  <prop> [].");
//        addTest("x:x.<thing>.:y  <prop> [].");
//        addTest("<thing>.:y^:z  <prop> [].");
//        addTest(":y.<thing>.:z  <prop> [].");
//        addTest("<thing> :px.:py.:pz [] . ");
//        addTest("<thing> :px!:py!:pz [] . ");

        // Paths and formulae
//        addTest("{ :a.:b.:c . }.");
//        addTest("{ :a.:b.<c>.}.");

		// Named things
//		addTest("_:anon :- [a:p a:v] .");
//		addTest("<uri> :- [a:p [ a:p a:v] ] .");
//		// Named list: Not supported by cwm (as of 2001, 2002, 2003/09) but needed for printing shared
//		addTest("_:anon :- (\"1\") .");
//		// Named formulae: Not supported by cwm (as of 2001, 2002, 2003/09)
//		addTest("_:anon :- { ?a ?b ?c } .");

        // Datatypes
        addTest("a:subj a:prop '123'^^xsd:integer .");
        addTest("a:subj a:prop '123'^^<uri> .");
        addTest("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral .");

        // Numbers
        addTest("a:subj a:prop 123 .");
        // addTest("a:subj a:prop 123."); Illegal N3
        addTest("a:subj a:prop 123.1 .");
        addTest("a:subj a:prop -123.1 .");
        addTest("a:subj a:prop 123.1e3 .");
        addTest("a:subj a:prop 123.1e-3 .");
        addTest("a:subj a:prop 123.1E3 .");
        addTest("a:subj a:prop 123.1E-3 .");

        // Language tags
        addTest("a:subj a:prop 'text'@en .");
        // Illegal in N-Triples
        //addTest("a:subj a:prop 'text'^^a:lang@en .");
        //addTest("a:subj a:prop 'text'@en^^a:lang ."); // Can't have both

        // XML Literal
        addTest("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral ."); // Can't have both
//        addTest("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral@fr .");

        //addTest("a:subj a:prop ?x^^xsd:integer ."); // No varibales
        //addTest("a:subj a:prop '123'^^?x .");
        //addTest("a:subj a:prop ?x^^?y .");

        // Unicode 00E9 is e-acute
        // Unicode 03B1 is alpha
        addTest("a:subj a:prop '\u00E9'.");
        addTest("a:subj a:prop '\u003B1'.");

        addTest("\u00E9:subj a:prop '\u00E9'.");
        addTest("a:subj-\u00E9 a:prop '\u00E9'.");

        addTest("\u03B1:subj a:prop '\u03B1'.");
        addTest("a:subj-\u03B1 a:prop '\u03B1'.");
	}

	void addTest(String string) {  addTest(new Test(string)); }

	static class Test extends TestCase
	{
		String testString;

		Test(String s) { super(TestUtils.safeName(s)); testString = s; }

		@Override
        protected void runTest() throws Throwable
		{
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
            parser.parse();
		}
	}
}
