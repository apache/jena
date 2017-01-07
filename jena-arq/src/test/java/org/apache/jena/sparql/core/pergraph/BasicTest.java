package org.apache.jena.sparql.core.pergraph;

import org.apache.jena.sparql.core.AbstractDatasetGraphTests;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;

public class BasicTest extends AbstractDatasetGraphTests {

	@Override
	protected DatasetGraph emptyDataset() {
		return new DatasetGraphPerGraphLocking();
	}

}
