/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract mapper implementation which helps in grouping triples by assigning
 * them a {@link NodeWritable} key in place of their existing key. Derived
 * implementations of this may select the key based on some component of the
 * triple or by other custom logic.
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public abstract class AbstractTripleGroupingMapper<TKey> extends AbstractNodeTupleGroupingMapper<TKey, Triple, TripleWritable> {

    @Override
    protected final NodeWritable selectKey(TripleWritable tuple) {
        return this.selectKey(tuple.get());
    }
    
    protected abstract NodeWritable selectKey(Triple triple);
}
