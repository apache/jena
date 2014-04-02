/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;

/**
 * Abstract tests for mappers
 * 
 * @author rvesse
 * @param <TKey>
 *            Mapper input key type
 * @param <TValue>
 *            Mapper input value type
 * @param <TIntermediateKey>
 *            Mapper output/Reducer input key type
 * @param <TIntermediateValue>
 *            Mapper output/Reducer input value type
 * @param <TReducedKey>
 *            Reducer output key type
 * @param <TReducedValue>
 *            Reducer output value type
 * 
 * 
 */
public abstract class AbstractMapReduceTests<TKey, TValue, TIntermediateKey, TIntermediateValue, TReducedKey, TReducedValue> {

    /**
     * Gets the mapper instance to test
     * 
     * @return Mapper instance
     */
    protected abstract Mapper<TKey, TValue, TIntermediateKey, TIntermediateValue> getMapperInstance();

    /**
     * Gets the reducer instance to test
     * 
     * @return Reducer instance
     */
    protected abstract Reducer<TIntermediateKey, TIntermediateValue, TReducedKey, TReducedValue> getReducerInstance();

    /**
     * Gets a map reduce driver that can be used to create a test case
     * 
     * @return Map reduce driver
     */
    protected MapReduceDriver<TKey, TValue, TIntermediateKey, TIntermediateValue, TReducedKey, TReducedValue> getMapReduceDriver() {
        return new MapReduceDriver<TKey, TValue, TIntermediateKey, TIntermediateValue, TReducedKey, TReducedValue>(
                this.getMapperInstance(), this.getReducerInstance());
    }
}
