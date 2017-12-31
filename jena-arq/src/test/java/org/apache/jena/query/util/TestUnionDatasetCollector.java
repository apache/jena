package org.apache.jena.query.util;

import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.sparql.sse.SSE.parseGraph;

import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

public class TestUnionDatasetCollector extends TestDatasetCollector {

    @Override
    public DatasetCollector testInstance() {
        return DatasetLib.collectors().union();
    }

    @Test
    public void testUnion() {
        final Graph g1 = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        final Model m1 = createModelForGraph(g1);
        final Dataset dsg1 = DatasetFactory.create(m1);
        final String graphName1 = NodeFactory.createBlankNode().toString();
        dsg1.addNamedModel(graphName1, m1);
        final Graph g2 = parseGraph("(graph (triple <s2> <p2> <o2> ))");
        final Dataset dsg2 = DatasetFactory.create(createModelForGraph(g2));
        final Model m2 = createModelForGraph(g2);
        final String graphName2 = NodeFactory.createBlankNode().toString();
        dsg2.addNamedModel(graphName2, m2);
        Dataset dataset = Stream.<Dataset>builder().add(dsg1).add(dsg2).build().collect(testInstance());

        assertEquals(2, Iter.count(dataset.listNames()));
        assertTrue(m1.isIsomorphicWith(dataset.getNamedModel(graphName1)));
        assertTrue(m2.isIsomorphicWith(dataset.getNamedModel(graphName2)));
        m1.listStatements().mapWith(dataset.getDefaultModel()::contains).forEachRemaining(Assert::assertTrue);
        m2.listStatements().mapWith(dataset.getDefaultModel()::contains).forEachRemaining(Assert::assertTrue);
    }
    
}
