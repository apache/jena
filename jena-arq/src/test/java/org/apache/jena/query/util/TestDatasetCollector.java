package org.apache.jena.query.util;

import static org.apache.jena.rdf.model.ModelFactory.createModelForGraph;
import static org.apache.jena.sparql.sse.SSE.parseGraph;

import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

public abstract class TestDatasetCollector extends Assert {

    public abstract DatasetCollector testInstance();

    @Test
    public void collectionOfEmptyStreamShouldBeEmpty() {
        final Dataset collected = Stream.<Dataset>empty().collect(testInstance());
        assertTrue(collected.isEmpty());
    }

    @Test
    public void collectionOfStreamOfEmptyDatasetsShouldBeEmpty() {
        Stream<Dataset> stream = Stream.<Dataset>builder()
                .add(DatasetFactory.create())
                .add(DatasetFactory.create())
                .add(DatasetFactory.create()).build();
        final Dataset collected = stream.collect(testInstance());
        assertTrue(collected.isEmpty());
    }

    @Test(expected=NullPointerException.class)
    public void noNullDatasetsAllowed() {
        Stream.<Dataset>builder().add(null).build().collect(testInstance());
    }
    
    @Test
    public void collectingOneDatasetGivesThatDataset() {
        Graph graph = parseGraph("(graph (triple <s1> <p1> <o1> ))");
        Model model = createModelForGraph(graph);
        Dataset dataset = DatasetFactory.create(model);
        Node graphName = NodeFactory.createBlankNode();
        dataset.addNamedModel(graphName.toString(), model);
        Dataset collection = Stream.<Dataset>builder().add(dataset).build().collect(testInstance());
        assertDatasetsAreIsomorphicPerGraph(dataset, collection);
    }

    protected static void assertDatasetsAreIsomorphicPerGraph(Dataset dataset1, Dataset dataset2) {
        assertGraphsAreIsomorphic(dataset1.getDefaultModel(), dataset2.getDefaultModel());
        dataset1.listNames().forEachRemaining(graphName ->
            assertGraphsAreIsomorphic(dataset1.getNamedModel(graphName), dataset2.getNamedModel(graphName)));
    }
    
    protected static void assertGraphsAreIsomorphic(Model graph1, Model graph2) {
        assertTrue(graph1.isIsomorphicWith(graph2));
    }
}
