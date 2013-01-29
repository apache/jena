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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.junit.Test ;

public class TestTurtleTerms extends BaseTest
{

	static public final String QUOTE3 = "\"\"\"" ;
	static public boolean VERBOSE = false ;
	
	
	@Test public void turtle_001() { parse("a:subj a:prop a:d .") ; }
	@Test public void turtle_002() { parse("a:subj a:prop a:d . ") ; }
	@Test public void turtle_003() { parse("a:subj a:prop a:d.") ; }
	@Test public void turtle_004() { parse("a:subj a:prop a:d. ") ; }

	@Test public void turtle_005() { parse("rdf: rdf:type :_.") ; }
	@Test public void turtle_006() { parse("@prefix start: <somewhere>.") ; }
	@Test public void turtle_007() { parse("<http://here/subj> <http://here/prep> <http://here/obj>.") ; }
	        
	        // Whitespace, comments
	@Test public void turtle_008() { parse("a:subj\ta:prop\ta:d.\t") ; }
	@Test public void turtle_009() { parse("       a:subj\ta:prop\ta:d.     ") ; }
	@Test public void turtle_010() { parse("a:subj a:prop a:d.  ") ; }
	@Test public void turtle_011() { parse("") ; }
	@Test public void turtle_012() { parse(" #Comment") ; }
	@Test public void turtle_013() { parse("a:subj a:prop a:d.  # Comment") ; }
	@Test public void turtle_014() { parse("a:subj a:prop a:d.# Comment") ; }

	        // Literal: strings
	@Test public void turtle_015() { parse("a:subj a:prop 'string1'.") ; }
	@Test public void turtle_016() { parse("a:subj a:prop \"string2\".") ; }
	@Test public void turtle_017() { parse("a:subj a:prop '''string3'''.") ; }
	@Test public void turtle_018() { parse("a:subj a:prop "+QUOTE3+"string3"+QUOTE3+".") ; }
	        
	        // Literals: datatypes
	@Test public void turtle_019() { parse("a:subj a:prop 'string1'^^x:dt.") ; }
	@Test public void turtle_020() { parse("a:subj a:prop 'string1'^^<uriref>.") ; }
	        
	        // Literals: numbers.
	@Test public void turtle_021() { parse("a: :p 2.") ; }
	@Test public void turtle_022() { parse("a: :p +2.") ; }
	@Test public void turtle_023() { parse("a: :p -2 .") ; }
	@Test public void turtle_024() { parse("a: :p 2e6.") ; }
	@Test public void turtle_025() { parse("a: :p 2e-6.") ; }
	@Test public void turtle_026() { parse("a: :p -2e-6.") ; }
	@Test public void turtle_027() { parse("a: :p 2.0e-6.") ; }
	@Test public void turtle_028() { parse("a: :p 2.0 .") ; }
	        
//	      // The "unusual" cases
//	      @Test public void turtle_029() { parse("a:subj 'prop'^^<uriref> 'string'.") ; }
//	      @Test public void turtle_030() { parse("a:subj a:prop 'string1'^^'stringDT'.") ; }
//	      @Test public void turtle_031() { parse("a:subj a:prop1 ?x ^^ x:dt.") ; }
//	      @Test public void turtle_032() { parse("a:subj a:prop2 ?x ^^ ?x.") ; }

	// Quotes in string
	@Test public void turtle_033() { parse("a:subj a:prop \"\\'string2\\'\".") ; }
	@Test public void turtle_034() { parse("a:subj a:prop \"\\\"string2\\\"\".") ; }
	@Test public void turtle_035() { parse("a:subj a:prop '\\'string1\\'\'.") ; }
	@Test public void turtle_036() { parse("a:subj a:prop '\\\"string1\\\"\'.") ; }
	        
	@Test public void turtle_037() { parse("a:q21 a:prop "+QUOTE3+"start\"finish"+QUOTE3+".") ; }
	@Test public void turtle_038() { parse("a:q22 a:prop "+QUOTE3+"start\"\"finish"+QUOTE3+".") ; }
	@Test public void turtle_039() { parse("a:q2e3 a:prop "+QUOTE3+"start\\\"\\\"\\\"finish"+QUOTE3+".") ; }
	@Test public void turtle_040() { parse("a:q13 a:prop "+QUOTE3+"start'''finish"+QUOTE3+".") ; }
	        
	@Test public void turtle_041() { parse("a:q11 a:prop '''start'finish'''.") ; }
	@Test public void turtle_042() { parse("a:q12 a:prop '''start''finish'''.") ; }
	@Test public void turtle_043() { parse("a:q12 a:prop '''start\\'\\'\\'finish'''.") ; }
	@Test public void turtle_044() { parse("a:q23 a:prop '''start\"\"\"finish'''.") ; }
	        
