package org.apache.jena.query.util;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.DifferenceDatasetGraph;
import org.apache.jena.sparql.util.UnionDatasetGraph;

public class DatasetLib {

	public static Dataset union(final Dataset d1, final Dataset d2) {
		return DatasetFactory.wrap(new UnionDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph()));
	}

	public static Dataset union(final Dataset d1, final Dataset d2, Context c) {
		return DatasetFactory.wrap(new UnionDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
	}

	public static Dataset intersection(final Dataset d1, final Dataset d2) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public static Dataset difference(final Dataset d1, final Dataset d2) {
		return DatasetFactory.wrap(new DifferenceDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph()));
	}

	public static Dataset difference(final Dataset d1, final Dataset d2, Context c) {
		return DatasetFactory.wrap(new DifferenceDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
	}
}
