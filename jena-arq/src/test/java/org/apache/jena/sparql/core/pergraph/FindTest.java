package org.apache.jena.sparql.core.pergraph;

import org.apache.jena.sparql.core.AbstractDatasetGraphFind;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;

public class FindTest extends AbstractDatasetGraphFind {

	@Override
	protected DatasetGraph create() {
		return new DatasetGraphPerGraphLocking();
	}

}
