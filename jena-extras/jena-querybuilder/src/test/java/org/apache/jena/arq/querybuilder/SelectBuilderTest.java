/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.arq.querybuilder;

import org.apache.jena.arq.AbstractRegexpBasedTest;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.vocabulary.RDF ;
import org.junit.Before;
import org.junit.Test;

public class SelectBuilderTest extends AbstractRegexpBasedTest {

	private SelectBuilder builder;
	
    @Before
	public void setup() {
		builder = new SelectBuilder();
	}

	@Test
	public void testSelectAsterisk() {
		builder.addVar("*").addWhere("?s", "?p", "?o");

		assertContainsRegex(SELECT + "\\*" + SPACE + WHERE + OPEN_CURLY
				+ var("s") + SPACE + var("p") + SPACE + var("o") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(Var.alloc("p"), RDF.type);

		assertContainsRegex(SELECT + "\\*" + SPACE + WHERE + OPEN_CURLY
				+ var("s") + SPACE
				+ regexRDFtype
				+ SPACE + var("o") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}

	@Test
	public void testAll() {
		builder.addVar("s").addPrefix("foaf", "http://xmlns.com/foaf/0.1/")
				.addWhere("?s", RDF.type, "foaf:Person")
				.addOptional("?s", "foaf:name", "?name").addOrderBy("?s");

		String query = builder.buildString();
		/*
		 * PREFIX foaf: <http://xmlns.com/foaf/0.1/>
		 * 
		 * SELECT ?s WHERE { ?s
		 * <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> foaf:Person .
		 * OPTIONAL { ?s foaf:name ?name .} } ORDER BY ?s
		 */
		assertContainsRegex(PREFIX + "foaf:" + SPACE
				+ uri("http://xmlns.com/foaf/0.1/"), query);
		assertContainsRegex(SELECT + var("s"), query);
		assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE
				+ regexRDFtype
				+ SPACE + "foaf:Person" + SPACE + OPTIONAL
				+ OPEN_CURLY + var("s") + SPACE + "foaf:name" + SPACE
				+ var("name") + OPT_SPACE + CLOSE_CURLY
				+ CLOSE_CURLY, query);
		assertContainsRegex(ORDER_BY + var("s"), query);

		builder.setVar("name", "Smith");

		query = builder.buildString();
		assertContainsRegex(PREFIX + "foaf:" + SPACE
				+ uri("http://xmlns.com/foaf/0.1/"), query);
		assertContainsRegex(SELECT + var("s"), query);
		assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE
                + regexRDFtype
				+ SPACE + "foaf:Person" + SPACE + OPTIONAL
				+ OPEN_CURLY + var("s") + SPACE + "foaf:name" + SPACE
				+ quote("Smith") + presentStringType()
				+ OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY, query);
		assertContainsRegex(ORDER_BY + var("s"), query);
	}

	@Test
	public void testPredicateVar() {
		builder.addVar("*").addPrefix("", "http://example/")
				.addWhere(":S", "?p", ":O");
		String query = builder.buildString();

		assertContainsRegex(WHERE + OPEN_CURLY + ":S" + SPACE + var("p")
				+ SPACE + ":O" + OPT_SPACE + CLOSE_CURLY, query);
	}

	@Test
	public void testSubjectVar() {
		builder.addVar("*").addPrefix("", "http://example/")
				.addWhere("?s", ":P", ":O");
		String query = builder.buildString();

		assertContainsRegex(WHERE + OPEN_CURLY + var("s") + SPACE + ":P"
				+ SPACE + ":O" + OPT_SPACE + CLOSE_CURLY, query);
	}

	@Test
	public void testObjectVar() {
		builder.addVar("*").addPrefix("", "http://example/")
				.addWhere(":S", ":P", "?o");
		String query = builder.buildString();

		assertContainsRegex(WHERE + OPEN_CURLY + ":S" + SPACE + ":P" + SPACE
				+ var("o") + OPT_SPACE +  CLOSE_CURLY, query);
	}

	@Test
	public void testNoVars() {
		builder.addWhere("?s", "?p", "?o");
		String query = builder.buildString();

		assertContainsRegex(SELECT + "\\*" + SPACE, query);
	}
}
