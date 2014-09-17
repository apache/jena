package com.hp.hpl.jena.sparql.serializer;

import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;

/**
 * Interface for query serializer factories
 *
 */
public interface QuerySerializerFactory {

    /**
     * Return true if this factory can create a serializer for the given syntax
     */
    public boolean accept(Syntax syntax);

    /**
     * Return a serializer for the given syntax
     */
    public QueryVisitor create(Syntax syntax, Prologue prologue, IndentedWriter writer);
}
