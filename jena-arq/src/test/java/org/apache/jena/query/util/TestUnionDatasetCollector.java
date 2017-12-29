package org.apache.jena.query.util;

import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.sparql.sse.SSE.parseGraph;

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

public class TestUnionDatasetCollector extends TestDatasetCollector {

    @Override
    public DatasetCollector testInstance() {
        return DatasetLib.collectors().union();
    }

    @Test
    public void collectingOneDatasetGivesOneDataset() {
        Graph graph = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        Model model = createModelForGraph(graph);
        Dataset dataset = DatasetFactory.create(model);
        Dataset collection = Stream.<Dataset>builder().add(dataset).build().collect(testInstance());
        assertTrue(dataset.getDefaultModel().isIsomorphicWith(collection.getDefaultModel()));
        dataset.listNames().forEachRemaining(graphName -> assertTrue(
                dataset.getNamedModel(graphName).isIsomorphicWith(collection.getNamedModel(graphName))));
    }

}
