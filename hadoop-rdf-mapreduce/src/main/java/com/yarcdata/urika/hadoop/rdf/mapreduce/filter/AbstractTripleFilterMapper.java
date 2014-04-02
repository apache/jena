/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract mapper implementation for filtering triples
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public abstract class AbstractTripleFilterMapper<TKey> extends AbstractNodeTupleFilterMapper<TKey, Triple, TripleWritable> {

}
