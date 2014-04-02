/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.types;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparable;

import com.hp.hpl.jena.graph.Node;

/**
 * Represents a characteristic for a single node and contains the node and a
 * count associated with that node
 * <p>
 * Note that characteristics are compared based upon only the nodes and not
 * their counts
 * </p>
 * 
 * @author rvesse
 * 
 */
public class CharacteristicWritable implements WritableComparable<CharacteristicWritable> {

    private NodeWritable node = new NodeWritable();
    private LongWritable count = new LongWritable();

    /**
     * Creates an empty characteristic writable
     */
    public CharacteristicWritable() {
        this(null);
    }

    /**
     * Creates a characteristic writable with the given node and the default
     * count of 1
     * 
     * @param n
     *            Node
     */
    public CharacteristicWritable(Node n) {
        this(n, 1);
    }

    /**
     * Creates a characteristic writable with the given node and count
     * 
     * @param n
     *            Node
     * @param count
     *            Count
     */
    public CharacteristicWritable(Node n, long count) {
        this.node.set(n);
        this.count.set(count);
    }

    /**
     * Creates a new instance and reads in its data from the given input
     * 
     * @param input
     *            Input
     * @return New instance
     * @throws IOException
     */
    public static CharacteristicWritable read(DataInput input) throws IOException {
        CharacteristicWritable cw = new CharacteristicWritable();
        cw.readFields(input);
        return cw;
    }

    /**
     * Gets the node
     * 
     * @return Node
     */
    public NodeWritable getNode() {
        return this.node;
    }

    /**
     * Gets the count
     * 
     * @return Count
     */
    public LongWritable getCount() {
        return this.count;
    }

    /**
     * Increments the count by 1
     */
    public void increment() {
        this.increment(1);
    }

    /**
     * Increments the count by the given value
     * 
     * @param l
     *            Value to increment by
     */
    public void increment(long l) {
        this.count.set(this.count.get() + l);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        this.node.write(output);
        this.count.write(output);
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        this.node.readFields(input);
        this.count.readFields(input);
    }

    @Override
    public int compareTo(CharacteristicWritable o) {
        return this.node.compareTo(o.node);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CharacteristicWritable))
            return false;
        return this.compareTo((CharacteristicWritable) other) == 0;
    }

    @Override
    public int hashCode() {
        return this.node.hashCode();
    }

    @Override
    public String toString() {
        return "(" + this.node.toString() + ", " + this.count.toString() + ")";
    }

}
