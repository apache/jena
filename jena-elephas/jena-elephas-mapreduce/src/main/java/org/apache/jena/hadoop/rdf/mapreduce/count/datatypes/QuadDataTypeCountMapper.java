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

package org.apache.jena.hadoop.rdf.mapreduce.count.datatypes;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.hadoop.rdf.mapreduce.count.NodeCountReducer;
import org.apache.jena.hadoop.rdf.mapreduce.count.QuadNodeCountMapper;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.QuadWritable;

/**
 * A mapper for counting data type usages within quads designed primarily for
 * use in conjunction with {@link NodeCountReducer}
 * <p>
 * This mapper extracts the data types for typed literal objects and converts
 * them into nodes so they can be counted
 * </p>
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadDataTypeCountMapper<TKey> extends QuadNodeCountMapper<TKey> {

    private static final NodeWritable[] EMPTY = new NodeWritable[0];

    @Override
    protected NodeWritable[] getNodes(QuadWritable tuple) {
        Node object = tuple.get().getObject();
        if (!object.isLiteral())
            return EMPTY;
        String dtUri = object.getLiteralDatatypeURI();
        if (dtUri == null)
            return EMPTY;
        return new NodeWritable[] { new NodeWritable(NodeFactory.createURI(dtUri)) };
    }
}
