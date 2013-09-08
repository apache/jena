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

package com.hp.hpl.jena.graph.impl;

import static com.hp.hpl.jena.testing_framework.GraphTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

public class LiteralLabelsTest {
	private void assertEq(LiteralLabel ll1, LiteralLabel ll2) {
		assertEquivalent(ll1, ll2);
		assertTrue(ll1.sameValueAs(ll2));
	}

	@Test
	public void testBlankLanguageEquality() {
		assertEq(LiteralLabelFactory.create("test", "", null),
				LiteralLabelFactory.create("test", "", null));
	}

	@Test
	public void testBase64BinaryEquality() {
		assertEq(node("'0123'http://www.w3.org/2001/XMLSchema#base64Binary")
				.getLiteral(),
				node("'0123'http://www.w3.org/2001/XMLSchema#base64Binary")
						.getLiteral());
	}

	@Test
	public void testIllegalBase64BinaryEquality() {
		assertEq(node("'illgeal'http://www.w3.org/2001/XMLSchema#base64Binary")
				.getLiteral(),
				node("'illgeal'http://www.w3.org/2001/XMLSchema#base64Binary")
						.getLiteral());
	}

	@Test
	public void testHexBinaryEquality() {
		assertEq(node("'0123'http://www.w3.org/2001/XMLSchema#hexBinary")
				.getLiteral(),
				node("'0123'http://www.w3.org/2001/XMLSchema#hexBinary")
						.getLiteral());
	}

	@Test
	public void testIllegalHexBinaryEquality() {
		assertEq(node("'illegal'http://www.w3.org/2001/XMLSchema#hexBinary")
				.getLiteral(),
				node("'illegal'http://www.w3.org/2001/XMLSchema#hexBinary")
						.getLiteral());
	}

	@Test
	public void testDatatypeIsEqualsNotCalledIfSecondOperandIsNotTyped() {
		RDFDatatype d = new BaseDatatype("eh:/FakeDataType") {
			@Override
			public boolean isEqual(LiteralLabel ll1, LiteralLabel ll2) {
				fail("RDFDatatype::isEquals should not be called if B has no datatype");
				return false;
			}
		};
		LiteralLabel ll1 = LiteralLabelFactory.create("17", "", d);
		LiteralLabel ll2 = LiteralLabelFactory.create("17", "", null);
		assertFalse(ll1.sameValueAs(ll2));
	}

	@Test
	public void testStringEquality() {
		assertEq(LiteralLabelFactory.create("xyz"),
				LiteralLabelFactory.create("xyz"));
	}

	@Test
	public void testDifferentCaseStringInequality() {
		LiteralLabel ll1 = LiteralLabelFactory.create("xyz");
		LiteralLabel ll2 = LiteralLabelFactory.create("XYZ");

		assertFalse(ll1.equals(ll2));
		assertFalse(ll1.sameValueAs(ll2));
	}

	@Test
	public void testDifferentLanguageStringInequality() {
		LiteralLabel ll1 = LiteralLabelFactory.create("xyz", "en-us");
		LiteralLabel ll2 = LiteralLabelFactory.create("xyz", "en-uk");
		assertFalse(ll1.equals(ll2));
		assertFalse(ll1.sameValueAs(ll2));
	}

	@Test
	public void testDifferentLanguageStringCaseInequality() {
		LiteralLabel ll1 = LiteralLabelFactory.create("xyz", "en-UK");
		LiteralLabel ll2 = LiteralLabelFactory.create("xyz", "en-uk");
		assertFalse(ll1.equals(ll2));
		assertTrue(ll1.sameValueAs(ll2));
	}

}
