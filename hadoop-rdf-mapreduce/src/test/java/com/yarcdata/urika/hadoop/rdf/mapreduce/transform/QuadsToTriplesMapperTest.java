/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.transform;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.mapreduce.AbstractMapperTests;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link QuadsToTriplesMapper}
 * 
 * @author rvesse
 * 
 */
public class QuadsToTriplesMapperTest extends AbstractMapperTests<LongWritable, QuadWritable, LongWritable, TripleWritable> {

    @Override
    protected Mapper<LongWritable, QuadWritable, LongWritable, TripleWritable> getInstance() {
        return new QuadsToTriplesMapper<LongWritable>();
    }

    protected void generateData(MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver, int num) {
        for (int i = 0; i < num; i++) {
            Triple t = new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                    NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger));
            Quad q = new Quad(Quad.defaultGraphNodeGenerated, t);
            driver.addInput(new LongWritable(i), new QuadWritable(q));
            driver.addOutput(new LongWritable(i), new TripleWritable(t));
        }
    }

    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_01() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();

        Triple t = new Triple(NodeFactory.createURI("http://s"), NodeFactory.createURI("http://p"),
                NodeFactory.createLiteral("test"));
        Quad q = new Quad(Quad.defaultGraphNodeGenerated, t);
        driver.withInput(new Pair<LongWritable, QuadWritable>(new LongWritable(1), new QuadWritable(q))).withOutput(
                new Pair<LongWritable, TripleWritable>(new LongWritable(1), new TripleWritable(t)));
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_02() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();
        this.generateData(driver, 100);
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_03() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();
        this.generateData(driver, 1000);
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_04() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();
        this.generateData(driver, 10000);
        driver.runTest();
    }
}
