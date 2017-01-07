package org.apache.jena.sparql.core.pergraph;

import static org.apache.jena.query.DatasetFactory.wrap;

import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemoryLock;

public class LockTest extends TestDatasetGraphInMemoryLock {

	@Override
	protected Dataset createDataset() {
		return wrap(new DatasetGraphPerGraphLocking());
	}

}
