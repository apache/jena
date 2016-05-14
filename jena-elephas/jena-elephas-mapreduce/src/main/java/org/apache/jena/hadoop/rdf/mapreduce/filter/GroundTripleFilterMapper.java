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

package org.apache.jena.hadoop.rdf.mapreduce.filter;

import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

/**
 * A triple filter which accepts only ground triples i.e. those with no blank
 * nodes or variables
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public class GroundTripleFilterMapper<TKey> extends AbstractTripleFilterMapper<TKey> {

    @Override
    protected boolean accepts(Object key, TripleWritable tuple) {
        Triple t = tuple.get();
        if (!t.isConcrete())
            return false;
        // Ground if all nodes are URI/Literal
        return (t.getSubject().isURI() || t.getSubject().isLiteral())
                && (t.getPredicate().isURI() || t.getPredicate().isLiteral())
                && (t.getObject().isURI() || t.getObject().isLiteral());
    }

}
