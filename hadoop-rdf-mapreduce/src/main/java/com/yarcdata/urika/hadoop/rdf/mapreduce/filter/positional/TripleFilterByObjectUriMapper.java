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
 * A triple filter which selects triples which have matching objects
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class TripleFilterByObjectUriMapper<TKey> extends AbstractTripleFilterByPositionMapper<TKey> {

    private List<Node> objects = new ArrayList<Node>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        // Get the subject URIs we are filtering on
        String[] objectUris = context.getConfiguration().getStrings(RdfMapReduceConstants.FILTER_OBJECT_URIS);
        if (objectUris != null) {
            for (String objectUri : objectUris) {
                this.objects.add(NodeFactory.createURI(objectUri));
            }
        }
    }

    @Override
    protected boolean acceptsObject(Node object) {
        if (this.objects.size() == 0)
            return false;
        return this.objects.contains(object);
    }

    @Override
    protected boolean acceptsAllPredicates() {
        return true;
    }

    @Override
    protected boolean acceptsAllSubjects() {
        return true;
    }
}
