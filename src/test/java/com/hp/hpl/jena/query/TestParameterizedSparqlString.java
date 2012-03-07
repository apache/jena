/**
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

package com.hp.hpl.jena.query;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.XSD;

import junit.framework.Assert;

public class TestParameterizedSparqlString {
	
	private void test(ParameterizedSparqlString query, String[] expected, String[] notExpected)
	{
		System.out.println("Raw Command:");
		System.out.println(query.getCommandText());
		String command = query.toString();
		System.out.println("Injected Command:");
		System.out.println(command);
		for (String x : expected)
		{
			Assert.assertTrue(command.contains(x));
		}
		for (String x : notExpected)
		{
			Assert.assertFalse(command.contains(x));
		}
	}
	
	private Query testAsQuery(ParameterizedSparqlString query)
	{
		return query.asQuery();
	}
	
	private UpdateRequest testAsUpdate(ParameterizedSparqlString update)
	{
		return update.asUpdate();
	}
	
	@Test
	public void test_param_string_constructor_1()
	{
		//Test empty constructor
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		Assert.assertEquals("", query.getCommandText());
	}
	
	@Test
	public void test_param_string_constructor_2()
	{
		//Test constructor with null command - null command should map to empty command automagically
		ParameterizedSparqlString query = new ParameterizedSparqlString((String)null);
		Assert.assertEquals("", query.getCommandText());
	}
	
	@Test
	public void test_param_string_constructor_3()
	{
		//Test constructor with base URI
		ParameterizedSparqlString query = new ParameterizedSparqlString("", "http://example.org");
		Assert.assertEquals("http://example.org", query.getBaseUri());
	}
	
	@Test
	public void test_param_string_constructor_4()
	{
		//Test constructor with predefined parameters
		QuerySolutionMap map = new QuerySolutionMap();
		Resource r = ResourceFactory.createResource("http://example.org");
		map.add("s", r);
		ParameterizedSparqlString query = new ParameterizedSparqlString("", map);
		
		Assert.assertEquals(r.asNode(), query.getParam("s"));
	}
	
	@Test
	public void test_param_string_constructor_5()
	{
		//Test constructor with predefined parameters - variant of constructor that does not require command text
		QuerySolutionMap map = new QuerySolutionMap();
		Resource r = ResourceFactory.createResource("http://example.org");
		map.add("s", r);
		ParameterizedSparqlString query = new ParameterizedSparqlString(map);
		
		Assert.assertEquals(r.asNode(), query.getParam("s"));
	}
	
	@Test
	public void test_param_string_constructor_6()
	{
		//Test constructor with predefined parameters
		QuerySolutionMap map = new QuerySolutionMap();
		Resource r = ResourceFactory.createResource("http://example.org");
		map.add("s", r);
		Literal l = ResourceFactory.createPlainLiteral("example");
		map.add("o", l);
		ParameterizedSparqlString query = new ParameterizedSparqlString("", map);
		
		Assert.assertEquals(r.asNode(), query.getParam("s"));
		Assert.assertEquals(l.asNode(), query.getParam("o"));
	}
	
	@Test
	public void test_param_string_constructor_7()
	{
		//Test constructor with predefined parameters - variant of constructor that does not require command text
		QuerySolutionMap map = new QuerySolutionMap();
		Resource r = ResourceFactory.createResource("http://example.org");
		map.add("s", r);
		Literal l = ResourceFactory.createPlainLiteral("example");
		map.add("o", l);
		ParameterizedSparqlString query = new ParameterizedSparqlString(map);
		
		Assert.assertEquals(r.asNode(), query.getParam("s"));
		Assert.assertEquals(l.asNode(), query.getParam("o"));
	}
	
	@Test
	public void test_param_string_constructor_8()
	{
		//Test constructors with predefined prefixes
		PrefixMappingImpl prefixes = new PrefixMappingImpl();
		prefixes.setNsPrefix("ex", "http://example.org");
		ParameterizedSparqlString query = new ParameterizedSparqlString("", prefixes);
		
		Assert.assertEquals(prefixes.getNsPrefixURI("ex"), query.getNsPrefixURI("ex"));
	}
	
	@Test
	public void test_param_string_constructor_9()
	{
		//Test constructors with predefined prefixes - variant of constructor that does not require command text
		PrefixMappingImpl prefixes = new PrefixMappingImpl();
		prefixes.setNsPrefix("ex", "http://example.org");
		ParameterizedSparqlString query = new ParameterizedSparqlString(prefixes);
		
		Assert.assertEquals(prefixes.getNsPrefixURI("ex"), query.getNsPrefixURI("ex"));
	}
	
	@Test
	public void test_param_string_iri_1()
	{
		//Test simple injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		
		test(query, new String[] { "<http://example.org>" }, new String[] { "?s" });
	}
	
	@Test
	public void test_param_string_iri_2()
	{
		//Test simple injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("p", "http://example.org");
		
		test(query, new String[] { "<http://example.org>" }, new String[] { "?p" });
	}
	
	@Test
	public void test_param_string_iri_3()
	{
		//Test simple injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("o", "http://example.org");
		
		test(query, new String[] { "<http://example.org>" }, new String[] { "?o" });
	}
	
	@Test
	public void test_param_string_iri_4()
	{
		//Test simple injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o . ?s a ?type }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		
		test(query, new String[] { "<http://example.org>" }, new String[] { "?s" });
	}
	
	@Test
	public void test_param_string_iri_5()
	{
		//Test simple injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		query.setIri("p", "http://predicate");
		
		test(query, new String[] { "<http://example.org>", "<http://predicate>" }, new String[] { "?s", "?p" });
	}
	
	@Test
	public void test_param_string_mixed_1()
	{
		//Test simple injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o . }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		query.setIri("p", "http://predicate");
		query.setLiteral("o", true);
		
		test(query, new String[] { "<http://example.org>", "<http://predicate>", "true" }, new String[] { "?s", "?p", "?o" });
	}
	
	@Test
	public void test_param_string_boolean_1()
	{
		//Test boolean injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", true);
		
		//We don't expect #boolean as booleans should be formatted as plain literals
		test(query, new String[] { "true" }, new String[] { "?o", XSD.xboolean.toString() });
	}
	
	@Test
	public void test_param_string_boolean_2()
	{
		//Test boolean injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", false);
		
		//We don't expect #boolean as booleans should be formatted as plain literals
		test(query, new String[] { "false" }, new String[] { "?o", XSD.xboolean.toString() });
	}
	
	@Test
	public void test_param_string_boolean_3()
	{
		//Test invalid boolean injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xboolean.toString()));
		
		//We expect #boolean as booleans with invalid lexical values should not be formatted as plain literals
		test(query, new String[] { "xyz", XSD.xboolean.toString() }, new String[] { "?o"});
	}
	
	@Test
	public void test_param_string_int_1()
	{
		//Test integer injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", 1234);
		
		//We don't expect #integer as integers should be formatted as typed literals
		test(query, new String[] { "1234" }, new String[] { "?o", XSD.integer.toString() });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_int_2()
	{
		//Test long integer injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", 3000000000l);
		
		//We don't expect #integer as integers should be formatted as typed literals
		test(query, new String[] { "3000000000" }, new String[] { "?o", XSD.integer.toString()});
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_int_3()
	{
		//Test invalid integer injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.integer.toString()));
		
		//We don't expect #integer as integers should be formatted as typed literals
		test(query, new String[] { "xyz", XSD.integer.toString() }, new String[] { "?o" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_double_1()
	{
		//Test double injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", 123.4);
		
		//We expect #double as doubles without exponents cannot be formatted as plain literals
		test(query, new String[] { "123.4", XSD.xdouble.toString() }, new String[] { "?o" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_double_2()
	{
		//Test double injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", 123.0e5);
		
		//We don't expect #double as we expected doubles to be formatted as plain literals
		test(query, new String[] { "1.23E7" }, new String[] { "?o", XSD.xdouble.toString() });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_double_3()
	{
		//Test invalid double injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xdouble.toString()));
		
		//We expect #double as invalid doubles cannot be formatted as plain literals
		test(query, new String[] { "xyz", XSD.xdouble.toString() }, new String[] { "?o" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_float_1()
	{
		//Test float injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", 123.4f);
		
		//We expect #float as floats should be formatted as typed literals
		test(query, new String[] { "123.4", XSD.xfloat.toString() }, new String[] { "?o" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_date_1()
	{
		//Test date injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		Calendar dt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dt.set(2012, 1, 24, 12, 0, 0);
		query.setLiteral("o", dt);
		
		//We expect #dateTime as dateTime should be formatted as typed literals
		test(query, new String[] { "2012-02-24T12:00:00", XSD.dateTime.toString() }, new String[] { "?o" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_date_2()
	{
		//Test invalid date injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", "xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.dateTime.toString()));
		
		//We expect #dateTime as dateTime should be formatted as typed literals
		test(query, new String[] { "xyz", XSD.dateTime.toString() }, new String[] { "?o" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_lang_1()
	{
		//Test lang literal injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", "hello", "en");
		
		test(query, new String[] { "hello", "@en" }, new String[] { "?o" });
		testAsQuery(query);
	}
		
	@Test
	public void test_param_string_lang_2()
	{
		//Test lang literal injection
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setLiteral("o", "bonjour", "fr");
		
		test(query, new String[] { "bonjour", "@fr" }, new String[] { "?o" });
		testAsQuery(query);
	}
	
	@Test(expected=QueryException.class)
	public void test_param_string_bad_1()
	{
		//Test bad input - not a valid query
		String cmdText = "Not a query";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		
		testAsQuery(query);
	}
	
	@Test(expected=QueryException.class)
	public void test_param_string_simple_bad_1()
	{
		//Test bad input - injecting the parameter makes the query invalid
		String cmdText = "SELECT ?s WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		
		test(query, new String[] { "<http://example.org>" }, new String[] { "?s" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_precedence_1()
	{
		//Test simple injection precedence
		//Setting parameter multiple times just overrides the existing setting
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		query.setIri("s", "http://alternate.org");
		
		test(query, new String[] { "<http://alternate.org>" }, new String[] { "?s", "<http://example.org>" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_name_collision_1()
	{
		//Test name collision
		//The parameter we inject has a name which is a prefix of another variable name, only the
		//actual name should be injected to
		String cmdText = "SELECT * WHERE { ?a ?ab ?abc }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("a", "http://example.org");
		
		//In the not expected list we need the whitespace after ?a as otherwise the test will give a 
		//false negative since obviously we should still have ?ab and ?abc present
		test(query, new String[] { "<http://example.org>", "?ab", "?abc" }, new String[] { "?a " });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_name_collision_2()
	{
		//Test name collision
		//The parameter we inject has a name which is a prefix of another variable name, only the
		//actual name should be injected to
		String cmdText = "SELECT * WHERE { ?abc ?ab ?a. }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("a", "http://example.org");
		
		//In the not expected list we need the whitespace after ?a as otherwise the test will give a 
		//false negative since obviously we should still have ?ab and ?abc present
		test(query, new String[] { "<http://example.org>", "?ab", "?abc" }, new String[] { "?a " });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_clear_1()
	{
		//Test clearing of parameter
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		query.clearParam("s");
		
		test(query, new String[] { "?s" }, new String[] { "<http://example.org>" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_clear_2()
	{
		//Test clearing of parameter
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		query.clearParams();
		
		test(query, new String[] { "?s" }, new String[] { "<http://example.org>" });
		testAsQuery(query);
	}

	@Test
	public void test_param_string_clear_3()
	{
		//Test indirect clearing of parameter by setting param to null
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setIri("s", "http://example.org");
		query.setParam("s", (Node)null);
		
		test(query, new String[] { "?s" }, new String[] { "<http://example.org>" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_prefixes_1()
	{
		//Test prefixes are prepended
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setNsPrefix("ex", "http://example.org");
		
		test(query, new String[] { "PREFIX", "ex:", "<http://example.org>" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_prefixes_2()
	{
		//Test prefixes are prepended
		String cmdText = "SELECT * WHERE { ?s ex:predicate ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setNsPrefix("ex", "http://example.org");
		
		test(query, new String[] { "PREFIX", "ex:", "<http://example.org>", "ex:predicate" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test(expected=QueryException.class)
	public void test_param_string_prefixes_bad_1()
	{
		//Test bad input - using a prefix without defining prefix
		String cmdText = "SELECT * WHERE { ?s ex:predicate ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		
		//Testing with an PName using an undefined prefix in the string
		//Should fail on parsing
		test(query, new String[] { "ex:predicate" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_base_1()
	{
		//Test base is prepended
		String cmdText = "SELECT * WHERE { ?s <#predicate> ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		query.setBaseUri("http://example.org");
		
		test(query, new String[] { "BASE", "<http://example.org>" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_base_bad_1()
	{
		//Test questionable input - using relative URI without defining base
		//ARQ accepts this, not sure if this is a way to disable this as this test should
		//ideally be expecting a QueryException
		String cmdText = "SELECT * WHERE { ?s <#predicate> ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		
		test(query, new String[] { }, new String[] { "BASE", "<http://example.org>" });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_copy_1()
	{
		//Test copying - copying always copies command text
		String cmdText = "SELECT * WHERE { ?s ?p ?o }";
		ParameterizedSparqlString query = new ParameterizedSparqlString(cmdText);
		ParameterizedSparqlString copy = query.copy();
		
		Assert.assertEquals(cmdText, copy.getCommandText());
	}
	
	@Test
	public void test_param_string_copy_2()
	{
		//Test copying - copying and changing a parameter changes only one instance
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.setIri("x", "http://example.org/original");
		ParameterizedSparqlString copy = query.copy();
		copy.setIri("x", "http://example.org/copy");
		
		Assert.assertEquals("http://example.org/original", query.getParam("x").toString());
		Assert.assertFalse("http://example.org/copy".equals(query.getParam("x").toString()));
		
		Assert.assertEquals("http://example.org/copy", copy.getParam("x").toString());
		Assert.assertFalse("http://example.org/original".equals(copy.getParam("x").toString()));
	}
	
	@Test
	public void test_param_string_copy_3()
	{
		//Test copying - copying should copy prefixes
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.setNsPrefix("ex", "http://example.org");
		ParameterizedSparqlString copy = query.copy();
		
		Assert.assertEquals("http://example.org", copy.getNsPrefixURI("ex"));
	}
	
	@Test
	public void test_param_string_copy_4()
	{
		//Test copying - copying should copy base URI
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.setBaseUri("http://example.org");
		ParameterizedSparqlString copy = query.copy();
		
		Assert.assertEquals("http://example.org", copy.getBaseUri());
	}
	
	@Test
	public void test_param_string_copy_5()
	{
		//Test selective copying - copying without copying parameters
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.setIri("x", "http://example.org/original");
		ParameterizedSparqlString copy = query.copy(false);
		
		Assert.assertEquals("http://example.org/original", query.getParam("x").toString());
		Assert.assertEquals(null, copy.getParam("x"));
	}
	
	@Test
	public void test_param_string_copy_6()
	{
		//Test selective copying - copying without copying prefixes
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.setNsPrefix("ex", "http://example.org");
		ParameterizedSparqlString copy = query.copy(true, true, false);
		
		Assert.assertFalse("http://example.org".equals(copy.getNsPrefixURI("ex")));
	}
	
	@Test
	public void test_param_string_append_1()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ?o }");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "?o" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_2()
	{
		//Test appending simple types
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.append(true);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "true" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_3()
	{
		//Test appending simple types
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.append(123);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_4()
	{
		//Test appending simple types
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.append(123l);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_5()
	{
		//Test appending simple types
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.append(123.0e5);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "1.23E7" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_iri_1()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ");
		query.appendIri("http://example.org");
		query.append(" ?o }");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "<http://example.org>", "?o" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_iri_2()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ");
		query.appendIri(IRIFactory.iriImplementation().construct("http://example.org"));
		query.append(" ?o }");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "<http://example.org>", "?o" }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_boolean_1()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral(true);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "true" }, new String[] { XSD.xboolean.toString() });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_boolean_2()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral("xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xboolean.toString()));
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "xyz", XSD.xboolean.toString() }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_integer_1()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral(123);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] { XSD.integer.toString() });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_integer_2()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral("xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.integer.toString()));
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "xyz", XSD.integer.toString() }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_integer_3()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral(123l);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "123" }, new String[] { XSD.integer.toString() });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_double_1()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral(123.0e5);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "1.23E7" }, new String[] { XSD.xdouble.toString() });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_double_2()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral(1.23d);
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "1.23", XSD.xdouble.toString() }, new String[] { });
		testAsQuery(query);
	}
	
	@Test
	public void test_param_string_append_double_3()
	{
		//Test appending text
		ParameterizedSparqlString query = new ParameterizedSparqlString();
		query.append("SELECT *");
		query.append('\n');
		query.append("WHERE { ?s ?p ");
		query.appendLiteral("xyz", TypeMapper.getInstance().getSafeTypeByName(XSD.xdouble.toString()));
		query.append("}");
		
		test(query, new String[] { "SELECT", "*", "\n", "WHERE", "?s", "?p", "xyz", XSD.xdouble.toString() }, new String[] { });
		testAsQuery(query);
	}
	
}

