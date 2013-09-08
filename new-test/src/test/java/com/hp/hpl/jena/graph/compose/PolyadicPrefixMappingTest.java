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

package com.hp.hpl.jena.graph.compose;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.shared.AbstractPrefixMappingTest;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

public class PolyadicPrefixMappingTest extends AbstractPrefixMappingTest {
	private Polyadic poly;
	private Graph gBase;
	private PrefixMapping gBaseMapping;
	private Graph g1;
	private PrefixMapping g1Mapping;
	private Graph g2;
	private PrefixMapping g2Mapping;
	private PolyadicPrefixMappingImpl prefixMapping;

	@Before
	public void setup() {
		gBase = mock(Graph.class);
		gBaseMapping = mock(PrefixMapping.class);
		when(gBase.getPrefixMapping()).thenReturn(gBaseMapping);
		g1 = Mockito.mock(Graph.class);
		g1Mapping = mock(PrefixMapping.class);
		when(g1.getPrefixMapping()).thenReturn(g1Mapping);
		g2 = Mockito.mock(Graph.class);
		g2Mapping = mock(PrefixMapping.class);
		when(g2.getPrefixMapping()).thenReturn(g2Mapping);
		poly = mock(Polyadic.class);
		when(poly.getBaseGraph()).thenReturn(gBase);
		when(poly.getSubGraphs()).thenReturn(
				Arrays.asList(new Graph[] { g1, g2 }));
		prefixMapping = new PolyadicPrefixMappingImpl(poly);
		when(poly.getPrefixMapping()).thenReturn(prefixMapping);

	}

	// PolyadicPrefixMappingImpl
	@Override
	protected PrefixMapping getMapping() {
		PrefixMappingImpl pfx = new PrefixMappingImpl();
		Graph gBase = mock(Graph.class);
		when(gBase.getPrefixMapping()).thenReturn(new PrefixMappingImpl());
		Graph g1 = Mockito.mock(Graph.class);
		when(g1.getPrefixMapping()).thenReturn(new PrefixMappingImpl());
		Graph g2 = Mockito.mock(Graph.class);
		when(g2.getPrefixMapping()).thenReturn(new PrefixMappingImpl());
		poly = mock(Polyadic.class);
		when(poly.getBaseGraph()).thenReturn(gBase);
		when(poly.getSubGraphs()).thenReturn(
				Arrays.asList(new Graph[] { g1, g2 }));
		return new PolyadicPrefixMappingImpl(poly);
	}

	protected static final String alpha = "something:alpha#";
	protected static final String beta = "something:beta#";

	/*
	 * tests for polyadic prefix mappings (a) base mapping is the mutable one
	 * (b) base mapping over-rides all others (c) non-overridden mappings in
	 * other maps are visible
	 */
	@Test
	public void testOnlyBaseMutated() {
		prefixMapping.setNsPrefix("a", alpha);
		verify(g1Mapping, times(0)).setNsPrefix(anyString(), anyString());
		verify(g2Mapping, times(0)).setNsPrefix(anyString(), anyString());
		verify(gBaseMapping, times(1)).setNsPrefix(anyString(), anyString());
		verify(gBaseMapping, times(1)).setNsPrefix("a", alpha);
	}

	@Test
	public void testUpdatesVisible() {
		when(g1Mapping.getNsPrefixURI("a")).thenReturn(alpha);
		when(g2Mapping.getNsPrefixURI("b")).thenReturn(beta);
		assertEquals(alpha, prefixMapping.getNsPrefixURI("a"));
		assertEquals(beta, prefixMapping.getNsPrefixURI("b"));
		verify(gBaseMapping, times(1)).getNsPrefixURI("a");
		verify(gBaseMapping, times(1)).getNsPrefixURI("b");
	}

	@Test
	public void testUpdatesOverridden() {
		when(g1Mapping.getNsPrefixURI("x")).thenReturn(alpha);
		when(gBaseMapping.getNsPrefixURI("x")).thenReturn(beta);
		assertEquals(beta, poly.getPrefixMapping().getNsPrefixURI("x"));
	}

	@Test
	public void testQNameComponents() {
		when(g1Mapping.qnameFor(alpha + "hoop")).thenReturn("x:hoop");
		when(g2Mapping.qnameFor(beta + "lens")).thenReturn("y:lens");

		assertEquals("x:hoop", poly.getPrefixMapping().qnameFor(alpha + "hoop"));
		assertEquals("y:lens", poly.getPrefixMapping().qnameFor(beta + "lens"));
	}

	/**
	 * Test that the default namespace of a sub-graph doesn't appear as a
	 * default namespace of the polyadic graph.
	 */
	@Test
	public void testSubgraphsDontPolluteDefaultPrefix() {
		String imported = "http://imported#", local = "http://local#";

		Map<String, String> g1Map = new HashMap<String, String>();
		g1Map.put("", imported);

		Map<String, String> g2Map = new HashMap<String, String>();
		g2Map.put("", local);

		when(g1Mapping.getNsPrefixMap()).thenReturn(g1Map);
		when(gBaseMapping.getNsPrefixMap()).thenReturn(g2Map);

		assertEquals(null, poly.getPrefixMapping().getNsURIPrefix(imported));
	}

	@Test
	public void testPolyDoesntSeeImportedDefaultPrefix() {
		String imported = "http://imported#";
		Map<String, String> g1Map = new HashMap<String, String>();
		g1Map.put("", imported);
		when(g1Mapping.getNsPrefixMap()).thenReturn(g1Map);

		assertEquals(null, poly.getPrefixMapping().getNsPrefixURI(""));
	}

	@Test
	public void testPolyMapOverridesFromTheLeft() {
		Map<String, String> g1Map = new HashMap<String, String>();
		g1Map.put("a", "eh:/U1");

		Map<String, String> g2Map = new HashMap<String, String>();
		g2Map.put("a", "eh:/U2");

		when(g1Mapping.getNsPrefixMap()).thenReturn(g1Map);
		when(g2Mapping.getNsPrefixMap()).thenReturn(g2Map);

		String a = poly.getPrefixMapping().getNsPrefixMap().get("a");
		assertEquals("eh:/U1", a);
	}

	@Test
	public void testPolyMapHandlesBase() {
		Map<String, String> g1Map = new HashMap<String, String>();
		g1Map.put("", "eh:/U1");

		Map<String, String> g2Map = new HashMap<String, String>();
		g2Map.put("", "eh:/U2");

		when(g1Mapping.getNsPrefixMap()).thenReturn(g1Map);
		when(g2Mapping.getNsPrefixMap()).thenReturn(g2Map);

		String a = poly.getPrefixMapping().getNsPrefixMap().get("");
		assertEquals(poly.getPrefixMapping().getNsPrefixURI(""), a);
	}

}
