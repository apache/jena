/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.types;

import java.io.DataInput;
import java.io.IOException;

import org.apache.jena.atlas.lib.Tuple;
import com.hp.hpl.jena.graph.Node;

/**
 * A writable RDF tuple
 * <p>
 * Unlike the more specific {@link TripleWritable} and {@link QuadWritable} this
 * class allows for arbitrary length tuples and does not restrict tuples to
 * being of uniform size.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class NodeTupleWritable extends AbstractNodeTupleWritable<Tuple<Node>> {

    /**
     * Creates a new empty instance
     */
    public NodeTupleWritable() {
        this(null);
    }

    /**
     * Creates a new instance with the given value
     * 
     * @param tuple
     *            Tuple
     */
    public NodeTupleWritable(Tuple<Node> tuple) {
        super(tuple);
    }

    /**
     * Creates a new instance from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static NodeTupleWritable read(DataInput input) throws IOException {
        NodeTupleWritable t = new NodeTupleWritable();
        t.readFields(input);
        return t;
    }

    @Override
    protected Tuple<Node> createTuple(Node[] ns) {
        return Tuple.create(ns);
    }

    @Override
    protected Node[] createNodes(Tuple<Node> tuple) {
        return tuple.tuple();
    }
}
