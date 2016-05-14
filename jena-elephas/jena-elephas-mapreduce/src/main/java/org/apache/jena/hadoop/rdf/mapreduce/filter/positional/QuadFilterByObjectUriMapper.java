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
 * A quad filter which selects quads which have matching objects
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadFilterByObjectUriMapper<TKey> extends AbstractQuadFilterByPositionMapper<TKey> {

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
    protected boolean acceptsAllGraphs() {
        return true;
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
