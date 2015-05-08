package org.apache.jena.atlas.lib.cache;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.rangeClosed;
import static org.junit.Assert.assertEquals;

import org.apache.jena.atlas.lib.Cache;
import org.junit.Test;

/**
 * Simple test to ensure that {@link CacheSimple} evidences the fixed-size
 * behavior we desire.
 * 
 * @author ajs6f
 * @date 8 May 2015
 *
 */
public class CacheSimpleTest {

	@Test
	public void testFixedSize() {
		final int maxSize = 5;
		final int submittedEntries = 10;
		final Cache<Integer, Object> testCache = new CacheSimple<>(maxSize);
		rangeClosed(1, submittedEntries).boxed().collect(toMap(k -> k, v -> v))
				.forEach(testCache::put);
		assertEquals("Test cache failed to maintain fixed size!", maxSize, testCache.size());
	}
}
