/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract tests for triple output formats
 * @author rvesse
 *
 */
public abstract class AbstractTripleOutputFormatTests extends AbstractNodeTupleOutputFormatTests<Triple, TripleWritable> {

    @Override
    protected Iterator<TripleWritable> generateTuples(int num) {
        List<TripleWritable> ts = new ArrayList<TripleWritable>();
        for (int i = 0; i < num; i++) {
            Triple t = new Triple(NodeFactory.createURI("http://example.org/subjects/" + i), NodeFactory.createURI("http://example.org/predicate"), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger));
            ts.add(new TripleWritable(t));
        }
        return ts.iterator();
    }
}
