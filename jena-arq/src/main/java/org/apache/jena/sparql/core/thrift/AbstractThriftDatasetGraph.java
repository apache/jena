package org.apache.jena.sparql.core.thrift;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;

public class AbstractThriftDatasetGraph extends DatasetGraphWrapper {

	public static final int DEFAULT_BYTE_BUFFER_CAPACITY = 1024;
	
	public static final int HAS_NEXT = 2;
	
	public static final int BUFFER_OVERFLOW = 4;
	
	public static final int TRANSACTION_READ = 2;
	
	public static final int TRANSACTION_WRITE = 4;

	public AbstractThriftDatasetGraph(final DatasetGraph datasetGraph) {
		super(datasetGraph);
	}

}
