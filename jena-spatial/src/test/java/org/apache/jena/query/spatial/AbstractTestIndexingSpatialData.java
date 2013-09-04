package org.apache.jena.query.spatial;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.junit.Test;

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
		doTestSearch(turtle, queryString, expectedURIs);
	}

}
