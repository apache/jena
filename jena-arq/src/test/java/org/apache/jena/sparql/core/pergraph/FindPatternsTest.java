package org.apache.jena.sparql.core.pergraph;

import org.apache.jena.sparql.core.AbstractDatasetGraphFindPatterns;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;

public class FindPatternsTest extends AbstractDatasetGraphFindPatterns {

	@Override
	protected DatasetGraph create() {
		return new DatasetGraphPerGraphLocking();
	}

}
