/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.transform;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A mapper which transforms quads into triples
 * <p>
 * Keys are left as is by this mapper.
 * </p>
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadsToTriplesMapper<TKey> extends Mapper<TKey, QuadWritable, TKey, TripleWritable> {

    @Override
    protected void map(TKey key, QuadWritable value, Context context) throws IOException, InterruptedException {
        context.write(key, new TripleWritable(value.get().asTriple()));
    }

}
