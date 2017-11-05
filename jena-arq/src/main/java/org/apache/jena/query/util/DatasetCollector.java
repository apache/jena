package org.apache.jena.query.util;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.IdentityFinishCollector;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.util.Context;

public interface DatasetCollector<T> extends IdentityFinishCollector<T, Dataset> {

    @Override
    default Supplier<Dataset> supplier() {
        return DatasetFactory::createGeneral;
    }

    @Override
    default BinaryOperator<Dataset> combiner() {
        return DatasetCollector::union;
    }

    static Dataset union(final Dataset d1, final Dataset d2) {
        return DatasetLib.union(d1, d2, Context.emptyContext);
    }
}
