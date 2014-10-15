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

package org.apache.jena.query.spatial;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Test;
import static org.junit.Assert.fail;

import com.spatial4j.core.exception.InvalidShapeException;

public class AbstractTestIndexingSpatialData extends
		AbstractTestDatasetWithSpatialIndex {

	@Test
	public void testIndexingStringLiteral() {
		final String turtle = StrUtils.strjoinNL(TURTLE_PROLOG, "<"
				+ RESOURCE_BASE + "testIndexingStringLiteral>",
				"   geo:lat '51.3827' ;", "   geo:long '-2.71909' ", ".");
		String queryString = StrUtils
				.strjoinNL(
						QUERY_PROLOG,
						"SELECT ?s",
						"WHERE {",
						" ?s spatial:nearby (51.3000 -2.71000 100.0 'miles' -1) .",
						"}");
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs
				.addAll(Arrays
						.asList((new String[] { "http://example.org/data/resource/testIndexingStringLiteral" })));
		doTestSearch(turtle, queryString, expectedURIs);
	}

	@Test
	public void testIndexingWKTLiteral() {
		boolean jts_context_ready = false;
		try {
			SpatialIndex index = (SpatialIndex) dataset.getContext().get(
					SpatialQuery.spatialIndex);
			index.getDocDef().setSpatialContextFactory(
					SpatialQuery.JTS_SPATIAL_CONTEXT_FACTORY_CLASS);
			jts_context_ready = true;
		}catch (NoClassDefFoundError e){
			//Log.warn(this, "JTS lib is not on the classpath!");
		}
		
		final String turtle = StrUtils
				.strjoinNL(
						TURTLE_PROLOG,
						"<" + RESOURCE_BASE + "testIndexingWKTLiteral>",
						"   wkt:asWKT 'POINT(-1.74803 52.4539)'^^wkt:wktLiteral  ",
						".");
		String queryString = StrUtils
				.strjoinNL(
						QUERY_PROLOG,
						"SELECT ?s",
						"WHERE {",
						" ?s spatial:nearby (51.3000 -2.71000 100.0 'miles' -1) .",
						"}");
		Set<String> expectedURIs = (new HashSet<String>());
		expectedURIs
				.addAll(Arrays
						.asList((new String[] { "http://example.org/data/resource/testIndexingWKTLiteral" })));
		
		try {
			doTestSearch(turtle, queryString, expectedURIs);
		}catch (InvalidShapeException e){
			if (jts_context_ready){
				fail("The exception is not supposed to be thrown: "+ e.getMessage());
			}
		}
	}

}
