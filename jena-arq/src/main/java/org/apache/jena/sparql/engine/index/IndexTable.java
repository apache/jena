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

package org.apache.jena.sparql.engine.index;

import org.apache.jena.sparql.engine.binding.Binding ;

/**
 * Interface for indexes that are used for identifying matching
 * {@link org.apache.jena.sparql.engine.binding.Binding}s when
 * {@link org.apache.jena.sparql.engine.iterator.QueryIterMinus} is trying to determine
 * which Bindings need to be removed.
 */
public interface IndexTable {
    // Contribution from P Gearon
    /**
     * Is there a binding in the table that has a shared domain (variables in common) and
     * is join-compatible? This is teh condition for MINUS to exclude the argument
     * binding.
     */
	public abstract boolean containsCompatibleWithSharedDomain(Binding binding);
}
