/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count.namespaces;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.mapreduce.TextCountReducer;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A mapper for counting namespace usages within quads designed primarily for
 * use in conjunction with the {@link TextCountReducer}
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public class QuadNamespaceCountMapper<TKey> extends AbstractNodeTupleNamespaceCountMapper<TKey, Quad, QuadWritable> {

    @Override
    protected NodeWritable[] getNodes(QuadWritable tuple) {
        Quad q = tuple.get();
        return new NodeWritable[] { new NodeWritable(q.getGraph()), new NodeWritable(q.getSubject()),
                new NodeWritable(q.getPredicate()), new NodeWritable(q.getObject()) };
    }

}
