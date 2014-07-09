package org.apache.jena.hadoop.rdf.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

public class TestDistinctTriples
        extends
        AbstractMapReduceTests<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> {

    @Override
    protected Mapper<LongWritable, TripleWritable, TripleWritable, NullWritable> getMapperInstance() {
        return new ValuePlusNullMapper<LongWritable, TripleWritable>();
    }

    @Override
    protected Reducer<TripleWritable, NullWritable, NullWritable, TripleWritable> getReducerInstance() {
        return new NullPlusKeyReducer<TripleWritable, NullWritable>();
    }

    @Test
    public void distinct_triples_01() throws IOException {
        MapReduceDriver<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> driver = this
                .getMapReduceDriver();

        Triple t = new Triple(NodeFactory.createURI("urn:s"), NodeFactory.createURI("urn:p"),
                NodeFactory.createLiteral("1"));
        TripleWritable tw = new TripleWritable(t);
        driver.addInput(new LongWritable(1), tw);
        driver.addOutput(NullWritable.get(), tw);

        driver.runTest();
    }

    @Test
    public void distinct_triples_02() throws IOException {
        MapReduceDriver<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> driver = this
                .getMapReduceDriver();

        Triple t = new Triple(NodeFactory.createURI("urn:s"), NodeFactory.createURI("urn:p"),
                NodeFactory.createLiteral("1"));
        TripleWritable tw = new TripleWritable(t);
        for (int i = 0; i < 100; i++) {
            driver.addInput(new LongWritable(i), tw);
        }
        driver.addOutput(NullWritable.get(), tw);

        driver.runTest();
    }
    
    @Test
    public void distinct_triples_03() throws IOException {
        MapReduceDriver<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> driver = this
                .getMapReduceDriver();

        Triple t = new Triple(NodeFactory.createURI("urn:s"), NodeFactory.createURI("urn:p"),
                NodeFactory.createLiteral("1"));
        Triple t2 = new Triple(t.getSubject(), t.getPredicate(), NodeFactory.createLiteral("2"));
        Assert.assertNotEquals(t, t2);
        
        TripleWritable tw = new TripleWritable(t);
        TripleWritable tw2 = new TripleWritable(t2);
        Assert.assertNotEquals(tw, tw2);
        
        driver.addInput(new LongWritable(1), tw);
        driver.addInput(new LongWritable(2), tw2);
        driver.addOutput(NullWritable.get(), tw);
        driver.addOutput(NullWritable.get(), tw2);
        
        driver.runTest(false);
    }
}
