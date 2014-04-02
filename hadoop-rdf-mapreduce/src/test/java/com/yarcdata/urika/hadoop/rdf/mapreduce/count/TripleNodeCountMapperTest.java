/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link TripleNodeCountMapper}
 * 
 * @author rvesse
 * 
 */
public class TripleNodeCountMapperTest extends AbstractNodeTupleNodeCountTests<Triple, TripleWritable> {

    @Override
    protected Mapper<LongWritable, TripleWritable, NodeWritable, LongWritable> getInstance() {
        return new TripleNodeCountMapper<LongWritable>();
    }

    @Override
    protected TripleWritable createValue(int i) {
        return new TripleWritable(
                new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                        NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
    }

    @Override
    protected NodeWritable[] getNodes(TripleWritable tuple) {
        Triple t = tuple.get();
        return new NodeWritable[] { new NodeWritable(t.getSubject()), new NodeWritable(t.getPredicate()),
                new NodeWritable(t.getObject()) };
    }

}
