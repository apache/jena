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
public class QuadFilterBySubjectUriMapper<TKey> extends AbstractQuadFilterByPositionMapper<TKey> {

    private List<Node> subjects = new ArrayList<Node>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);

        // Get the subject URIs we are filtering on
        String[] subjectUris = context.getConfiguration().getStrings(RdfMapReduceConstants.FILTER_SUBJECT_URIS);
        if (subjectUris != null) {
            for (String subjectUri : subjectUris) {
                this.subjects.add(NodeFactory.createURI(subjectUri));
            }
        }
    }
    
    @Override
    protected boolean acceptsAllGraphs() {
        return true;
    }

    @Override
    protected boolean acceptsSubject(Node subject) {
        if (this.subjects.size() == 0)
            return false;
        return this.subjects.contains(subject);
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
