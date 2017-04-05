package org.apache.jena.sparql.core.mosaic;

import org.apache.jena.sparql.core.DatasetGraph;

public class Tessera {

	protected final DatasetGraph datasetGraph;
	
	protected final DatasetGraphTessera datasetGraphTry;

	public Tessera(final DatasetGraph datasetGraph) {
		super();
		this.datasetGraph = datasetGraph;
		datasetGraphTry = DatasetGraphTessera.wrap(datasetGraph);
	}
	
	public DatasetGraph getDatasetGraph() {
		return datasetGraph;
	}
	
	public DatasetGraphTessera getDatasetGraphTry() {
		return datasetGraphTry;
	}
}
