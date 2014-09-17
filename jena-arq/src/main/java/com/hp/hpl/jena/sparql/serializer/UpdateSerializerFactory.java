package com.hp.hpl.jena.sparql.serializer;

import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.modify.request.UpdateSerializer;

/**
 * Interface for update serializer factories
 *
 */
public interface UpdateSerializerFactory {

    /**
     * Return true if this factory can create a serializer for the given syntax
     */
    public boolean accept(Syntax syntax);

    /**
     * Return a serializer for the given syntax
     */
    public UpdateSerializer create(Syntax syntax, Prologue prologue, IndentedWriter writer);
}
