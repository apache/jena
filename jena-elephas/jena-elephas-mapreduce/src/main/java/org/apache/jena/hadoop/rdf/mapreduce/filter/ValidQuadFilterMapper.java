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

import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.sparql.core.Quad ;

/**
 * A quad filter mapper which accepts only valid quads, by which we mean they
 * meet the following criteria:
 * <ul>
 * <li>Graph is a URI or Blank Node</li>
 * <li>Subject is a URI or Blank Node</li>
 * <li>Predicate is a URI</li>
 * <li>Object is a URI, Blank Node or Literal</li>
 * </ul>
 * 
 * 
 * 
 * @param <TKey>
 */
public final class ValidQuadFilterMapper<TKey> extends AbstractQuadFilterMapper<TKey> {

    @Override
    protected final boolean accepts(TKey key, QuadWritable tuple) {
        Quad q = tuple.get();
        return (q.getGraph().isURI() || q.getGraph().isBlank()) && (q.getSubject().isURI() || q.getSubject().isBlank())
                && q.getPredicate().isURI() && (q.getObject().isURI() || q.getObject().isBlank() || q.getObject().isLiteral());
    }

}
