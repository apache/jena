/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3.test;

import com.hp.hpl.jena.n3.* ;
import junit.framework.* ;
import java.io.* ;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3InternalTests.java,v 1.3 2003-01-28 18:24:52 andy_seaborne Exp $
 */
public class N3InternalTests extends TestSuite
{
	static public final String QUOTE3 = "\"\"\"" ;
	static public boolean VERBOSE = false ;
	PrintWriter pw = null ;
	
	N3InternalTests()
	{
		super("N3 Parser Syntactic tests") ;
		
		if ( VERBOSE )
			pw = new PrintWriter(System.out) ;
		
		// Make sure basic things at least parse.
		
		// URIs, qnames, statements, prefixes
		addTest(new Test("a:subj a:prop a:d .")) ;
		addTest(new Test("a: _: 1:.")) ;
		addTest(new Test("_:a :1 :_1.")) ;	
		addTest(new Test("rdf: rdf:type :_.")) ;	
		addTest(new Test("@prefix start: <somewhere>.")) ;
		addTest(new Test("<http://here/subj> <http://here/prep> <http://here/obj>.")) ;		
		
		// Whitespace, comments
		addTest(new Test("a:subj\ta:prop\ta:d.\t")) ;
		addTest(new Test("       a:subj\ta:prop\ta:d.     ")) ;
		addTest(new Test("a:subj a:prop a:d.  ")) ;
		addTest(new Test("")) ;
		addTest(new Test(" #Comment")) ;
		addTest(new Test("a:subj a:prop a:d.  # Comment")) ;
		addTest(new Test("a:subj a:prop a:d.# Comment")) ;
		

		// Literals
		//addTest(new Test("a:subj a:prop 123.")) ;
		//addTest(new Test("a:subj a:prop -123.")) ;
		//addTest(new Test("a:subj a:prop +123.")) ;
		addTest(new Test("a:subj a:prop 'string1'.")) ;
		addTest(new Test("a:subj a:prop \"string2\".")) ;
		addTest(new Test("a:subj a:prop '''string3'''.")) ;
		addTest(new Test("a:subj a:prop "+QUOTE3+"string3"+QUOTE3+".")) ;
		
		// Literals: datatypes
		addTest(new Test("a:subj a:prop 'string1'^^x:dt.")) ;
		addTest(new Test("a:subj a:prop 'string1'^^<uriref>.")) ;

		// The "unusual" cases
		addTest(new Test("a:subj 'prop'^^<uriref> 'string'.")) ;
		addTest(new Test("a:subj a:prop 'string1'^^'stringDT'.")) ;

		// Broken
		//addTest(new Test("a:subj a:prop1 ?x ^^ x:dt.")) ;
		//addTest(new Test("a:subj a:prop2 ?x ^^ ?x.")) ;

		// Quotes in string
		addTest(new Test("a:subj a:prop \"\\'string2\\'\".")) ;
		addTest(new Test("a:subj a:prop \"\\\"string2\\\"\".")) ;
		addTest(new Test("a:subj a:prop '\\'string1\\'\'.")) ;
		addTest(new Test("a:subj a:prop '\\\"string1\\\"\'.")) ;
		
		addTest(new Test("a:q21 a:prop "+QUOTE3+"start\"finish"+QUOTE3+".")) ;
		addTest(new Test("a:q22 a:prop "+QUOTE3+"start\"\"finish"+QUOTE3+".")) ;
		addTest(new Test("a:q2e3 a:prop "+QUOTE3+"start\\\"\\\"\\\"finish"+QUOTE3+".")) ;
		addTest(new Test("a:q13 a:prop "+QUOTE3+"start'''finish"+QUOTE3+".")) ;
		
		addTest(new Test("a:q11 a:prop '''start'finish'''.")) ;
		addTest(new Test("a:q12 a:prop '''start''finish'''.")) ;
		addTest(new Test("a:q12 a:prop '''start\\'\\'\\'finish'''.")) ;
		addTest(new Test("a:q23 a:prop '''start\"\"\"finish'''.")) ;
		
		
		// Keywords and syntactic sugar
		addTest(new Test("this a:prop x:y .")) ;
		addTest(new Test("a:subj  a   x:y .")) ;
		addTest(new Test("a:subj  =   x:y .")) ;
		addTest(new Test("a:subj  =>  x:y .")) ;
		addTest(new Test("a:subj  <=  x:y .")) ;
		//addTest(new Test("a:subj  <=> x:y .")) ;
		addTest(new Test("a:subj  >- x:y -> 'value' .")) ;
		addTest(new Test("a:subj  >- x:y -> 'value1', 'value2' .")) ;


		
		// Not keywords
		addTest(new Test("a:subj <a>  x:y .")) ;
		addTest(new Test("<this>  a   x:y .")) ;
		addTest(new Test("@prefix has: <uri>.")) ;
		addTest(new Test("has:s a:a :of.")) ;
		
		addTest(new Test("<>   has a:prop  x:y .")) ;
		addTest(new Test("x:v  is a:prop of  <>.")) ;			// Reverses subject and object
		addTest(new Test("x:v  is a:prop of  a:s1, a:s2.")) ;	// Reverses subject and object
		addTest(new Test("<>   a:prop  x:y .")) ;
		addTest(new Test("<#>  a:prop  x:y .")) ;
		
		
		// Object lists
		addTest(new Test("a:subj a:prop a:d, a:e.")) ;
		addTest(new Test("a:subj a:prop a:d, '123'.")) ;
		addTest(new Test("a:subj a:prop '123', a:e.")) ;

		// Property lists
		addTest(new Test("a:subj a:p1 a:v1 ;  a:p2 a:v2 .")) ;
		addTest(new Test("a:subj a:p1 a:v1, a:v2 ;  a:p2 a:v2 ; a:p3 'v4' ,'v5' .")) ;
		
		// anon nodes
		addTest(new Test("[a:prop a:val].")) ;
		addTest(new Test("[] a:prop a:val.")) ;
		addTest(new Test("[] a:prop [].")) ;
		
		//formulae
		addTest(new Test("{ this a \"string2\" . } => { this a 'string1' .} .")) ;
		
		// Variables
		addTest(new Test("?who ?knows ?what .")) ;
		addTest(new Test("{?who ?knows ?what} => {'somesort' 'of' 'logic'}." )) ;

		
		// Formulae do not need the trailing '.'
		addTest(new Test("{ this a \"string2\". } => { this a 'string1'} .")) ;
		
		// And can have directives in.
		addTest(new Test("{ @prefix : <a> } => { this a 'string1'} .")) ;
		addTest(new Test("{ @prefix : <a> . a:x <b> 'c'} => { this a 'string1'} .")) ;
		
		// (DAML) lists
		addTest(new Test("() a daml:list.")) ;
		addTest(new Test("<here> <list> ().")) ;
		addTest(new Test(" ( a:i1 a:i2 a:i3 ) a daml:list.")) ;
		
		//Paths
		addTest(new Test(":x!:y <prop> [].")) ;
		addTest(new Test(":x!:y!:z <prop> [].")) ;
		addTest(new Test(":x^:y <prop> [].")) ;
		addTest(new Test(":x^:y^:z <prop> [].")) ;
		addTest(new Test("[] <prop> :x!:y^:z.")) ;
		addTest(new Test("[] :x^:y!:z [].")) ;
		
		// Named things
		addTest(new Test("_:anon :- [a:p a:v] .")) ;
		addTest(new Test("<uri> :- [a:p [ a:p a:v] ] .")) ;		
		// Named list: Not supported by cwm (as of 2002/09) but needed for printing shared 
		addTest(new Test("_:anon :- (\"1\") .")) ;
		// Named formulae: Not supported by cwm (as of 2002/09)
		addTest(new Test("_:anon :- { ?a ?b ?c } .")) ;
        
        // Datatypes
        addTest(new Test("a:subj a:prop '123'^^xsd:integer .")) ;
        addTest(new Test("a:subj a:prop '123'^^<uri> .")) ;
        addTest(new Test("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral .")) ;

        // Language tags
        addTest(new Test("a:subj a:prop 'text'@en .")) ;
        addTest(new Test("a:subj a:prop 'text'@en^^a:lang .")) ;
        // Illegal in N-Triples
        addTest(new Test("a:subj a:prop 'text'^^a:lang@en .")) ;
        
        // XML Literal
        addTest(new Test("a:subj a:prop '<tag>text</tag>'@fr^^rdf:XMLLiteral .")) ;
        addTest(new Test("a:subj a:prop '<tag>text</tag>'^^rdf:XMLLiteral@fr .")) ;

        addTest(new Test("a:subj a:prop ?x^^xsd:integer .")) ;
        addTest(new Test("a:subj a:prop '123'^^?x .")) ;
        addTest(new Test("a:subj a:prop ?x^^?y .")) ;
	}
	
	class Test extends TestCase
	{
		N3ParserEventHandler handler ;
		String testString ;
		Test(String s)
		{
			super("N3 Internal test: "+(s!=null?s:"<skipped test>")) ;
			testString = s ; 
			if ( VERBOSE )
				handler = new N3EventPrinter(pw) ;
			else
				handler = new NullN3EventHandler() ;
		}
	
		
		protected void runTest() throws Throwable
		{
			if ( testString == null )
			{
				if ( pw != null )
					pw.println("Skipped test") ;
					return ;
			}
			
			
			if ( pw != null )
				pw.println("Input: "+testString) ;
			N3Parser n3Parser =
				new N3Parser(new StringReader(testString), handler);
			n3Parser.parse();
			if ( pw != null )
			{
				pw.println() ;
				pw.flush() ;
			}
		}
	}
}

/*
 *  (c) Copyright Hewlett-Packard Company 2002
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
