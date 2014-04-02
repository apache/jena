/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A triple filter mapper which accepts only valid triples, by which we mean they
 * meet the following criteria:
 * <ul>
 * <li>Subject is a URI or Blank Node</li>
 * <li>Predicate is a URI</li>
 * <li>Object is a URI, Blank Node or Literal</li>
 * </ul>
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public final class ValidTripleFilterMapper<TKey> extends AbstractTripleFilterMapper<TKey> {

    @Override
    protected final boolean accepts(TKey key, TripleWritable tuple) {
        Triple t = tuple.get();
        return (t.getSubject().isURI() || t.getSubject().isBlank()) && t.getPredicate().isURI()
                && (t.getObject().isURI() || t.getObject().isBlank() || t.getObject().isLiteral());
    }

}
