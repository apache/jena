/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract tests for {@link AbstractNodeTupleSplitToNodesMapper}
 * implementations that work on Triples
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractTripleSplitWithNodesTests extends AbstractNodeTupleSplitWithNodesTests<Triple, TripleWritable> {

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
