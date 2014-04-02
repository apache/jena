/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics.TripleCharacteristicSetGeneratingReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.group.TripleGroupBySubjectMapper;
import com.yarcdata.urika.hadoop.rdf.types.CharacteristicSetWritable;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link TripleCharacteristicSetGeneratingReducer}
 * 
 * @author rvesse
 * 
 */
public class TripleCharacteristicSetGeneratingReducerTest extends AbstractCharacteristicSetGeneratingReducerTests<Triple, TripleWritable> {

    @Override
    protected Mapper<LongWritable, TripleWritable, NodeWritable, TripleWritable> getMapperInstance() {
        return new TripleGroupBySubjectMapper<LongWritable>();
    }

    @Override
    protected Reducer<NodeWritable, TripleWritable, CharacteristicSetWritable, NullWritable> getReducerInstance() {
        return new TripleCharacteristicSetGeneratingReducer();
    }

    @Override
    protected TripleWritable createTuple(int i, String predicateUri) {
        return new TripleWritable(new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI(predicateUri),
                NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
    }

}
