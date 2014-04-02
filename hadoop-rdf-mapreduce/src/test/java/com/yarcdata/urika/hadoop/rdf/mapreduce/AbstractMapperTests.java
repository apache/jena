/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;

/**
 * Abstract tests for mappers
 * 
 * @author rvesse
 * @param <TKeyIn>
 *            Input key type
 * @param <TValueIn>
 *            Input value type
 * @param <TKeyOut>
 *            Output key type
 * @param <TValueOut>
 *            Output value type
 * 
 */
public abstract class AbstractMapperTests<TKeyIn, TValueIn, TKeyOut, TValueOut> {

    /**
     * Gets the mapper instance to test
     * 
     * @return Mapper instance
     */
    protected abstract Mapper<TKeyIn, TValueIn, TKeyOut, TValueOut> getInstance();

    /**
     * Gets a map driver that can be used to create a test case
     * 
     * @return Map driver
     */
    protected MapDriver<TKeyIn, TValueIn, TKeyOut, TValueOut> getMapDriver() {
        MapDriver<TKeyIn, TValueIn, TKeyOut, TValueOut> driver = new MapDriver<TKeyIn, TValueIn, TKeyOut, TValueOut>(this.getInstance());
        this.configureDriver(driver);
        return driver;
    }
    
    /**
     * Method that may be overridden by test harnesses which need to configure the driver in more detail e.g. add configuration keys
     * @param driver Driver
     */
    protected void configureDriver(MapDriver<TKeyIn, TValueIn, TKeyOut, TValueOut> driver) {
        // Does nothing
    }
}
