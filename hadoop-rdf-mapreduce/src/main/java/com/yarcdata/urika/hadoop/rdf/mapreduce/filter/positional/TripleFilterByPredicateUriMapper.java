/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter.positional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.yarcdata.urika.hadoop.rdf.mapreduce.RdfMapReduceConstants;

/**
 * A triple filter which selects triples which have matching predicates
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class TripleFilterByPredicateUriMapper<TKey> extends AbstractTripleFilterByPositionMapper<TKey> {

    private List<Node> predicates = new ArrayList<Node>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        // Get the predicate URIs we are filtering on
        String[] predicateUris = context.getConfiguration().getStrings(RdfMapReduceConstants.FILTER_PREDICATE_URIS);
        if (predicateUris != null) {
            for (String predicateUri : predicateUris) {
                this.predicates.add(NodeFactory.createURI(predicateUri));
            }
        }
    }

    @Override
    protected boolean acceptsAllSubjects() {
        return true;
    }

    @Override
    protected boolean acceptsPredicate(Node predicate) {
        if (this.predicates.size() == 0)
            return false;
        return this.predicates.contains(predicate);
    }

    @Override
    protected boolean acceptsAllObjects() {
        return true;
    }
}
