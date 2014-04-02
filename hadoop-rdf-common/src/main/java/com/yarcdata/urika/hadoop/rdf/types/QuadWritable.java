/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.types;

import java.io.DataInput;
import java.io.IOException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A writable quad
 * 
 * @author rvesse
 * 
 */
public class QuadWritable extends AbstractNodeTupleWritable<Quad> {

    /**
     * Creates a new empty instance
     */
    public QuadWritable() {
        this(null);
    }

    /**
     * Creates a new instance with the given value
     * 
     * @param q
     *            Quad
     */
    public QuadWritable(Quad q) {
        super(q);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static QuadWritable read(DataInput input) throws IOException {
        QuadWritable q = new QuadWritable();
        q.readFields(input);
        return q;
    }

    @Override
    protected Quad createTuple(Node[] ns) {
        if (ns.length != 4)
            throw new IllegalArgumentException(String.format("Incorrect number of nodes to form a quad - got %d but expected 4",
                    ns.length));
        return new Quad(ns[0], ns[1], ns[2], ns[3]);
    }

    @Override
    protected Node[] createNodes(Quad tuple) {
        Quad q = this.get();
        return new Node[] { q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject() };
    }

}
