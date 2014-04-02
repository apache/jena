/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

import com.yarcdata.urika.hadoop.rdf.mapreduce.AbstractMapperTests;
import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * Abstract tests for {@link AbstractNodeTupleSplitToNodesMapper}
 * implementations
 * 
 * @author rvesse
 * 
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractNodeTupleSplitToNodesTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractMapperTests<LongWritable, T, LongWritable, NodeWritable> {

    /**
     * Generates data for use in tests
     * 
     * @param driver
     *            Driver
     * @param num
     *            Number of tuples to generate
     */
    protected void generateData(MapDriver<LongWritable, T, LongWritable, NodeWritable> driver, int num) {
        for (int i = 0; i < num; i++) {
            LongWritable key = new LongWritable(i);
            T value = this.createValue(i);
            NodeWritable[] nodes = this.getNodes(value);

            driver.addInput(key, value);
            for (NodeWritable n : nodes) {
                driver.addOutput(key, n);
            }
        }
    }

    protected abstract T createValue(int i);

    protected abstract NodeWritable[] getNodes(T tuple);

    protected final void testSplitToNodes(int num) throws IOException {
        MapDriver<LongWritable, T, LongWritable, NodeWritable> driver = this.getMapDriver();
        this.generateData(driver, num);
        driver.runTest();
    }

    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_01() throws IOException {
        this.testSplitToNodes(1);
    }
    
    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_02() throws IOException {
        this.testSplitToNodes(100);
    }
    
    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_03() throws IOException {
        this.testSplitToNodes(1000);
    }
    
    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_04() throws IOException {
        this.testSplitToNodes(2500);
    }
}
