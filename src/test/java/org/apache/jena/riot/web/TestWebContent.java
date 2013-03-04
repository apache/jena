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

package org.apache.jena.riot.web;

import org.junit.Assert;

import org.apache.jena.riot.WebContent ;
import org.junit.Test;

public class TestWebContent {

	@Test
	public void testCanonicaliseMimeTypes1()
	{
		testCanonicalise(WebContent.contentTypeTurtle, WebContent.contentTypeTurtle);
		testCanonicalise(WebContent.contentTypeTurtleAlt1, WebContent.contentTypeTurtle);
		testCanonicalise(WebContent.contentTypeTurtleAlt2, WebContent.contentTypeTurtle);
	}
	
	@Test
	public void testCanonicaliseMimeTypes2()
	{
		testCanonicalise(WebContent.contentTypeN3, WebContent.contentTypeN3);
		testCanonicalise(WebContent.contentTypeN3Alt1, WebContent.contentTypeN3);
		testCanonicalise(WebContent.contentTypeN3Alt2, WebContent.contentTypeN3);
	}
	
	@Test
	public void testCanonicaliseMimeTypes3()
	{
		testCanonicalise(WebContent.contentTypeNTriples, WebContent.contentTypeNTriples);
		testCanonicalise(WebContent.contentTypeNTriplesAlt, WebContent.contentTypeNTriples);
	}
	
	@Test
	public void testCanonicaliseMimeTypes4()
	{
		testCanonicalise(WebContent.contentTypeNQuads, WebContent.contentTypeNQuads);
		testCanonicalise(WebContent.contentTypeNQuadsAlt1, WebContent.contentTypeNQuads);
		testCanonicalise(WebContent.contentTypeNQuadsAlt2, WebContent.contentTypeNQuads);
	}
	
	@Test
	public void testCanonicaliseMimeTypes5()
	{
		testCanonicalise(WebContent.contentTypeTriG, WebContent.contentTypeTriG);
		testCanonicalise(WebContent.contentTypeTriGAlt1, WebContent.contentTypeTriG);
		testCanonicalise(WebContent.contentTypeTriGAlt2, WebContent.contentTypeTriG);
	}
	
	private void testCanonicalise(String input, String expected)
	{
		String canonical = WebContent.contentTypeCanonical(input);
		Assert.assertEquals(expected, canonical);		
	}
	
}

