/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A quad filter mapper which accepts only valid quads, by which we mean they
 * meet the following criteria:
 * <ul>
 * <li>Graph is a URI or Blank Node</li>
 * <li>Subject is a URI or Blank Node</li>
 * <li>Predicate is a URI</li>
 * <li>Object is a URI, Blank Node or Literal</li>
 * </ul>
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public final class ValidQuadFilterMapper<TKey> extends AbstractQuadFilterMapper<TKey> {

    @Override
    protected final boolean accepts(TKey key, QuadWritable tuple) {
        Quad q = tuple.get();
        return (q.getGraph().isURI() || q.getGraph().isBlank()) && (q.getSubject().isURI() || q.getSubject().isBlank())
                && q.getPredicate().isURI() && (q.getObject().isURI() || q.getObject().isBlank() || q.getObject().isLiteral());
    }

}
