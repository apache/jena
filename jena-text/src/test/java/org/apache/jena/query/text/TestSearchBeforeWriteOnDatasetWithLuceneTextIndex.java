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

package org.apache.jena.query.text;

import java.util.HashSet;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSearchBeforeWriteOnDatasetWithLuceneTextIndex extends AbstractTestDatasetWithLuceneTextIndex {
	
	@BeforeClass public static void beforeClass() {
		init();
	}	
	
	@AfterClass public static void afterClass() {
		deleteOldFiles();
	}
	
	@Test public void testNoResultsOnFirstCreateIndex(){
		String turtle = "";
		String queryString = StrUtils.strjoinNL(
				QUERY_PROLOG,
				"SELECT ?s",
				"WHERE {",
				"    ?s text:query ( rdfs:label \"a* OR b* OR c* OR d* OR e* OR f* OR g* OR h* OR i* OR j* OR k* OR l* OR m* OR n* OR o* OR p* OR q* OR r* OR s* OR t* OR u* OR v* OR w* OR x* OR y* OR z*\" 10 ) .",
				"}"
				);
		doTestSearch(turtle, queryString, new HashSet<String>());
	}
}
