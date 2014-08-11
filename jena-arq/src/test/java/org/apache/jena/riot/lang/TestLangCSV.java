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

import java.io.StringReader;

import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.IRIResolver;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestLangCSV extends BaseTest {
	private static final String FILE_NAME = "src/test/resources/test.csv";
	private static final String FILE_URI = IRIResolver.resolveString(FILE_NAME);

	@Test
	public void testPredicateWithSpace() {
		String[] s1 = { "Predicate With Space", "PredicateWithSpace" };
		String[] s2 = {
				//"<"+ LangCSV.caculateSubject(1, FILE_NAME) + "> <" + FILE_URI + "#Predicate+With+Space> 'PredicateWithSpace' ; ",
				" [] <" + FILE_URI + "#Predicate%20With%20Space> 'PredicateWithSpace' ; ",
				" <http://w3c/future-csv-vocab/row> 1 ." };
		assertIsomorphicWith(s1, s2);
	}
	
	@Test
	public void testNonURICharacters() {
		String[] s1 = { "`~!@#$%^&*()-_=+[{]}|\\;:'\"<.>/?", "NonURICharacters" };
		String[] s2 = {
				//"<"+ LangCSV.caculateSubject(1, FILE_NAME) + "> <" + FILE_URI + "#%60%7E%21%40%23%24%25%5E%26*%28%29-_%3D%2B%5B%7B%5D%7D%7C%5C%3B%3A%27%22%3C.%3E%2F%3F> 'NonURICharacters' ; ",
				" [] <" + FILE_URI + "#%60~%21%40%23%24%25%5E%26%2A%28%29-_%3D%2B%5B%7B%5D%7D%7C%5C%3B%3A%27%22%3C.%3E%2F%3F> 'NonURICharacters' ; ",
				" <http://w3c/future-csv-vocab/row> 1 ." };
		assertIsomorphicWith(s1, s2);
	}
	
	@Test
	public void testDigitalLocalName() {
		String[] s1 = { "1234", "DigitalLocalName" };
		String[] s2 = {
				//"<"+ LangCSV.caculateSubject(1, FILE_NAME) + "> <" + FILE_URI + "#1234> 'DigitalLocalName' ; ",
				" [] <" + FILE_URI + "#1234> 'DigitalLocalName' ; ",
				" <http://w3c/future-csv-vocab/row> 1 ." };
		assertIsomorphicWith(s1, s2);
	}

	@Test
	public void testMoney() {
		String[] s1 = { "£", "£" };
		String[] s2 = {
				//"<"+ LangCSV.caculateSubject(1, FILE_NAME) + "> <" + FILE_URI + "#1234> 'DigitalLocalName' ; ",
				" [] <" + FILE_URI + "#%A3> '£' ; ",
				" <http://w3c/future-csv-vocab/row> 1 ." };
		assertIsomorphicWith(s1, s2);
	}
	
	@Test
	public void RDFDataMgrReadTest() {
		Model m1 = RDFDataMgr.loadModel(FILE_NAME, RDFLanguages.CSV);
		Model m2 = ModelFactory.createDefaultModel();
		m2.read(FILE_NAME, "CSV");
		assertEquals(12, m1.size());
		assertTrue(m1.isIsomorphicWith(m2));
	}

	private Model parseToModel(String[] strings, Lang lang) {
		String string = StrUtils.strjoin("\n", strings);
		StringReader r = new StringReader(string);
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, r, FILE_NAME, lang);
		return model;
	}
	
	private void assertIsomorphicWith(String[] s1, String[] s2){
		Model m1 = parseToModel(s1, RDFLanguages.CSV);
		Model m2 = parseToModel(s2, RDFLanguages.TURTLE);
		assertTrue(m1.isIsomorphicWith(m2));
	}

}
