package org.apache.jena.sparql.core.pergraph;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphPerGraphLocking;
import org.apache.jena.sparql.core.TestDatasetGraphViewGraphs;

public class ViewTest extends TestDatasetGraphViewGraphs {

	@Override
	protected DatasetGraph createBaseDSG() {
		return new DatasetGraphPerGraphLocking();
	}
}
