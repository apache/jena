/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

import com.yarcdata.urika.hadoop.rdf.mapreduce.AbstractMapperTests;
import com.yarcdata.urika.hadoop.rdf.mapreduce.split.AbstractNodeTupleSplitToNodesMapper;
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
public abstract class AbstractNodeTupleGroupingTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractMapperTests<LongWritable, T, NodeWritable, T> {

    /**
     * Generates data for use in tests
     * 
     * @param driver
     *            Driver
     * @param num
     *            Number of tuples to generate
     */
    protected void generateData(MapDriver<LongWritable, T, NodeWritable, T> driver, int num) {
        for (int i = 0; i < num; i++) {
            LongWritable inputKey = new LongWritable(i);
            T value = this.createValue(i);
            NodeWritable outputKey = this.getOutputKey(value);

            driver.addInput(inputKey, value);
            driver.addOutput(outputKey, value);
        }
    }

    protected abstract T createValue(int i);

    protected abstract NodeWritable getOutputKey(T tuple);

    protected final void testGrouping(int num) throws IOException {
        MapDriver<LongWritable, T, NodeWritable, T> driver = this.getMapDriver();
        this.generateData(driver, num);
        driver.runTest();
    }

    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_01() throws IOException {
        this.testGrouping(1);
    }
    
    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_02() throws IOException {
        this.testGrouping(100);
    }
    
    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_03() throws IOException {
        this.testGrouping(1000);
    }
    
    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_04() throws IOException {
        this.testGrouping(2500);
    }
}
