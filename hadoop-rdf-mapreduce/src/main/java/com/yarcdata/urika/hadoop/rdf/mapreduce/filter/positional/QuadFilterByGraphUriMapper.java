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
 * A quad filter which selects quads which have matching subjects
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadFilterByGraphUriMapper<TKey> extends AbstractQuadFilterByPositionMapper<TKey> {

    private List<Node> graphs = new ArrayList<Node>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        // Get the subject URIs we are filtering on
        String[] graphUris = context.getConfiguration().getStrings(RdfMapReduceConstants.FILTER_GRAPH_URIS);
        if (graphUris != null) {
            for (String graphUri : graphUris) {
                this.graphs.add(NodeFactory.createURI(graphUri));
            }
        }
    }

    @Override
    protected boolean acceptsAllSubjects() {
        return true;
    }

    @Override
    protected boolean acceptsGraph(Node graph) {
        if (this.graphs.size() == 0)
            return false;
        return this.graphs.contains(graph);
    }

    @Override
    protected boolean acceptsAllPredicates() {
        return true;
    }

    @Override
    protected boolean acceptsAllObjects() {
        return true;
    }
}
