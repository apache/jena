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

package com.hp.hpl.jena.sparql.engine.index;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * An {@link IndexTable} implementation optimized for the case where there is
 * only a single common variable
 * 
 */
public class SetIndexTable implements IndexTable {

    private Var var;
    private Set<Node> values = new HashSet<>();

    /**
     * Creates a new index table
     * 
     * @param commonVars
     *            Common Variables
     * @param data
     *            Data
     */
    public SetIndexTable(Set<Var> commonVars, QueryIterator data) {
        if (commonVars.size() != 1)
            throw new IllegalArgumentException("Common Variables must be of size 1");

        this.var = commonVars.iterator().next();
        while (data.hasNext()) {
            Binding binding = data.next();
            Node value = binding.get(this.var);

            if (value == null)
                continue;
            this.values.add(value);
        }
    }

    @Override
    public boolean containsCompatibleWithSharedDomain(Binding binding) {
        Node value = binding.get(this.var);
        if (value == null)
            return true;
        return this.values.contains(value);
    }

}
