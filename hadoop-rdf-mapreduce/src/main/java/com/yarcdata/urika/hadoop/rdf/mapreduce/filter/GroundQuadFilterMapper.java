/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A quad filter which accepts only ground quads i.e. those with no blank nodes
 * or variables
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class GroundQuadFilterMapper<TKey> extends AbstractQuadFilterMapper<TKey> {

    @Override
    protected boolean accepts(Object key, QuadWritable tuple) {
        Quad q = tuple.get();
        if (!q.isConcrete())
            return false;
        // Ground if all nodes are URI/Literal
        return (q.getGraph().isURI() || q.getGraph().isLiteral()) && (q.getSubject().isURI() || q.getSubject().isLiteral())
                && (q.getPredicate().isURI() || q.getPredicate().isLiteral())
                && (q.getObject().isURI() || q.getObject().isLiteral());
    }

}
