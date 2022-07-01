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

package org.apache.jena.graph;

import org.apache.jena.mem.GraphMem;

/**
 * Interface for expressing capabilities.
 */
@SuppressWarnings("deprecation")
public interface Capabilities {
    /**
     * Answer true iff Graph::size() is accurate.
     */
    boolean sizeAccurate();

    /**
     * Answer true iff {@link Graph#add} can be used to add at least some triples to the
     * graph.
     */
    boolean addAllowed();

    /**
     * Answer true iff {@link Graph#delete} can be used to remove at least some triples
     * from the graph.
     */
    boolean deleteAllowed();

    /**
     * Answer true iff this graph compares literals for equality by value in find()
     * operations, rather just applying RDFTerm equality. Only applies to
     * {@link GraphMem}. TDB models have this effect because of canonicalization for
     * certain datatypes and ranges, SDB models don't.
     */
    boolean handlesLiteralTyping();
}
