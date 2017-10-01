package org.apache.jena.query.util;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.IdentityFinishCollector;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

public interface DatasetCollector<Input> extends IdentityFinishCollector<Input, Dataset> {

    @Override
    default Supplier<Dataset> supplier() {
        return DatasetFactory::createGeneral;
    }

    @Override
    default BinaryOperator<Dataset> combiner() {
        return DatasetLib::union;
    }
}
