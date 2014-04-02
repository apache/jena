/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterNT;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * An abstract writer for line based triple formats
 * 
 * @author rvesse
 * @param <TKey> 
 * 
 */
public abstract class AbstractLineBasedTripleWriter<TKey> extends AbstractLineBasedNodeTupleWriter<TKey, Triple, TripleWritable> {

    /**
     * Creates a new writer using the default NTriples node formatter
     * 
     * @param writer
     *            Writer
     */
    public AbstractLineBasedTripleWriter(Writer writer) {
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
    public AbstractLineBasedTripleWriter(Writer writer, NodeFormatter formatter) {
        super(writer, formatter);
    }

    @Override
    protected Node[] getNodes(TripleWritable tuple) {
        Triple t = tuple.get();
        return new Node[] { t.getSubject(), t.getPredicate(), t.getObject() };
    }

}
