package org.apache.jena.query.spatial;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.logging.Log;
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
			Log.warn(this, "JTS lib is not ready in classpath! An exception should be thrown later on!");
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
			if (!jts_context_ready){
				fail("An exception is supposed to be thrown for reading WKT shapes without JTS!");
			}
		}catch (InvalidShapeException e){
			if (jts_context_ready){
				fail("The exception is not supposed to be thrown: "+ e.getMessage());
			}
		}
	}

}
