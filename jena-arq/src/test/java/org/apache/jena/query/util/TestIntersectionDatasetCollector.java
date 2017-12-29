package org.apache.jena.query.util;

public class TestIntersectionDatasetCollector extends TestDatasetCollector{

    @Override
    public DatasetCollector testInstance() {
        return DatasetLib.collectors().intersect();
    }

    
    
}
