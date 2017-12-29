package org.apache.jena.sparql.util;

import static org.apache.jena.sparql.sse.SSE.parseGraph;

import org.apache.jena.graph.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Test;

public class TestIntersectionDatasetGraph extends TestViewDatasetGraph {

    @Override
    public IntersectionDatasetGraph testInstance(DatasetGraph left, DatasetGraph right, Context c) {
        return new IntersectionDatasetGraph(left, right, c);
    }

    @Test
    public void testIntersection() {
        final Graph g1 = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        final DatasetGraph dsg1 = DatasetGraphFactory.create(g1);
        final Node graphName1 = NodeFactory.createBlankNode();
        dsg1.addGraph(graphName1, g1);
        final Graph g2 = parseGraph("(graph (triple <s2> <p2> <o2> ))");
        final DatasetGraph dsg2 = DatasetGraphFactory.create(g2);
        final Node graphName2 = NodeFactory.createBlankNode();
        dsg2.addGraph(graphName2, g2);
        final Node graphName3 = NodeFactory.createBlankNode();
        dsg1.addGraph(graphName3, g1);
        dsg2.addGraph(graphName3, g1);
        DatasetGraph dsg = testInstance(dsg1, dsg2, Context.emptyContext);
        assertEquals(1, dsg.size());
        assertTrue(dsg.getDefaultGraph().isEmpty());
        assertTrue(dsg.getGraph(graphName1).isEmpty());
        assertTrue(dsg.getGraph(graphName2).isEmpty());
        assertTrue(g1.isIsomorphicWith(dsg.getGraph(graphName3)));
    }
}
