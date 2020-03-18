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

package org.apache.jena.hadoop.rdf.mapreduce.characteristics;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.hadoop.rdf.mapreduce.AbstractMapReduceTests;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.CharacteristicSetWritable;
import org.apache.jena.hadoop.rdf.types.CharacteristicWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.junit.Test;

/**
 * Abstract tests for the {@link AbstractCharacteristicSetGeneratingReducer}
 * 
 * 
 * 
 * @param <TValue>
 * @param <T>
 */
public abstract class AbstractCharacteristicSetGeneratingReducerTests<TValue, T extends AbstractNodeTupleWritable<TValue>>
        extends AbstractMapReduceTests<LongWritable, T, NodeWritable, T, CharacteristicSetWritable, NullWritable> {

    /**
     * Create a tuple
     * 
     * @param i
     *            Key to use in creating the subject
     * @param predicateUri
     *            Predicate URI string
     * @return Tuple
     */
    protected abstract T createTuple(int i, String predicateUri);

    /**
     * Creates a set consisting of the given predicates
     * 
     * @param predicates
     *            Predicates
     * @return Set
     */
    protected CharacteristicSetWritable createSet(MapReduceDriver<LongWritable, T, NodeWritable, T, CharacteristicSetWritable, NullWritable> driver, int occurrences, String... predicates) {
        CharacteristicSetWritable set = new CharacteristicSetWritable();
        for (String predicateUri : predicates) {
            set.add(new CharacteristicWritable(NodeFactory.createURI(predicateUri)));
        }
        for (int i = 1; i <= occurrences; i++) {
            driver.addOutput(set, NullWritable.get());
        }
        return set;
    }

    /**
     * Test basic characteristic set computation
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_generating_reducer_01() throws IOException {
        MapReduceDriver<LongWritable, T, NodeWritable, T, CharacteristicSetWritable, NullWritable> driver = this
                .getMapReduceDriver();
        T tuple = this.createTuple(1, "http://predicate");
        driver.addInput(new LongWritable(1), tuple);

        this.createSet(driver, 1, "http://predicate");

        driver.runTest(false);
    }

    /**
     * Test basic characteristic set computation
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_generating_reducer_02() throws IOException {
        MapReduceDriver<LongWritable, T, NodeWritable, T, CharacteristicSetWritable, NullWritable> driver = this
                .getMapReduceDriver();
        T tuple = this.createTuple(1, "http://predicate");
        driver.addInput(new LongWritable(1), tuple);
        driver.addInput(new LongWritable(1), tuple);

        this.createSet(driver, 1, "http://predicate");

        driver.runTest(false);
    }

    /**
     * Test basic characteristic set computation
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_generating_reducer_03() throws IOException {
        MapReduceDriver<LongWritable, T, NodeWritable, T, CharacteristicSetWritable, NullWritable> driver = this
                .getMapReduceDriver();
        T tuple = this.createTuple(1, "http://predicate");
        driver.addInput(new LongWritable(1), tuple);
        tuple = this.createTuple(2, "http://predicate");
        driver.addInput(new LongWritable(2), tuple);

        this.createSet(driver, 2, "http://predicate");

        driver.runTest(false);
    }

    /**
     * Test basic characteristic set computation
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_generating_reducer_04() throws IOException {
        MapReduceDriver<LongWritable, T, NodeWritable, T, CharacteristicSetWritable, NullWritable> driver = this
                .getMapReduceDriver();
        T tuple = this.createTuple(1, "http://predicate");
        driver.addInput(new LongWritable(1), tuple);
        tuple = this.createTuple(1, "http://other");
        driver.addInput(new LongWritable(1), tuple);

        // Single entry sets
        this.createSet(driver, 1, "http://predicate");
        this.createSet(driver, 1, "http://other");
        
        // Two entry sets
        this.createSet(driver, 1, "http://predicate", "http://other");

        driver.runTest(false);
    }

    /**
     * Test basic characteristic set computation
     * 
     * @throws IOException
     */
    @Test
    public void characteristic_set_generating_reducer_05() throws IOException {
        MapReduceDriver<LongWritable, T, NodeWritable, T, CharacteristicSetWritable, NullWritable> driver = this
                .getMapReduceDriver();
        T tuple = this.createTuple(1, "http://predicate");
        driver.addInput(new LongWritable(1), tuple);
        tuple = this.createTuple(1, "http://other");
        driver.addInput(new LongWritable(2), tuple);
        tuple = this.createTuple(1, "http://third");
        driver.addInput(new LongWritable(3), tuple);

        // Single entry sets
        this.createSet(driver, 1, "http://predicate");
        this.createSet(driver, 1, "http://other");
        this.createSet(driver, 1, "http://third");

        // Two entry sets
        this.createSet(driver, 1, "http://predicate", "http://other");
        this.createSet(driver, 1, "http://predicate", "http://third");
        this.createSet(driver, 1, "http://other", "http://third");
        
        // Three entry sets
        this.createSet(driver, 1, "http://predicate", "http://other", "http://third");

        driver.runTest(false);
    }
}
