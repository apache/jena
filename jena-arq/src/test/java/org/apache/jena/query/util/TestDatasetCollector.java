package org.apache.jena.query.util;

import java.util.stream.Stream;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
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
}
