/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.types;

import java.io.DataInput;
import java.io.IOException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * A writable triple
 * 
 * @author rvesse
 * 
 */
public class TripleWritable extends AbstractNodeTupleWritable<Triple> {

    /**
     * Creates a new instance using the default NTriples node formatter
     */
    public TripleWritable() {
        this(null);
    }

    /**
     * Creates a new instance with a given value that uses a specific node
     * formatter
     * 
     * @param t
     *            Triple
     */
    public TripleWritable(Triple t) {
        super(t);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static TripleWritable read(DataInput input) throws IOException {
        TripleWritable t = new TripleWritable();
        t.readFields(input);
        return t;
    }

    @Override
    protected Triple createTuple(Node[] ns) {
        if (ns.length != 3)
            throw new IllegalArgumentException(String.format(
                    "Incorrect number of nodes to form a triple - got %d but expected 3", ns.length));
        return new Triple(ns[0], ns[1], ns[2]);
    }

    @Override
    protected Node[] createNodes(Triple tuple) {
        Triple t = this.get();
        return new Node[] { t.getSubject(), t.getPredicate(), t.getObject() };
    }
}
