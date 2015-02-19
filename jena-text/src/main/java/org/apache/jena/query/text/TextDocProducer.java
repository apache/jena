/**
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

package org.apache.jena.query.text;

import com.hp.hpl.jena.sparql.core.DatasetChanges ;

/**
 <p>
    <code>TextDocProducer</code> is a marker interface for a piece of
    middleware that observes changes to a dataset and can
    add or remove elements in a text index of the contents
    of triples. The default implementation {@link TextDocProducerTriples}
    treats each RDF triple as a single text document, but
    other strategies are possible. Implementations of this
    interface are responsible for accumulating and managing
    state; this interface merely provides an interaction point
    for intercepting dataset changes.
</p>
*/
public interface TextDocProducer extends DatasetChanges
{
}

