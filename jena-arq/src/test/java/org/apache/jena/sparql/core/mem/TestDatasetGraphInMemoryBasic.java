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

package org.apache.jena.sparql.core.mem;

import static java.lang.System.err ;
import static org.apache.jena.atlas.iterator.Iter.anyMatch ;
import static org.apache.jena.atlas.iterator.Iter.iter ;
import static org.apache.jena.graph.Node.ANY ;
import static org.apache.jena.graph.NodeFactory.createBlankNode ;
import static org.apache.jena.graph.NodeFactory.createURI ;
import static org.apache.jena.sparql.core.Quad.unionGraph ;
import static org.apache.jena.sparql.graph.GraphFactory.createGraphMem ;
import static org.apache.jena.sparql.sse.SSE.parseNode ;
import static org.apache.jena.sparql.sse.SSE.parseQuad ;
import static org.apache.jena.sparql.sse.SSE.parseTriple ;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertTrue ;

import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.query.DatasetFactory ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.sparql.core.AbstractDatasetGraphTests ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.junit.Test ;

public class TestDatasetGraphInMemoryBasic extends AbstractDatasetGraphTests {

	@Test
	public void orderingOfNodesFromFindIsCorrect() {
		final DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;

        final Node p = parseNode(":p") ;
        final Triple triple = parseTriple("(:s :p :o)");
		dsg.getDefaultGraph().add(triple);
        final Iterator<Triple> iter = dsg.getDefaultGraph().find(null, p, null) ;
        assertTrue(anyMatch(iter, triple::equals));


        final Node p1 = parseNode(":p1") ;
        final Quad quad = parseQuad("(:g1 :s1 :p1 :o1)");
		dsg.add(quad) ;

        final Iterator<Quad> iter2 = dsg.find(null, null, p1, null) ;

        assertTrue(anyMatch(iter2, quad::equals));
        Iter.print(err,iter2);
	}

	@Test
	public void prefixesAreManaged() {
		final Node graphName = createURI("http://example/g");
		final DatasetGraph dsg = emptyDataset();
		dsg.addGraph(graphName, createGraphMem());
		final Dataset dataset = DatasetFactory.wrap(dsg);
		Model model = dataset.getNamedModel(graphName.getURI());
		final String testPrefix = "example";
		final String testURI = "http://example/";
		model.setNsPrefix(testPrefix, testURI);
		assertEquals(testURI, model.getNsPrefixURI(testPrefix));
		model.close();
		model = dataset.getNamedModel(graphName.getURI());
		final String nsURI = dataset.getNamedModel(graphName.getURI()).getNsPrefixURI(testPrefix);
		assertNotNull(nsURI);
		assertEquals(testURI, nsURI);
	}

	@Test
	public void unionGraphWorksProperly() {
	    DatasetGraph dsg = emptyDataset();
		// quads from named graphs should appear in union
		Quad q = Quad.create(createBlankNode(), createBlankNode(), createBlankNode(), createBlankNode());
		dsg.add(q);
		// Expected in the union graph
		Quad q2 = Quad.create(unionGraph, q.asTriple());
		assertTrue(iter(dsg.find(unionGraph, ANY, ANY, ANY)).some(q2::equals));
		// no triples from default graph should appear in union
		Triple t = Triple.create(createBlankNode(), createBlankNode(), createBlankNode());
		dsg.getDefaultGraph().add(t);
		assertFalse(iter(dsg.find(unionGraph, ANY, ANY, ANY)).some(Quad::isDefaultGraph));
	}

    @Test
    public void listGraphNodesHasNoPhantomEmptyGraphs() {
        final DatasetGraph dsg = emptyDataset();
        final Node g = createURI("http://example/g");
        final Node s = createURI("http://example/s");
        final Node p = createURI("http://example/p");
        final Node o = createURI("http://example/o");
        dsg.add(g, s, p, o);
        Iterator<Node> graphNodes = dsg.listGraphNodes();
        assertTrue("Missing named graph!", graphNodes.hasNext());
        assertEquals("Wrong graph name!", g, graphNodes.next());
        assertFalse("Too many named graphs!", graphNodes.hasNext());
        dsg.delete(g, s, p, o);
        graphNodes = dsg.listGraphNodes();
        assertFalse("Too many named graphs!", graphNodes.hasNext());
    }

	@Override
	protected DatasetGraph emptyDataset() {
		return DatasetGraphFactory.createTxnMem();
	}
}
