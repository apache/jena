/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.yarcdata.urika.hadoop.rdf.mapreduce.AbstractMapReduceTests;
import com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics.AbstractCharacteristicSetGeneratingReducer;
import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;
import com.yarcdata.urika.hadoop.rdf.types.CharacteristicSetWritable;
import com.yarcdata.urika.hadoop.rdf.types.CharacteristicWritable;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * Abstract tests for the {@link AbstractCharacteristicSetGeneratingReducer}
 * 
 * @author rvesse
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
