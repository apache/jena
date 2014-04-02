/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * A mapper which assists in grouping triples by subject by reassigning their
 * keys to be their subjects
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class TripleGroupBySubjectMapper<TKey> extends AbstractTripleGroupingMapper<TKey> {

    @Override
    protected NodeWritable selectKey(Triple triple) {
        return new NodeWritable(triple.getSubject());
    }

}
