/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A triple filter which accepts only ground triples i.e. those with no blank
 * nodes or variables
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class GroundTripleFilterMapper<TKey> extends AbstractTripleFilterMapper<TKey> {

    @Override
    protected boolean accepts(Object key, TripleWritable tuple) {
        Triple t = tuple.get();
        if (!t.isConcrete())
            return false;
        // Ground if all nodes are URI/Literal
        return (t.getSubject().isURI() || t.getSubject().isLiteral())
                && (t.getPredicate().isURI() || t.getPredicate().isLiteral())
                && (t.getObject().isURI() || t.getObject().isLiteral());
    }

}
