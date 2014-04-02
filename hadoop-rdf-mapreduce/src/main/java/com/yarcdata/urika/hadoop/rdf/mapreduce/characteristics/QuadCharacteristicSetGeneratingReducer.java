/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A reducer which converts quads grouped by some node into characteristic sets
 * 
 * @author rvesse
 * 
 */
public class QuadCharacteristicSetGeneratingReducer extends AbstractCharacteristicSetGeneratingReducer<Quad, QuadWritable> {

    @Override
    protected NodeWritable getPredicate(QuadWritable tuple) {
        return new NodeWritable(tuple.get().getPredicate());
    }

}
