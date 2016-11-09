package projects.dsg2.test;

import org.apache.jena.sparql.core.AbstractDatasetGraphFind ;
import org.apache.jena.sparql.core.DatasetGraph ;
import projects.dsg2.DatasetGraphStorage ;
import projects.dsg2.storage.StorageMem ;

public class TestDatasetGraphStorageFindTests extends AbstractDatasetGraphFind {
    @Override
    protected DatasetGraph create() {
        return new DatasetGraphStorage(new StorageMem()) ;
    }
}
