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
 * A triple filter mapper which accepts only valid triples, by which we mean they
 * meet the following criteria:
 * <ul>
 * <li>Subject is a URI or Blank Node</li>
 * <li>Predicate is a URI</li>
 * <li>Object is a URI, Blank Node or Literal</li>
 * </ul>
 * 
 * 
 * 
 * @param <TKey>
 */
public final class ValidTripleFilterMapper<TKey> extends AbstractTripleFilterMapper<TKey> {

    @Override
    protected final boolean accepts(TKey key, TripleWritable tuple) {
        Triple t = tuple.get();
        return (t.getSubject().isURI() || t.getSubject().isBlank()) && t.getPredicate().isURI()
                && (t.getObject().isURI() || t.getObject().isBlank() || t.getObject().isLiteral());
    }

}