	// Keywords and syntactic sugar (N3)
//	      @Test public void turtle_045() { parse("this a:prop x:y .") ; }
//	      @Test public void turtle_046() { parse("a:subj  a   x:y .") ; }
//	      @Test public void turtle_047() { parse("a:subj  =   x:y .") ; }
//	      @Test public void turtle_048() { parse("a:subj  =>  x:y .") ; }
//	      @Test public void turtle_049() { parse("a:subj  <=  x:y .") ; }

//	      // <=> is not legal : it would mean "implies and is implied by" 
//	        // @Test public void turtle_050() { parse("a:subj  <=> x:y .") ; }

//	      @Test public void turtle_051() { parse("a:subj  >- x:y -> 'value' .") ; }
//	      @Test public void turtle_052() { parse("a:subj  >- x:y -> 'value1', 'value2' .") ; }

	@Test public void turtle_053() { parse("a:subj <a>  x:y .") ; }
	@Test public void turtle_054() { parse("<this>  a   x:y .") ; }
	@Test public void turtle_055() { parse("@prefix has: <uri>.") ; }
	        
	@Test public void turtle_056() { parse("<>   a:prop  x:y .") ; }
	@Test public void turtle_057() { parse("<#>  a:prop  x:y .") ; }
	        
	        // Object lists
	@Test public void turtle_058() { parse("a:subj a:prop a:d, a:e.") ; }
	@Test public void turtle_059() { parse("a:subj a:prop a:d, '123'.") ; }
	@Test public void turtle_060() { parse("a:subj a:prop '123', a:e.") ; }
    // Null object list        
	//@Test public void turtle_061() { parse("a:subj a:prop '123', .") ; }
	//@Test public void turtle_062() { parse("a:subj a:prop '123', '456', .") ; }
	        
	// Property lists
	@Test public void turtle_063() { parse("a:subj a:p1 a:v1 ;  a:p2 a:v2 .") ; }
	@Test public void turtle_064() { parse("a:subj a:p1 a:v1, a:v2 ;  a:p2 a:v2 ; a:p3 'v4' ,'v5' .") ; }
	@Test public void turtle_065() { parse("a:subj a:p1 a:v1; .") ; }
	@Test public void turtle_066() { parse("a:subj a:p1 a:v1; a:p2 a:v2; .") ; }
	        
	// anon nodes
	@Test public void turtle_067() { parse("[a:prop a:val].") ; }
	@Test public void turtle_068() { parse("[] a:prop a:val.") ; }
	@Test public void turtle_069() { parse("[] a:prop [].") ; }
	        
	// formulae
	// The final dot (statement terminator of outer statement) is necessary
	// Inside formulae, it is not.
//	        @Test public void turtle_070() { parse("{:x :y :z} => {:x :y :z}.") ; }
//	        @Test public void turtle_071() { parse("{:x :y :z} => {:x :y :z . }.") ; }
//	        @Test public void turtle_072() { parse("{:x :y :z. } => {:x :y :z}.") ; }
	        
//  // Variables
//	      @Test public void turtle_073() { parse("?who ?knows ?what .") ; }
//	      @Test public void turtle_074() { parse("{?who ?knows ?what} => {'somesort' 'of' 'logic'}." ) ; }
	        
// 	// Formulae do not need the trailing '.'
//	      @Test public void turtle_075() { parse("{ this a \"string2\". } => { this a 'string1'} .") ; }
	        
//	// And they can have directives in.
//	      @Test public void turtle_076() { parse("{ @prefix : <a> } => { this a 'string1'} .") ; }
//	      @Test public void turtle_077() { parse("{ @prefix : <a> . a:x <b> 'c'} => { this a 'string1'} .") ; }

	        
	// RDF collections
	//@Test public void turtle_078() { parse("() .") ; }
	@Test public void turtle_079() { parse("<here> <list> ().") ; }
	@Test public void turtle_080() { parse(" ( a:i1 a:i2 a:i3 ) a rdf:List.") ; }
	        
	        // Paths - N3
//	      @Test public void turtle_081() { parse(":x!:y <prop> [].") ; }
//	      @Test public void turtle_082() { parse(":x!:y!:z <prop> [].") ; }
//	      @Test public void turtle_083() { parse(":x^:y <prop> [].") ; }
//	      @Test public void turtle_084() { parse(":x^:y^:z <prop> [].") ; }
//	      @Test public void turtle_085() { parse("[] <prop> :x!:y^:z.") ; }
//	      @Test public void turtle_086() { parse("[] :x^:y!:z [].") ; }
	        
	        // Paths - using . (dot) - N3
//	        @Test public void turtle_087() { parse(":x.:y <prop> [].") ; }
//	        @Test public void turtle_088() { parse(":x.:y.:z <prop> [].") ; }
//	        @Test public void turtle_089() { parse("[] <prop> :a.:c.") ; }
//	        @Test public void turtle_090() { parse("<thing>.:y  <prop> [].") ; }
//	        @Test public void turtle_091() { parse("x:x.<thing>.:y  <prop> [].") ; }
//	        @Test public void turtle_092() { parse("<thing>.:y^:z  <prop> [].") ; }
//	        @Test public void turtle_093() { parse(":y.<thing>.:z  <prop> [].") ; }
//	        @Test public void turtle_094() { parse("<thing> :px.:py.:pz [] . ") ; }
//	        @Test public void turtle_095() { parse("<thing> :px!:py!:pz [] . ") ; }
	        
