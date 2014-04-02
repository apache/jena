/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * An abstract writer for line based quad formats
 * 
 * @author rvesse
 * @param <TKey>
 * 
 */
public abstract class AbstractLineBasedQuadWriter<TKey> extends AbstractLineBasedNodeTupleWriter<TKey, Quad, QuadWritable> {

    /**
     * Creates a new writer using the default NTriples node formatter
     * 
     * @param writer
     *            Writer
     */
    public AbstractLineBasedQuadWriter(Writer writer) {
        this(writer, new NodeFormatterNT());
    }

    /**
     * Creates a new writer using the specified node formatter
     * 
     * @param writer
     *            Writer
     * @param formatter
     *            Node formatter
     */
    public AbstractLineBasedQuadWriter(Writer writer, NodeFormatter formatter) {
        super(writer, formatter);
    }

    @Override
    protected Node[] getNodes(QuadWritable tuple) {
        Quad q = tuple.get();
        if (q.isDefaultGraph()) {
            return new Node[] { q.getSubject(), q.getPredicate(), q.getObject() };
        } else {
            return new Node[] { q.getSubject(), q.getPredicate(), q.getObject(), q.getGraph() };
        }
    }

}
