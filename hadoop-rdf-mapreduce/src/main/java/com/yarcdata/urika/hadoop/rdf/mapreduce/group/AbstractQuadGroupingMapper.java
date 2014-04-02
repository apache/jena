/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract mapper implementation which helps in grouping quads by assigning
 * them a {@link NodeWritable} key in place of their existing key. Derived
 * implementations of this may select the key based on some component of the
 * quad or by other custom logic.
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public abstract class AbstractQuadGroupingMapper<TKey> extends AbstractNodeTupleGroupingMapper<TKey, Quad, QuadWritable> {

    protected final NodeWritable selectKey(QuadWritable tuple) {
        return this.selectKey(tuple.get());
    }

    /**
     * Selects the key to use
     * 
     * @param quad
     *            Quad
     * @return Key to use
     */
    protected abstract NodeWritable selectKey(Quad quad);
}
