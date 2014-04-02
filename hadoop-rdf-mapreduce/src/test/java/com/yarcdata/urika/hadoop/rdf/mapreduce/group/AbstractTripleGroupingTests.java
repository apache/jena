/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract tests for {@link AbstractTripleGroupingMapper} implementations
 * @author rvesse
 *
 */
public abstract class AbstractTripleGroupingTests extends AbstractNodeTupleGroupingTests<Triple, TripleWritable> {

    @Override
    protected TripleWritable createValue(int i) {
        return new TripleWritable(
                new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                        NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
    }
}
