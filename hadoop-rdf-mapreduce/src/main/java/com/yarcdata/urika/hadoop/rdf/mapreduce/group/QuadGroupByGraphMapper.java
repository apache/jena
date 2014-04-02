/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * A mapper which assists in grouping quads by graph by reassigning their keys
 * to be their graphs
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public class QuadGroupByGraphMapper<TKey> extends AbstractQuadGroupingMapper<TKey> {

    @Override
    protected NodeWritable selectKey(Quad quad) {
        return new NodeWritable(quad.getGraph());
    }
}