	        // Paths and formulae
//	        @Test public void turtle_096() { parse("{ :a.:b.:c . }.") ; }
//	        @Test public void turtle_097() { parse("{ :a.:b.<c>.}.") ; }
	        
	        // Named things
//	      @Test public void turtle_098() { parse("_:anon :- [a:p a:v] .") ; }
//	      @Test public void turtle_099() { parse("<uri> :- [a:p [ a:p a:v] ] .") ; }
	        
//	      // Named list: Not supported by cwm (as of 2001, 2002, 2003/09) but needed for printing shared 
//	      @Test public void turtle_100() { parse("_:anon :- (\"1\") .") ; }
//	      // Named formulae: Not supported by cwm (as of 2001, 2002, 2003/09)
//	      @Test public void turtle_101() { parse("_:anon :- { ?a ?b ?c } .") ; }

	        
    // Datatypes
	@Test public void turtle_102() { parse("a:subj a:prop '123'^^xsd:integer .") ; }
	@Test public void turtle_103() { parse("a:subj a:prop '123'^^<uri> .") ; }
	@Test public void turtle_104() { parse("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral .") ; }
	        
	// Numbers
	@Test public void turtle_105() { parse("a:subj a:prop 123 .") ; }
	// @Test public void turtle_106() { parse("a:subj a:prop 123.") ; } //  Illegal N3

	@Test public void turtle_107() { parse("a:subj a:prop 123.1 .") ; }
	@Test public void turtle_108() { parse("a:subj a:prop -123.1 .") ; }
	@Test public void turtle_109() { parse("a:subj a:prop 123.1e3 .") ; }
	@Test public void turtle_110() { parse("a:subj a:prop 123.1e-3 .") ; }
	@Test public void turtle_111() { parse("a:subj a:prop 123.1E3 .") ; }
	@Test public void turtle_112() { parse("a:subj a:prop 123.1E-3 .") ; }

	// Language tags
	@Test public void turtle_113() { parse("a:subj a:prop 'text'@en .") ; }
	// Illegal in Turtle.
	//@Test public void turtle_114() { parse("a:subj a:prop 'text'^^a:lang@en .") ; }
	//@Test public void turtle_115() { parse("a:subj a:prop 'text'@en^^a:lang .") ; }
	        
	// XML Literal : "a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral ."
	//@Test public void turtle_116() { parse("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral@fr .") ; }


    // Variable in specific literal places
	//@Test public void turtle_117() { parse("a:subj a:prop ?x^^xsd:integer .") ; }
	//@Test public void turtle_118() { parse("a:subj a:prop '123'^^?x .") ; }
	//@Test public void turtle_119() { parse("a:subj a:prop ?x^^?y .") ; }

	        
	// Unicode 00E9 is e-acute
	// Unicode 03B1 is alpha
	@Test public void turtle_120() { parse("a:subj a:prop '\u00E9'.") ; }
	@Test public void turtle_121() { parse("a:subj a:prop '\u003B1'.") ; }
	        
	@Test public void turtle_122() { parse("\u00E9:subj a:prop '\u00E9'.") ; }
	@Test public void turtle_123() { parse("a:subj-\u00E9 a:prop '\u00E9'.") ; }
	        
	@Test public void turtle_124() { parse("\u03B1:subj a:prop '\u03B1'.") ; }
	@Test public void turtle_125() { parse("a:subj-\u03B1 a:prop '\u03B1'.") ; }

	@Test public void turtle_150() { parse("<x> a <y> . ") ; }
	@Test public void turtle_151() { parse("[ a <y> ] . ") ; }
    @Test public void turtle_152() { parse("[ a <y> ; a <z> ] . ") ; }
    @Test public void turtle_153() { parse("[ a <z>, <z1> ] . ") ; }
    
	public static void parse(String testString)
	{
	    // Need to access the prefix mapping.
	    
	    Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(testString) ;
	    StreamRDF sink = StreamRDFLib.sinkNull() ;
	    LangTurtle parser = RiotReader.createParserTurtle(tokenizer, "http://base/", sink) ;
	    PrefixMap prefixMap = parser.getProfile().getPrologue().getPrefixMap() ;

	    prefixMap.add("a", "http://host/a#") ;
        prefixMap.add("x", "http://host/a#") ;
        // Unicode 00E9 is e-acute
        // Unicode 03B1 is alpha
        prefixMap.add("\u00E9", "http://host/e-acute/") ;
        prefixMap.add("\u03B1", "http://host/alpha/") ;
        prefixMap.add("", "http://host/") ;
        prefixMap.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
        prefixMap.add("xsd", "http://www.w3.org/2001/XMLSchema#") ;
        parser.parse();

        tokenizer.close();
	}
}
