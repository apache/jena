/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.hadoop.rdf.mapreduce;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.jena.sys.JenaSystem;

/**
 * Abstract tests for mappers
 * 
 * 
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

    static { JenaSystem.init(); }
    
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
        MapDriver<TKeyIn, TValueIn, TKeyOut, TValueOut> driver = new MapDriver<TKeyIn, TValueIn, TKeyOut, TValueOut>(
                this.getInstance());
        this.configureDriver(driver);
        return driver;
    }

    /**
     * Method that may be overridden by test harnesses which need to configure
     * the driver in more detail e.g. add configuration keys
     * 
     * @param driver
     *            Driver
     */
    protected void configureDriver(MapDriver<TKeyIn, TValueIn, TKeyOut, TValueOut> driver) {
        // Does nothing
    }
}
