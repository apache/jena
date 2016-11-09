package projects.dsg2.test;

import org.apache.jena.sparql.core.AbstractDatasetGraphTests ;
import org.apache.jena.sparql.core.DatasetGraph ;
import projects.dsg2.DatasetGraphStorage ;
import projects.dsg2.storage.StorageMem ;

public class TestDatasetGraphStorageTests extends AbstractDatasetGraphTests {
    @Override
    protected DatasetGraph emptyDataset() {
        return new DatasetGraphStorage(new StorageMem()) ;
    }

}
