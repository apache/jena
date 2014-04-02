/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A mapper which splits triples into their constituent nodes
 * 
 * @author rvesse
 * 
 * @param <TKey> Key type
 */
public class TripleSplitWithNodesMapper<TKey> extends AbstractNodeTupleSplitWithNodesMapper<TKey, Triple, TripleWritable> {

    @Override
    protected NodeWritable[] split(TripleWritable tuple) {
        Triple t = tuple.get();
        return new NodeWritable[] { new NodeWritable(t.getSubject()), new NodeWritable(t.getPredicate()),
                new NodeWritable(t.getObject()) };
    }
}
