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
 * Tests for the {@link TriplesToQuadsBySubjectMapper}
 * 
 * @author rvesse
 * 
 */
public class TriplesToQuadsBySubjectMapperTest extends AbstractMapperTests<LongWritable, TripleWritable, LongWritable, QuadWritable> {

    @Override
    protected Mapper<LongWritable, TripleWritable, LongWritable, QuadWritable> getInstance() {
        return new TriplesToQuadsBySubjectMapper<LongWritable>();
    }

    protected void generateData(MapDriver<LongWritable, TripleWritable, LongWritable, QuadWritable> driver, int num) {
        for (int i = 0; i < num; i++) {
            Triple t = new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                    NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger));
            Quad q = new Quad(t.getSubject(), t);
            driver.addInput(new LongWritable(i), new TripleWritable(t));
            driver.addOutput(new LongWritable(i), new QuadWritable(q));
        }
    }

    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void triples_to_quads_mapper_01() throws IOException {
        MapDriver<LongWritable, TripleWritable, LongWritable, QuadWritable> driver = this.getMapDriver();

        Triple t = new Triple(NodeFactory.createURI("http://s"), NodeFactory.createURI("http://p"),
                NodeFactory.createLiteral("test"));
        Quad q = new Quad(t.getSubject(), t);
        driver.withInput(new Pair<LongWritable, TripleWritable>(new LongWritable(1), new TripleWritable(t))).withOutput(
                new Pair<LongWritable, QuadWritable>(new LongWritable(1), new QuadWritable(q)));
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void triples_to_quads_mapper_02() throws IOException {
        MapDriver<LongWritable, TripleWritable, LongWritable, QuadWritable> driver = this.getMapDriver();
        this.generateData(driver, 100);
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void triples_to_quads_mapper_03() throws IOException {
        MapDriver<LongWritable, TripleWritable, LongWritable, QuadWritable> driver = this.getMapDriver();
        this.generateData(driver, 1000);
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void triples_to_quads_mapper_04() throws IOException {
        MapDriver<LongWritable, TripleWritable, LongWritable, QuadWritable> driver = this.getMapDriver();
        this.generateData(driver, 10000);
        driver.runTest();
    }
}
