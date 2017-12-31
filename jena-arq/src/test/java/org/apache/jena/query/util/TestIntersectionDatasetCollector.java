package org.apache.jena.query.util;

import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.sparql.sse.SSE.parseGraph;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

public class TestIntersectionDatasetCollector extends TestDatasetCollector {

    @Override
    public DatasetCollector testInstance() {
        return DatasetLib.collectors().intersect();
    }

    @Test
    public void testIntersection() {
        Graph graph = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        Model model = createModelForGraph(graph);
        Dataset dataset = DatasetFactory.create(model);
    }

}
