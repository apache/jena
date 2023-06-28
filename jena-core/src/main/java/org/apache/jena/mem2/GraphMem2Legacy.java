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

package org.apache.jena.mem2;

import org.apache.jena.mem2.store.legacy.LegacyTripleStore;

/**
 * A graph that stores triples in memory. This class is not thread-safe.
 * <p>
 * Purpose: Use this graph implementation if you want to maintain the 'old' behavior of GraphMem or if your memory
 * constraints prevent you from utilizing more memory-intensive solutions.
 * <p>
 * Slightly improved performance compared to {@link org.apache.jena.mem.GraphMem}
 * Simplified implementation, primarily due to lack of support for Iterator#remove
 * <p>
 * The heritage of GraphMem:
 * - Same basic structure
 * - Same memory consumption
 * - Also based on HashCommon
 * <p>
 * This implementation is based on the original {@link org.apache.jena.mem.GraphMem} implementation.
 * The main difference is that it strictly uses term equality for all nodes.
 * The inner workings of the used structures like ArrayBunch and HashedBunchMap are not changed.
 */
public class GraphMem2Legacy extends GraphMem2 {

    public GraphMem2Legacy() {
        super(new LegacyTripleStore());
    }

}
