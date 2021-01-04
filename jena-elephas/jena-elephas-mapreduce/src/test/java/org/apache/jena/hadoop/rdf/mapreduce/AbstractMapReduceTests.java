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
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.jena.sys.JenaSystem;

/**
 * Abstract tests for mappers
 * 
 * 
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

    static { JenaSystem.init(); }
    
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
