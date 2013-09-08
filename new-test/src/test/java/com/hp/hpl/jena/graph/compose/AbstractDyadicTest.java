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

import java.util.StringTokenizer;

import org.junit.Test;

import com.hp.hpl.jena.graph.AbstractGraphTest;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

public abstract class AbstractDyadicTest extends AbstractGraphTest {

	static private NiceIterator<String> things(final String x) {
		return new NiceIterator<String>() {
			private StringTokenizer tokens = new StringTokenizer(x);

			@Override
			public boolean hasNext() {
				return tokens.hasMoreTokens();
			}

			@Override
			public String next() {
				return tokens.nextToken();
			}
		};
	}

	@Test
	public void testDyadic() {
		ExtendedIterator<String> it1 = things("now is the time");
		ExtendedIterator<String> it2 = things("now is the time");
		ExtendedIterator<String> mt1 = things("");
		ExtendedIterator<String> mt2 = things("");
		assertEquals("mt1.hasNext()", false, mt1.hasNext());
		assertEquals("mt2.hasNext()", false, mt2.hasNext());
		assertEquals("andThen(mt1,mt2).hasNext()", false, mt1.andThen(mt2)
				.hasNext());
		assertEquals("butNot(it1,it2).hasNext()", false, CompositionBase
				.butNot(it1, it2).hasNext());
		assertEquals("x y z @butNot z", true,
				CompositionBase.butNot(things("x y z"), things("z")).hasNext());
		assertEquals("x y z @butNot a", true,
				CompositionBase.butNot(things("x y z"), things("z")).hasNext());
	}

	@Test
	public void testDyadicOperands() {
		Graph g = getGraphProducer().newGraph(), h = getGraphProducer()
				.newGraph();
		Dyadic d = new Dyadic(g, h) {
			@Override
			protected ExtendedIterator<Triple> _graphBaseFind(TripleMatch m) {
				return null;
			}
		};
		assertSame(g, d.getL());
		assertSame(h, d.getR());
	}

}
