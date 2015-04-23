/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.hadoop.rdf.mapreduce.filter.positional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.hadoop.rdf.mapreduce.RdfMapReduceConstants;

/**
 * A quad filter which selects quads which have matching predicates
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadFilterByPredicateMapper<TKey> extends AbstractQuadFilterByPositionMapper<TKey> {

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
    protected boolean acceptsAllGraphs() {
        return true;
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
