package org.apache.jena.sparql.core;

public class TestDatasetGraphMap extends AbstractDatasetGraphTests {

    @Override
    protected DatasetGraph emptyDataset() {
        return new DatasetGraphMap();
    }
}
