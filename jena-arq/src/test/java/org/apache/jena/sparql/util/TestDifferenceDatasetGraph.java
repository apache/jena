package org.apache.jena.sparql.util;

import static org.apache.jena.sparql.sse.SSE.parseGraph;

import org.apache.jena.graph.*;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Test;

public class TestDifferenceDatasetGraph extends TestViewDatasetGraph {

    @Override
    public DifferenceDatasetGraph testInstance(DatasetGraph left, DatasetGraph right, Context c) {
        return new DifferenceDatasetGraph(left, right, c);
    }
    
    @Test
    public void testDifference() {
        final Graph g1 = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        final DatasetGraph dsg1 = DatasetGraphFactory.create(g1);
        final Node graphName1 = NodeFactory.createBlankNode();
        dsg1.addGraph(graphName1, g1);
        final Graph g2 = parseGraph("(graph (triple <s2> <p2> <o2> ))");
        final DatasetGraph dsg2 = DatasetGraphFactory.create(g2);
        final Node graphName2 = NodeFactory.createBlankNode();
        dsg2.addGraph(graphName2, g2);
        DatasetGraph dsg = testInstance(dsg1, dsg2, Context.emptyContext);

        assertEquals(1, dsg.size());
        assertTrue(g1.isIsomorphicWith(dsg.getGraph(graphName1)));
        assertTrue(g1.isIsomorphicWith(dsg.getDefaultGraph()));
        assertTrue(dsg.getGraph(graphName2).isEmpty());
    }

}
