/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

import com.yarcdata.urika.hadoop.rdf.mapreduce.AbstractMapperTests;
import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;

/**
 * Abstract tests for {@link AbstractNodeTupleFilterMapper} implementations
 * which filter based on the validity of tuples
 * 
 * @author rvesse
 * 
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractNodeTupleFilterTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractMapperTests<LongWritable, T, LongWritable, T> {

    protected final void generateData(MapDriver<LongWritable, T, LongWritable, T> driver, int num) {
        for (int i = 0; i < num; i++) {
            LongWritable key = new LongWritable(i);
            if (i % 2 == 0 && !this.noValidInputs()) {
                T value = this.createValidValue(i);
                driver.addInput(key, value);
                if (!this.isInverted())
                    driver.addOutput(key, value);
            } else {
                T value = this.createInvalidValue(i);
                driver.addInput(key, value);
                if (this.isInverted())
                    driver.addOutput(key, value);
            }
        }
    }

    /**
     * Method that may be overridden for testing filters where all the generated
     * data will be rejected as invalid
     * 
     * @return True if there are no valid inputs, false otherwise (default)
     */
    protected boolean noValidInputs() {
        return false;
    }

    /**
     * Method that may be overridden for testing filters with inverted mode
     * enabled i.e. where normally valid input is considered invalid and vice
     * versa
     * 
     * @return True if inverted, false otherwise (default)
     */
    protected boolean isInverted() {
        return false;
    }

    /**
     * Creates an invalid value
     * 
     * @param i
     *            Key
     * @return Invalid value
     */
    protected abstract T createInvalidValue(int i);

    /**
     * Creates a valid value
     * 
     * @param i
     *            Key
     * @return Valid value
     */
    protected abstract T createValidValue(int i);

    protected final void testFilterValid(int num) throws IOException {
        MapDriver<LongWritable, T, LongWritable, T> driver = this.getMapDriver();
        this.generateData(driver, num);
        driver.runTest();
    }

    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void filter_valid_01() throws IOException {
        this.testFilterValid(1);
    }

    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void filter_valid_02() throws IOException {
        this.testFilterValid(100);
    }

    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void filter_valid_03() throws IOException {
        this.testFilterValid(1000);
    }

    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void filter_valid_04() throws IOException {
        this.testFilterValid(2500);
    }
}
