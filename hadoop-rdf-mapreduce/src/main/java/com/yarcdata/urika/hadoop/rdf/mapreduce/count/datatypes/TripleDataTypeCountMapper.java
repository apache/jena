/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count.datatypes;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.NodeCountReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.TripleNodeCountMapper;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A mapper for counting data type usages within triples designed primarily for
 * use in conjunction with {@link NodeCountReducer}
 * <p>
 * This mapper extracts the data types for typed literal objects and converts
 * them into nodes so they can be counted
 * </p>
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class TripleDataTypeCountMapper<TKey> extends TripleNodeCountMapper<TKey> {

    private static final NodeWritable[] EMPTY = new NodeWritable[0];

    @Override
    protected NodeWritable[] getNodes(TripleWritable tuple) {
        Node object = tuple.get().getObject();
        if (!object.isLiteral())
            return EMPTY;
        String dtUri = object.getLiteralDatatypeURI();
        if (dtUri == null)
            return EMPTY;
        return new NodeWritable[] { new NodeWritable(NodeFactory.createURI(dtUri)) };
    }
}
