package org.apache.jena.sparql.util;

import org.apache.jena.sparql.core.DatasetGraph;
import org.junit.Test;

public class TestIntersectionDatasetGraph extends TestViewDatasetGraph<IntersectionDatasetGraph> {

    @Override
    public IntersectionDatasetGraph testInstance(DatasetGraph left, DatasetGraph right, Context c) {
        return new IntersectionDatasetGraph(right, right, c);
    }

    @Test
    public void testIntersection() {
        
    }
}
