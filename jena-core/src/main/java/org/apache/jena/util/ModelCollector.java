package org.apache.jena.util;

import java.util.function.BinaryOperator;
import java.util.function.Supplier;

import org.apache.jena.atlas.lib.IdentityFinishCollector;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public interface ModelCollector<Input> extends IdentityFinishCollector<Input, Model> {

    @Override
    default Supplier<Model> supplier() {
        return ModelFactory::createDefaultModel;
    }

    @Override
    default BinaryOperator<Model> combiner() {
        return ModelFactory::createUnion;
    }
}
