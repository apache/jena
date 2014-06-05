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

package org.apache.jena.hadoop.rdf.mapreduce.count.positional;

import org.apache.jena.hadoop.rdf.mapreduce.count.NodeCountReducer;
import org.apache.jena.hadoop.rdf.mapreduce.count.TripleNodeCountMapper;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;


/**
 * A mapper for counting subject node usages within triples designed primarily for use
 * in conjunction with {@link NodeCountReducer}
 * 
 * 
 * 
 * @param <TKey> Key type
 */
public class TripleSubjectCountMapper<TKey> extends TripleNodeCountMapper<TKey> {

    @Override
    protected NodeWritable[] getNodes(TripleWritable tuple) {
        return new NodeWritable[] { new NodeWritable(tuple.get().getSubject()) };
    }
}
