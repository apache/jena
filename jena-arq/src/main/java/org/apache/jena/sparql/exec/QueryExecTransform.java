package org.apache.jena.sparql.exec;

import java.util.function.Function;

/** A function that returns a new (or the same) QueryExec for a given one. */
@FunctionalInterface
public interface QueryExecTransform
    extends Function<QueryExec, QueryExec>
{
}
