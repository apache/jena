/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A mapper which splits quads into their constituent nodes using the quad as
 * the key and the nodes as the values
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadSplitWithNodesMapper<TKey> extends AbstractNodeTupleSplitWithNodesMapper<TKey, Quad, QuadWritable> {

    @Override
    protected NodeWritable[] split(QuadWritable tuple) {
        Quad q = tuple.get();
        return new NodeWritable[] { new NodeWritable(q.getGraph()), new NodeWritable(q.getSubject()),
                new NodeWritable(q.getPredicate()), new NodeWritable(q.getObject()) };
    }
}
