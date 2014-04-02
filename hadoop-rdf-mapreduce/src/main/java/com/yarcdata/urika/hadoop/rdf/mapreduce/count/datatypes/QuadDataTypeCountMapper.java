/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count.datatypes;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.NodeCountReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.QuadNodeCountMapper;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A mapper for counting data type usages within quads designed primarily for
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
public class QuadDataTypeCountMapper<TKey> extends QuadNodeCountMapper<TKey> {

    private static final NodeWritable[] EMPTY = new NodeWritable[0];

    @Override
    protected NodeWritable[] getNodes(QuadWritable tuple) {
        Node object = tuple.get().getObject();
        if (!object.isLiteral())
            return EMPTY;
        String dtUri = object.getLiteralDatatypeURI();
        if (dtUri == null)
            return EMPTY;
        return new NodeWritable[] { new NodeWritable(NodeFactory.createURI(dtUri)) };
    }
}
