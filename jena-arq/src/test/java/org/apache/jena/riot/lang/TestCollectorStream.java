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
