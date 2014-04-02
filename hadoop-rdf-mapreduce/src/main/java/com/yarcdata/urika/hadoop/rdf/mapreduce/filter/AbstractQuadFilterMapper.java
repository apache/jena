/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract mapper implementation for filtering quads
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public abstract class AbstractQuadFilterMapper<TKey> extends AbstractNodeTupleFilterMapper<TKey, Quad, QuadWritable> {

}
