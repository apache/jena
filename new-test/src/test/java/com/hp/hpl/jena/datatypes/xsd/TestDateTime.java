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

package com.hp.hpl.jena.datatypes.xsd;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.AbstractDateTime;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import static org.junit.Assert.*;

/**
 * Tests behaviour of the AbstractDateTime support, specifically for comparison
 * operations. This complements the main tests in TestTypedLiterals.
 */
public class TestDateTime {

	private final AbstractDateTime[] time = {
			makeDateTime("2009-08-13T17:54:40.348Z"),
			makeDateTime("2009-08-13T18:54:39Z"),
			makeDateTime("2009-08-13T18:54:40Z"),
			makeDateTime("2009-08-13T18:54:40.077Z"),
			makeDateTime("2009-08-13T18:54:40.348Z"),
			makeDateTime("2009-08-13T18:54:40.505Z"),
			makeDateTime("2009-08-13T18:54:40.77Z"),
			makeDateTime("2009-08-13T18:54:40.780Z"),
			makeDateTime("2009-08-13T18:54:40.88Z"),
			makeDateTime("2009-08-13T18:54:40.989Z"),
			makeDateTime("2009-08-13T19:54:40.989Z")

	};

	private AbstractDateTime makeDateTime(String time) {
		return (XSDDateTime) XSDDatatype.XSDdateTime.parse(time);
	}

	@Test
	public void testXSDOrder() {
		for (int i = 0; i < time.length; i++) {
			for (int j = 0; j < i; j++) {
				assertEquals(String.format("%s > %s", time[i], time[j]),
						time[i].compare(time[j]), AbstractDateTime.GREATER_THAN);
			}

			assertEquals(String.format("%s == %s", time[i], time[i]),
					time[i].compare(time[i]), AbstractDateTime.EQUAL);

			for (int j = i + 1; j < time.length; j++) {
				assertEquals(String.format("%s < %s", time[i], time[j]),
						time[i].compare(time[j]), AbstractDateTime.LESS_THAN);
			}
		}
	}

	@Test
	public void testJavaOrder() {
		for (int i = 0; i < time.length; i++) {
			for (int j = 0; j < i; j++) {
				assertEquals(String.format("%s > %s", time[i], time[j]),
						time[i].compareTo(time[j]),
						AbstractDateTime.GREATER_THAN);
			}

			assertEquals(String.format("%s == %s", time[i], time[i]),
					time[i].compareTo(time[i]), AbstractDateTime.EQUAL);

			for (int j = i + 1; j < time.length; j++) {
				assertEquals(String.format("%s < %s", time[i], time[j]),
						time[i].compareTo(time[j]), AbstractDateTime.LESS_THAN);
			}
		}
	}

	// TODO move this to model test
	@Test
	public void testRoundTripping1() {
		Model m = ModelFactory.createDefaultModel();
		Property startTime = m
				.createProperty("http://jena.hpl.hp.com/test#startTime");

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		Literal xsdlit0 = m.createTypedLiteral(cal);

		Resource event = m.createResource();
		event.addProperty(startTime, xsdlit0);

		StringWriter sw = new StringWriter();
		m.write(sw);
		StringReader reader = new StringReader(sw.toString());
		Model m1 = ModelFactory.createDefaultModel();
		m1.read(reader, null);

		assertTrue(m.isIsomorphicWith(m1));

		Literal xsdlit1 = m1.listStatements().next().getObject()
				.as(Literal.class);
		assertEquals(xsdlit0, xsdlit1);
	}

	// Test that the string and calendar versions are the same.
	@Test
	public void testRoundTripping2() {
		// String lex = "2013-04-16T15:40:07.3Z" ;
		testCalendarRT(1366126807300L);
	}

	@Test
	public void testRoundTripping3() {
		// String lex = "2013-04-16T15:40:07.31Z" ;
		testCalendarRT(1366126807310L);
	}

	@Test
	public void testRoundTripping4() {
		// String lex = "2013-04-16T15:40:07.301Z" ;
		testCalendarRT(1366126807301L);
	}

	private void testCalendarRT(long value) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(value);
		Literal lit1 = ResourceFactory.createTypedLiteral(cal);
		Literal lit2 = ResourceFactory.createTypedLiteral(
				lit1.getLexicalForm(), lit1.getDatatype());

		assertEquals("equals: ", lit1, lit2);
		assertEquals("hash code: ", lit1.hashCode(), lit2.hashCode());
	}

}
