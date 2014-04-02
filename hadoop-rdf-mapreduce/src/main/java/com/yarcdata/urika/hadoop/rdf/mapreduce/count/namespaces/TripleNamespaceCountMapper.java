/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count.namespaces;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.mapreduce.TextCountReducer;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A mapper for counting namespace usages within triples designed primarily for
 * use in conjunction with the {@link TextCountReducer}
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public class TripleNamespaceCountMapper<TKey> extends AbstractNodeTupleNamespaceCountMapper<TKey, Triple, TripleWritable> {

    @Override
    protected NodeWritable[] getNodes(TripleWritable tuple) {
        Triple t = tuple.get();
        return new NodeWritable[] { new NodeWritable(t.getSubject()), new NodeWritable(t.getPredicate()),
                new NodeWritable(t.getObject()) };
    }

}
