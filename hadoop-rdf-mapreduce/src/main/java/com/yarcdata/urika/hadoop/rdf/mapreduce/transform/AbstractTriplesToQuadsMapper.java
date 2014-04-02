/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.transform;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * An abstract mapper which transforms triples into quads. Derived
 * implementations may choose how the graph to which triples are assigned is
 * decided.
 * <p>
 * Keys are left as is by this mapper.
 * </p>
 * 
 * @author rvesse
 * 
 * @param <TKey>
 */
public abstract class AbstractTriplesToQuadsMapper<TKey> extends Mapper<TKey, TripleWritable, TKey, QuadWritable> {

    @Override
    protected final void map(TKey key, TripleWritable value, Context context) throws IOException, InterruptedException {
        Triple triple = value.get();
        Node graphNode = this.selectGraph(triple);
        context.write(key, new QuadWritable(new Quad(graphNode, triple)));
    }

    /**
     * Selects the graph name to use for converting the given triple into a quad
     * 
     * @param triple
     *            Triple
     * @return Tuple
     */
    protected abstract Node selectGraph(Triple triple);
}
