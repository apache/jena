/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A mapper for counting node usages within quads designed primarily for use in
 * conjunction with {@link NodeCountReducer}
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadNodeCountMapper<TKey> extends AbstractNodeTupleNodeCountMapper<TKey, Quad, QuadWritable> {

    @Override
    protected NodeWritable[] getNodes(QuadWritable tuple) {
        Quad q = tuple.get();
        return new NodeWritable[] { new NodeWritable(q.getGraph()), new NodeWritable(q.getSubject()),
                new NodeWritable(q.getPredicate()), new NodeWritable(q.getObject()) };
    }

}
