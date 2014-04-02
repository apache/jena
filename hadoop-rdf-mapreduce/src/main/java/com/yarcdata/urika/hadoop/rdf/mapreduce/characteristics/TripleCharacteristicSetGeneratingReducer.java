/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A reducer which converts triples grouped by some node into characteristic
 * sets
 * 
 * @author rvesse
 * 
 */
public class TripleCharacteristicSetGeneratingReducer extends AbstractCharacteristicSetGeneratingReducer<Triple, TripleWritable> {

    @Override
    protected NodeWritable getPredicate(TripleWritable tuple) {
        return new NodeWritable(tuple.get().getPredicate());
    }

}
