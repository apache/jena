package org.apache.jena.query.util;

import static org.apache.jena.sparql.util.Context.emptyContext;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.DifferenceDatasetGraph;
import org.apache.jena.sparql.util.IntersectionDatasetGraph;
import org.apache.jena.sparql.util.UnionDatasetGraph;

public class DatasetLib {

    public static Dataset union(final Dataset d1, final Dataset d2, Context c) {
        return DatasetFactory.wrap(new UnionDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
    }

    public static Dataset union(final Dataset d1, final Dataset d2) {
        return union(d1, d2, emptyContext);
    }

    public static Dataset intersection(final Dataset d1, final Dataset d2, Context c) {
        return DatasetFactory.wrap(new IntersectionDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
    }

    public static Dataset intersection(final Dataset d1, final Dataset d2) {
        return intersection(d1, d2, emptyContext);
    }

    public static Dataset difference(final Dataset d1, final Dataset d2, Context c) {
        return DatasetFactory.wrap(new DifferenceDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), c));
    }
    
    public static Dataset difference(final Dataset d1, final Dataset d2) {
        return DatasetFactory.wrap(new DifferenceDatasetGraph(d1.asDatasetGraph(), d2.asDatasetGraph(), emptyContext));
    }
}
