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

package org.apache.jena.riot.lang;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.system.StreamRDF;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

public class TestCollectorStream  {
	
	private List<Triple> writeTriples(StreamRDF out, int size) {
		List<Triple> results = new ArrayList<>();
		out.start();
        for (int i = 1; i <= size; i++) {
            Triple t = new Triple(NodeFactory.createAnon(),
                    NodeFactory.createURI("http://predicate"), NodeFactoryExtra.intToNode(i));
            out.triple(t);
            results.add(t);
        }
        out.finish();
        return results;
	}
	
	@Test
	public void test_streamed_triples() {
		CollectorStreamTriples out = new CollectorStreamTriples();
		List<Triple> expected = writeTriples(out, 10);
		
		Assert.assertEquals(expected, out.getCollected());
	}
	
	private List<Quad> writeQuads(StreamRDF out, int size) {
		List<Quad> results = new ArrayList<>();
		out.start();
        for (int i = 1; i <= size; i++) {
        	Quad q = new Quad(NodeFactory.createURI("http://graph"),
                    NodeFactory.createAnon(),
                    NodeFactory.createURI("http://predicate"), NodeFactoryExtra.intToNode(i));
            out.quad(q);
            results.add(q);
        }
        out.finish();
        return results;
	}
	
	@Test
	public void test_streamed_quads() {
		CollectorStreamQuads out = new CollectorStreamQuads();
		List<Quad> expected = writeQuads(out, 10);
		
		Assert.assertEquals(expected, out.getCollected());
	}
}
